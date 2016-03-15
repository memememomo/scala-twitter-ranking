package controllers

import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}

import akka.serialization.Serialization
import org.apache.commons.lang3
import org.apache.commons.lang3.SerializationUtils
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
import serialization._
import Parse.Implicits.parseByteArray
import play.api.libs.json._
import play.api.libs.functional.syntax._

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

  val cacheSeconds = 30L * 60



  def index = Action { implicit req =>
    val redisClient = new RedisClient("localhost", 6379)
    val form = redisClient.get[String]("keyword") match {
      case Some(k) => queryForm.fill(QueryData(k, DateTime.now))
      case _ => queryForm
    }
    Ok(views.html.index(form, None))
  }

  def search = Action { implicit req =>
    implicit val rankingReads: Reads[Ranking] = (
      (JsPath \ "rank").read[Int] and
      (JsPath \ "name").read[String] and
      (JsPath \ "text").read[String] and
      (JsPath \ "favorite").read[Int] and
      (JsPath \ "retweet").read[Int] and
      (JsPath \ "imageUrls").read[List[String]]
    )(Ranking.apply _)

    implicit val rankingWrites: Writes[Ranking] = (
      (JsPath \ "rank").write[Int] and
      (JsPath \ "name").write[String] and
      (JsPath \ "text").write[String] and
      (JsPath \ "favorite").write[Int] and
      (JsPath \ "retweet").write[Int] and
      (JsPath \ "imageUrls").write[List[String]]
    )(unlift(Ranking.unapply))

    val queryData = queryForm.bindFromRequest.get
    val redisClient = new RedisClient("localhost", 6379)
    redisClient.get(queryData.toString) match {
      case Some(r) => {
        Logger.debug("Hit cache. ttl: " + redisClient.ttl(queryData.toString))
        val json = Json.parse(r)
        val ranking = json.validate[List[Ranking]]
        Ok(views.html.index(queryForm.fill(queryData), ranking.asOpt))
      }
      case None =>
        val ranking = TwitterApi.ranking(
          queryData.since,
          queryData.keyword
        )
        redisClient.set(queryData.toString, Json.toJson(ranking).toString, false, Seconds(cacheSeconds))
        Ok(views.html.index(queryForm.fill(queryData), Some(ranking)))
    }
  }

  def keyword = Action { implicit req =>
    Ok(views.html.keyword(keywordForm))
  }

  def createKeyword = Action { implicit req =>
    val keywordData = keywordForm.bindFromRequest.get
    val redisClient = new RedisClient("localhost", 6379)
    redisClient.set("keyword", keywordData.keyword)
    Logger.debug("Create keyword: " + keywordData.keyword)
    Redirect(routes.Application.keyword)
  }
}