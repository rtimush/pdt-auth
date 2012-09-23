package name.rtimushev.auth.framework

trait ResourceActions {
  trait CanRead extends Permission
  trait CanWrite extends Permission
}
