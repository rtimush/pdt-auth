package name.rtimushev.auth.model

import scala.collection.mutable
import name.rtimushev.auth.framework._
import name.rtimushev.auth.service._

class ResourceDao(storage: mutable.Map[ResourceId, Resource]) {

  def read(id: ResourceId)(implicit token: Token[id.CanRead]) = {
    storage.get(id)
  }

  def write(id: ResourceId, data: Option[Resource])(implicit token: Token[id.CanWrite]) {
    data match {
      case Some(value) => storage(id) = value
      case None => storage -= id
    }
  }

  def resources(implicit token: Token[Administrator]) = {
    storage.keys.toSet
  }

}
