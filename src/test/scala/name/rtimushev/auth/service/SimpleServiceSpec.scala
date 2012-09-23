package name.rtimushev.auth.service

import scala.collection.mutable
import org.scalatest.FreeSpec
import org.scalatest.matchers.ShouldMatchers
import name.rtimushev.auth.model._
import name.rtimushev.auth.framework._

class SimpleServiceSpec extends FreeSpec with ShouldMatchers {

  val resourceA = ResourceId("a")
  val resourceB = ResourceId("b")
  val storage = mutable.Map(resourceA -> Resource("value"))
  val dao = new ResourceDao(storage)
  val controller = new SimpleService(dao)

  "A resource controller" - {
    "for an ordinary user" - {
      implicit val ctx = new SecurityContext(Some("user"))
      "should allow reads" in {
        controller.getResource(resourceA) should equal("value")
      }
      "should forbid writes" in {
        intercept[NotAuthorized] { controller.setResource(resourceA, "new value") }
      }
      "should forbid copies" in {
        intercept[NotAuthorized] { controller.copyResource(resourceA, resourceB) }
      }
      "should forbid clears" in {
        intercept[NotAuthorized] { controller.clear }
      }
    }
    "for a resource owner" - {
      implicit val ctx = new SecurityContext(Some("b"))
      "should allow reads" in {
        controller.getResource(resourceA) should equal("value")
      }
      "should allow writes" in {
        controller.setResource(resourceB, "another value")
        controller.getResource(resourceB) should equal("another value")
      }
      "should allow copies" in {
        controller.copyResource(resourceA, resourceB)
        controller.getResource(resourceB) should equal("value")
      }
      "should forbid clears" in {
        intercept[NotAuthorized] { controller.clear }
      }
    }
    "for an administrator" - {
      implicit val ctx = new SecurityContext(Some("administrator"))
      "should allow reads" in {
        controller.getResource(resourceA) should equal("value")
      }
      "should allow writes" in {
        controller.setResource(resourceB, "another value")
        controller.getResource(resourceB) should equal("another value")
      }
      "should allow copies" in {
        controller.copyResource(resourceA, resourceB)
        controller.getResource(resourceB) should equal("value")
      }
      "should allow clears" in {
        controller.clear
        storage should be('empty)
      }
    }

  }

}
