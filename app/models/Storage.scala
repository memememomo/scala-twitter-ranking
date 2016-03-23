package models

import java.util.Date

import com.redis.RedisClient
import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._

object Storage {
  val keywordCacheName = "keyword"
  val redisClient = new RedisClient("localhost", 6379)

  implicit val rankingReads: Reads[Ranking] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "screenName").read[String] and
    (JsPath \ "text").read[String] and
    (JsPath \ "favorite").read[Int] and
    (JsPath \ "retweet").read[Int] and
    (JsPath \ "created").read[Date] and
    (JsPath \ "imageUrls").read[List[String]]
  )(Ranking.apply _)

  implicit val rankingWrites: Writes[Ranking] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "screenName").write[String] and
    (JsPath \ "text").write[String] and
    (JsPath \ "favorite").write[Int] and
    (JsPath \ "retweet").write[Int] and
    (JsPath \ "created").write[Date] and
    (JsPath \ "imageUrls").write[List[String]]
  )(unlift(Ranking.unapply))

  def getKeyword = {
    redisClient.get[String](keywordCacheName)
  }

  def setKeyword(keyword: String) = {
    redisClient.set(keywordCacheName, keyword)
  }

  def getRanking(key: String) = {
    redisClient.get(key) match {
      case Some(r) => {
        Logger.debug("Hit cache.")
        val json = Json.parse(r)
        json.validate[List[Ranking]].asOpt
      }
      case _ => None
    }
  }

  def setRanking(key: String, ranking: List[Ranking]) = {
    redisClient.set(key, Json.toJson(ranking).toString)
  }
}
