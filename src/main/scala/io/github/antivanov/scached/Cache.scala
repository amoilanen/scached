package io.github.antivanov.scached

import scala.concurrent.Future

trait Cache[K, V] {

  def get(key: K): Future[V]
}

object Cache {
  type Fetch[K, V] = K => Future[V]
}
