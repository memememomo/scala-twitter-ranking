package models

import twitter4j._
import twitter4j.conf._
import scala.collection.JavaConversions._
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat


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
    query.setQuery(keyword)
    search(query)
  }

  def search(query: Query): List[Status] = {
    val result = twitter.search(query)
    result.hasNext match {
      case true => result.getTweets.toList ++ search(result.nextQuery())
      case _ => result.getTweets.toList
    }
  }

  def ranking(since: DateTime, keyword: String): List[Status] = {
    search(since, keyword)
      .filter(!_.isRetweet)
      .sortWith(_.getFavoriteCount > _.getFavoriteCount)
  }
}
