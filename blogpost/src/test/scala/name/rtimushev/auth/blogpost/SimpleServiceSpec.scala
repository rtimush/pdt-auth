package name.rtimushev.auth.blogpost

import scala.collection.mutable
import org.scalatest.FreeSpec
import org.scalatest.matchers.ShouldMatchers

class SimpleServiceSpec extends FreeSpec with ShouldMatchers {

  import Demo._

  val resourceA = ResourceId("a")
  val resourceB = ResourceId("b")
  val storage = mutable.Map(resourceA -> Resource("value"))
  val dao = new ResourceDao(storage)
  val controller = new SimpleService(dao)

  "A resource controller from the blog post" - {
    "for an ordinary user" - {
      implicit val ctx = new SecurityContext(Some("user"))
      "should allow reads" in {
        controller.get(resourceA) should equal("value")
      }
      "should forbid writes" in {
        intercept[NotAuthorized] { controller.set(resourceA, "new value") }
      }
      "should forbid copies" in {
        intercept[NotAuthorized] { controller.copy(resourceA, resourceB) }
      }
      "should forbid clears" in {
        intercept[NotAuthorized] { controller.clear }
      }
    }
    "for a resource owner" - {
      implicit val ctx = new SecurityContext(Some("b"))
      "should allow reads" in {
        controller.get(resourceA) should equal("value")
      }
      "should allow writes" in {
        controller.set(resourceB, "another value")
        controller.get(resourceB) should equal("another value")
      }
      "should allow copies" in {
        controller.copy(resourceA, resourceB)
        controller.get(resourceB) should equal("value")
      }
      "should forbid clears" in {
        intercept[NotAuthorized] { controller.clear }
      }
    }
    "for an administrator" - {
      implicit val ctx = new SecurityContext(Some("administrator"))
      "should allow reads" in {
        controller.get(resourceA) should equal("value")
      }
      "should allow writes" in {
        controller.set(resourceB, "another value")
        controller.get(resourceB) should equal("another value")
      }
      "should allow copies" in {
        controller.copy(resourceA, resourceB)
        controller.get(resourceB) should equal("value")
      }
      "should allow clears" in {
        controller.clear
        storage should be('empty)
      }
    }

  }

}
