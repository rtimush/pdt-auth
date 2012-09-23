package name.rtimushev.auth.framework

trait Feature
trait Role extends Feature
trait Permission extends Feature

class Token[+T <: Feature] private[framework] ()

class TokenBuilder[T <: Feature](val granted: Boolean) {
  def and[S <: Feature](a: TokenBuilder[S]) = new TokenBuilder[T with S](granted && a.granted)
  def token = if (granted) new Token[T] else throw new NotAuthorized
  def apply[R](block: Token[T] => R): Either[NotAuthorized, R] = {
    if (granted) Right(block(new Token[T]))
    else Left(new NotAuthorized)
  }
}

class NotAuthorized extends Exception


