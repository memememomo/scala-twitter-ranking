package controllers

import org.joda.time.DateTime
import play.api._
import play.api.mvc._
import models.TwitterApi
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

case class QueryData(keyword: String, since: DateTime)

object Application extends Controller {

  val queryForm = Form(
    mapping(
      "keyword" -> text,
      "since" -> jodaDate
    )(QueryData.apply)(QueryData.unapply)
  )

  def index = Action { implicit req =>
    Ok(views.html.index(queryForm, None))
  }

  def search = Action { implicit req =>
    val queryData = queryForm.bindFromRequest.get
    val ranking = TwitterApi.ranking(
      queryData.since,
      queryData.keyword
    )
    Ok(views.html.index(queryForm, Some(ranking)))
  }
}