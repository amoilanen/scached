package io.github.antivanov.scached.concurrent

import scala.jdk.CollectionConverters._
import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent.Map

import io.github.antivanov.scached.Cache
import io.github.antivanov.scached.Cache.Fetch

import scala.concurrent.{ExecutionContext, Future}

class CacheImpl[K, V](fetch: Fetch[K, V])(implicit ec: ExecutionContext) extends Cache[K, V] {

  val values: Map[K, V] = new ConcurrentHashMap[K, V]().asScala

  override def get(key: K): Future[V] =
    values.get(key) match {
      case None =>
        fetch(key).map(value => {
          values.putIfAbsent(key, value)
          value
        })
      case Some(value) =>
        Future.successful(value)
    }
}
