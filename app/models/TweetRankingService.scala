package models

import controllers.{QueryData, KeywordData}
import org.joda.time.DateTime


object TweetRankingService {
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

}
