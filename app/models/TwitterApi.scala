package models

import java.util.Date

import twitter4j._
import twitter4j.conf._

import scala.collection.JavaConversions._
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

case class Ranking(
                    name: String,
                    screenName: String,
                    text: String,
                    favorite: Int,
                    retweet: Int,
                    created: Date,
                    imageUrls: List[String]
                  )

object TwitterApi {

 def config = {
    val config = ConfigFactory.load()
    val cb = new ConfigurationBuilder
    cb.setOAuthConsumerKey(config.getString("consumerKey"))
      .setOAuthConsumerSecret(config.getString("consumerSecret"))
      .setOAuthAccessToken(config.getString("accessToken"))
      .setOAuthAccessTokenSecret(config.getString("accessTokenSecret"))
    cb.build
  }

  def twitter = new TwitterFactory(config).getInstance()

  def search(since: DateTime, keyword: String): List[Status] = {
    val fmt = DateTimeFormat.forPattern("yyyy-MM-dd")
    val query = new Query()
    query.setSince(fmt.print(since))
    query.setUntil(fmt.print(since.plusDays(1)))
    query.setCount(100)
    query.setQuery(keyword + " -RT")
    search(query)
  }

  def search(query: Query): List[Status] = {
    val result = twitter.search(query)
    result.hasNext match {
      case true =>
        play.Logger.debug("Next result exists")
        result.getTweets.toList ++ search(result.nextQuery())
      case _ =>
        play.Logger.debug("Finish searching")
        result.getTweets.toList
    }
  }

  def ranking(since: DateTime, sort: String, keyword: String): List[Ranking] = {
    search(since, keyword)
      .filter(!_.isRetweet)
      .map{ status => Ranking(
        status.getUser.getName,
        status.getUser.getScreenName,
        status.getText,
        status.getFavoriteCount,
        status.getRetweetCount,
        status.getCreatedAt,
        status.getMediaEntities.map(s => s.getMediaURL).toList
      )}
  }

  def searchTarget(since: DateTime, keyword: String) = {
    val targetName = ConfigFactory.load().getString("targetScreenName")
    search(since, keyword)
      .filter(r => !r.isRetweet && r.getUser.getScreenName == targetName)
      .sortWith((a, b) => b.getCreatedAt.after(a.getCreatedAt)) match {
      case targets if targets.nonEmpty => Some(targets.head)
      case _ => None
    }
  }
}
