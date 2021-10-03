package io.github.antivanov.scached

import java.util.concurrent.ConcurrentHashMap

import io.github.antivanov.scached.Cache.Fetch

import scala.jdk.CollectionConverters._
import scala.collection.concurrent.Map
import scala.concurrent.{ExecutionContext, Future}

class Cache[K, V](fetch: Fetch[K, V])(implicit ec: ExecutionContext) {

  val map: Map[K, V] = new ConcurrentHashMap[K, V]().asScala

  def get(key: K): Future[V] =
    map.get(key) match {
      case None =>
        fetch(key).map(value => {
          //TODO: What if there are concurrent modifications/many threads accessing the value?
          map.putIfAbsent(key, value)
          value
        })
      case Some(value) =>
        Future.successful(value)
    }
}

object Cache {
  type Fetch[K, V] = K => Future[V]
}
