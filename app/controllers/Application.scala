package controllers

import play.api._
import play.api.mvc._
import models.Hosts
import play.api.libs.json.JsValue
import models.Statistics
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.Enumerator
import play.api.libs.concurrent.Promise
import scala.concurrent.duration.DurationInt
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.EventSource
import scala.util.Success

object Application extends Controller {  
  def index = Action {
    Ok( views.html.dashboard( "Dashboard", Hosts.hosts() ) )
  }
  
  def host( id: String ) = Action { implicit request =>
    Hosts.hosts.find( _.id == id ) match {
      case Some( host ) => Ok( views.html.host( host ) )
      case None => NoContent
    }    
  }
  
  def stats( id: String ) = Action { request =>
    Hosts.hosts.find( _.id == id ) match {
      case Some( host ) =>
        Async { 
          Statistics.attach( host ).map { enumerator =>      
        	Ok.stream( enumerator &> EventSource() ).as( "text/event-stream")
          }
        }
      case None => NoContent		
    }
  }
}