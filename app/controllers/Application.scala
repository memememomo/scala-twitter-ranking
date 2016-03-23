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

case class QueryData(keyword: String, sort: String, since: DateTime)
case class KeywordData(keyword: String)

class Application @Inject() (implicit val messagesApi: MessagesApi, webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  val queryForm = Form(
    mapping(
      "keyword" -> text,
      "sort" -> text,
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
      case Some(k) => queryForm.fill(QueryData(k, "created", DateTime.now))
      case _ => queryForm
    }
    Ok(views.html.index(form, None, None))
  }

  def search = Action { implicit req =>
    val queryData = queryForm.bindFromRequest.get
    val ranking = searchOrCache(queryData)
    val target = TwitterApi.searchTarget(queryData.since, queryData.keyword)
    Ok(views.html.index(queryForm.fill(queryData), ranking, target))
 }

  def searchOrCache(queryData: QueryData) = {
    TwitterApi.ranking(
      queryData.since,
      queryData.sort,
      queryData.keyword
    ) match {
      case ranking if ranking.length > 0 =>
        Storage.setRanking(queryData.toString, ranking)
        Some(sortRanking(ranking, queryData.sort))
      case _ =>
        Storage.getRanking(queryData.toString) match {
          case Some(ranking) => {
            Some(sortRanking(ranking, queryData.sort))
          }
          case _ => None
        }
    }
  }

  def cacheOrSearch(queryData: QueryData) = {
    Storage.getRanking(queryData.keyword) match {
      case Some(ranking) => Some(sortRanking(ranking, queryData.sort))
      case None => {
        TwitterApi.ranking(
          queryData.since,
          queryData.sort,
          queryData.keyword
        ) match {
          case ranking =>
            Storage.setRanking(queryData.toString, ranking)
            Some(sortRanking(ranking, queryData.sort))
          case _ => None
        }
      }
    }
  }

  def sortRanking(ranking: List[Ranking], sort: String) = {
     ranking.sortWith((a, b) =>
        sort match {
          case "like" => a.favorite > b.favorite
          case "rt" => a.retweet > b.retweet
          case "created" => b.created.after(a.created)
        }
     )
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