package io.github.antivanov.scached.zio

import io.github.antivanov.scached.Cache
import io.github.antivanov.scached.Cache.Fetch
import io.github.antivanov.scached.zio.{Cache => ZIOCache}
import izumi.reflect.Tag
import zio.Runtime

import scala.concurrent.{ExecutionContext, Future}

class CacheImpl[K: Tag, V: Tag](fetch: Fetch[K, V])(implicit ec: ExecutionContext) extends Cache[K, V] {

  val runtime: Runtime[zio.ZEnv] = Runtime.default
  val cache = ZIOCache.cacheOf[K, V](fetch)

  override def get(key: K): Future[V] =
    runtime.unsafeRunToFuture(ZIOCache.get[K, V](key).provideLayer(cache))
}
