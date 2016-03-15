package controllers

import org.joda.time.DateTime
import play.api._
import play.api.libs.json.{JsPath, Json}
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._
import javax.inject.Inject
import play.api.i18n.{MessagesApi, I18nSupport}
import com.redis._

case class QueryData(keyword: String, since: DateTime)
case class KeywordData(keyword: String)

class Application @Inject() (implicit val messagesApi: MessagesApi, webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  val queryForm = Form(
    mapping(
      "keyword" -> text,
      "since" -> jodaDate
    )(QueryData.apply)(QueryData.unapply)
  )

  val keywordForm = Form(
    mapping(
      "keyword" -> text
    )(KeywordData.apply)(KeywordData.unapply)
  )

  def index = Action { implicit req =>
    val form = Storage.getKeyword match {
      case Some(k) => queryForm.fill(QueryData(k, DateTime.now))
      case _ => queryForm
    }
    Ok(views.html.index(form, None))
  }

  def search = Action { implicit req =>
    val queryData = queryForm.bindFromRequest.get
    Storage.getRanking(queryData.toString) match {
      case Some(r) => {
        Ok(views.html.index(queryForm.fill(queryData), Some(r)))
      }
      case None =>
        val ranking = TwitterApi.ranking(
          queryData.since,
          queryData.keyword
        )
        Storage.setRanking(queryData.toString, ranking)
        Ok(views.html.index(queryForm.fill(queryData), Some(ranking)))
    }
  }

  def keyword = Action { implicit req =>
    Ok(views.html.keyword(keywordForm))
  }

  def createKeyword = Action { implicit req =>
    val keywordData = keywordForm.bindFromRequest.get
    Storage.setKeyword(keywordData.keyword)
    Logger.debug("Create keyword: " + keywordData.keyword)
    Redirect(routes.Application.keyword)
  }
}