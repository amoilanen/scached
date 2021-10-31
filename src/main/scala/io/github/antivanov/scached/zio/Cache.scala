package io.github.antivanov.scached.zio

import io.github.antivanov.scached.Cache.Fetch
import izumi.reflect.Tag
import zio.stm.TMap
import zio.{Has, RIO, Task, ULayer, ZIO, ZLayer}

object Cache {

  type Cache[K, V] = Has[Cache.Service[K, V]]

  trait Service[K, V] {
    def get(key: K): Task[V]
  }

  class ServiceImpl[K, V](values: TMap[K, V], fetch: Fetch[K, V]) extends Service[K, V] {

    //TODO: Is it possible to avoid doing two-phase changes in this case? Better way to do two-phase changes?
    override def get(key: K): Task[V] = for {
      cachedValue <- values.get(key).commit
      value <- if (cachedValue.isEmpty)
        ZIO.fromFuture(_ => fetch(key))
      else
        ZIO.succeed(cachedValue.get)
      _ <- values.put(key, value).commit
    } yield value
  }

  def cacheOf[K: Tag, V: Tag](fetch: Fetch[K, V]): ULayer[Cache[K, V]] =
    ZLayer.fromZIO(
      for {
        values <- TMap.empty[K, V].commit
      } yield
        new ServiceImpl[K, V](values, fetch)
    )

  def get[K: Tag, V: Tag](key: K): RIO[Cache[K, V], V] =
    ZIO.accessZIO(_.get.get(key))
}
