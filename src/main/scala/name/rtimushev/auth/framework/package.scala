package name.rtimushev.auth

package object framework {

  def grant[T <: Feature] = new TokenBuilder[T](true)
  def forbid[T <: Feature] = new TokenBuilder[T](false)

  def check[T <: Feature](b: TokenBuilder[T]) = b.token

}
