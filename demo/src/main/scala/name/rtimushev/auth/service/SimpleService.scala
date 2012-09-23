package name.rtimushev.auth.service

import name.rtimushev.auth.framework._
import name.rtimushev.auth.model._
import ResourceAccessChecks._
import RoleChecks._

class SimpleService(dao: ResourceDao) {

  def getResource(id: ResourceId)(implicit ctx: SecurityContext) = {
    implicit val token = check(ifCanRead(id))
    dao.read(id).get.content
  }

  def setResource(id: ResourceId, content: String)(implicit ctx: SecurityContext) {
    implicit val token = check(ifCanWrite(id))
    dao.write(id, Some(new Resource(content)))
  }

  def copyResource(source: ResourceId, destination: ResourceId)(implicit ctx: SecurityContext) {
    implicit val token = check(ifCanRead(source) and ifCanWrite(destination))
    dao.write(destination, dao.read(source))
  }

  def clear(implicit ctx: SecurityContext) {
    implicit val token = check(ifAdministrator)
    dao.resources foreach { dao.write(_, None) }
  }

}
