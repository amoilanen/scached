package io.github.antivanov.scached

import io.github.antivanov.scached.Cache.Fetch

import scala.concurrent.Future

class Cache[K, V](fetch: Fetch[K, V]) {

  def get(key: K): Future[V] = ???
}

object Cache {
  type Fetch[K, V] = K => Future[V]
}
