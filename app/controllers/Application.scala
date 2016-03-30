package controllers

import org.joda.time.DateTime
import play.api._
import play.api.libs.json.{JsPath, Json}
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._
import javax.inject.Inject

import com.typesafe.config.ConfigFactory

import collection.JavaConversions._
import is.tagomor.woothee.Classifier
import play.api.i18n.{I18nSupport, MessagesApi}

case class QueryData(keyword: String, sort: String, since: DateTime)
case class KeywordData(keyword: String)

class Application @Inject() (implicit val messagesApi: MessagesApi, webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  val queryForm = Form(
    mapping(
      "keyword" -> nonEmptyText,
      "sort" -> nonEmptyText,
      "since" -> jodaDate
    )(QueryData.apply)(QueryData.unapply)
  )

  val keywordForm = Form(
    mapping(
      "keyword" -> nonEmptyText
    )(KeywordData.apply)(KeywordData.unapply)
  )

  def index = Action { implicit req =>
    val form = Storage.getKeyword match {
      case Some(k) => queryForm.fill(QueryData(k, "created", DateTime.now))
      case _ => queryForm
    }
    Ok(views.html.index(form, None, None, None))
  }

  def search = Action { implicit req =>
    val userAgent = req.headers.get("User-Agent") match {
      case Some(u) => Some(Classifier.parse(u).toMap)
      case _ => None
    }
    val queryData = queryForm.bindFromRequest.get
    val ranking = TweetRankingService.searchOrCache(queryData)
    val target = TwitterApi.searchTarget(queryData.since, queryData.keyword)
    Ok(views.html.index(queryForm.fill(queryData), ranking, target, userAgent))
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