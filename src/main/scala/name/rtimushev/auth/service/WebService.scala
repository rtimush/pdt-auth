package name.rtimushev.auth.service

import scala.collection.JavaConverters._
import unfiltered._
import unfiltered.request._
import unfiltered.response._
import name.rtimushev.auth.model._
import name.rtimushev.auth.framework._
import ResourceAccessChecks._
import java.util.concurrent.ConcurrentHashMap

class WebService(dao: ResourceDao) {

  implicit def eitherToResponse[S, R <: ResponseFunction[S]](e: Either[NotAuthorized, R]): ResponseFunction[S] = e match {
    case Right(r) => r
    case Left(_) => Unauthorized ~> WWWAuthenticate( """Basic realm="/"""")
  }

  def authenticate[T](req: HttpRequest[T]): SecurityContext = req match {
    case BasicAuth(user, password) if password == user => new SecurityContext(Some(user))
    case _ => new SecurityContext(None)
  }

  val intent = Cycle.Intent[Any, Any] {
    case req@Path(Seg(key :: Nil)) =>
      implicit val ctx = authenticate(req)
      val id = ResourceId(key)
      req match {
        case GET(_) => ifCanRead(id) apply {
          implicit token =>
            dao.read(id) match {
              case Some(r) => Ok ~> ResponseString(r.content)
              case None => NotFound
            }
        }
        case PUT(_) => ifCanWrite(id) apply {
          implicit token =>
            dao.write(id, Some(Resource(Body.string(req)))); Ok
        }
        case DELETE(_) => ifCanWrite(id) apply {
          implicit token =>
            dao.write(id, None); NoContent
        }
        case _ => Pass
      }
  }

}

object WebService extends App {

  val dao = new ResourceDao(new ConcurrentHashMap[ResourceId, Resource]().asScala)
  val server = new WebService(dao)

  netty.Http(8080).plan(netty.cycle.Planify(server.intent)).run()

}
