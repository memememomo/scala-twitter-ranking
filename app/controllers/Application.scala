package controllers

import org.joda.time.DateTime
import play.api._
import play.api.mvc._
import models.TwitterApi

object Application extends Controller {

  def index = Action {
    val ranking = TwitterApi.ranking(
      new DateTime(),
      ""
    )
    Ok(views.html.index("Your new application is ready."))
  }

}