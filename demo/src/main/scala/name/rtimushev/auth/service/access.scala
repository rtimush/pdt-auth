package name.rtimushev.auth.service

import name.rtimushev.auth.framework._
import name.rtimushev.auth.model._

object RoleChecks {

  def ifAdministrator(implicit ctx: SecurityContext): TokenBuilder[Administrator] = ctx.user match {
    case Some("administrator") => grant
    case _ => forbid
  }

}

object ResourceAccessChecks {

  def ifCanRead(id: ResourceId)(implicit ctx: SecurityContext): TokenBuilder[id.CanRead] = {
    grant
  }

  def ifCanWrite(id: ResourceId)(implicit ctx: SecurityContext): TokenBuilder[id.CanRead with id.CanWrite] =
    ctx.user match {
      case Some("administrator") => grant
      case Some(user) if (user == id.id) => grant
      case _ => forbid
    }

  implicit def administratorCanEverything[R <: ResourceActions](implicit t: Token[Administrator]): Token[R#CanRead with R#CanWrite] = {
    t.asInstanceOf[Token[R#CanRead with R#CanWrite]]
  }

}
