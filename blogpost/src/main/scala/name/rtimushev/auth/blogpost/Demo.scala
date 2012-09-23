package name.rtimushev.auth.blogpost

import scala.collection.mutable

object Demo {

  trait Feature
  trait Permission extends Feature
  trait Role extends Feature

  trait Administrator extends Role

  trait ResourceActions {
    trait CanRead extends Permission
    trait CanWrite extends Permission
  }

  class NotAuthorized extends Exception

  class Token[+T <: Feature] {
    def and[S <: Feature](t: Token[S]) = new Token[T with S]
  }

  def grant[T <: Feature]: Token[T] = new Token[T]
  def forbid[T <: Feature]: Token[T] = throw new NotAuthorized

  case class ResourceId(id: String) extends ResourceActions
  case class Resource(content: String)

  class ResourceDao(storage: mutable.Map[ResourceId, Resource]) {
    def read(id: ResourceId)(implicit token: Token[id.CanRead]) = {
      storage.get(id)
    }
    def write(id: ResourceId, data: Option[Resource])(implicit token: Token[id.CanWrite]) {
      data match {
        case Some(v) => storage(id) = v
        case None => storage -= id
      }
    }
    def resources(implicit token: Token[Administrator]) = {
      storage.keys.toSet
    }
  }

  class SecurityContext(val user: Option[String])

  // we have a single administrator
  def isAdministrator(implicit ctx: SecurityContext): Token[Administrator] = {
    ctx.user match {
      case Some("administrator") => grant
      case _ => forbid
    }
  }

  // everyone can read
  def canRead(id: ResourceId)(implicit ctx: SecurityContext): Token[id.CanRead] = {
    grant
  }

  // only administrators and resource owners can write
  // (owner is a user with a name equal to the resource id)
  def canWrite(id: ResourceId)(implicit ctx: SecurityContext): Token[id.CanRead with id.CanWrite] = {
    ctx.user match {
      case Some("administrator") => grant
      case Some(user) if (user == id.id) => grant
      case _ => forbid
    }
  }

  // administrator has read and write permissions for all resources
  implicit def administratorCanEverything[R <: ResourceActions](implicit t: Token[Administrator]) = {
    new Token[R#CanRead with R#CanWrite]
  }

  class SimpleService(dao: ResourceDao) {

    def get(id: ResourceId)(implicit ctx: SecurityContext) = {
      implicit val token = canRead(id)
      dao.read(id).get.content
    }

    def set(id: ResourceId, content: String)(implicit ctx: SecurityContext) {
      implicit val token = canWrite(id)
      dao.write(id, Some(new Resource(content)))
    }

    def copy(src: ResourceId, dst: ResourceId)(implicit ctx: SecurityContext) {
      implicit val token = canRead(src) and canWrite(dst)
      dao.write(dst, dao.read(src))
    }

    def clear(implicit ctx: SecurityContext) {
      implicit val token = isAdministrator
      dao.resources foreach { dao.write(_, None) }
    }

  }

}
