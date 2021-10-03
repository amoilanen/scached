package io.github.antivanov.scached

import io.github.antivanov.scached.KeyCache.Fetch

import scala.concurrent.Future

class KeyCache[K, V](fetch: Fetch[K, V]) {

  def get(key: K): Future[V] = ???
}

object KeyCache {
  type Fetch[K, V] = K => Future[V]
}
