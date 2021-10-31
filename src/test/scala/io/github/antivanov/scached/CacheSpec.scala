package io.github.antivanov.scached

import io.github.antivanov.scached.Cache.Fetch
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import io.github.antivanov.scached.akka.{CacheImpl => AkkaCache}
import io.github.antivanov.scached.concurrent.{CacheImpl => ConcurrentCache}
import io.github.antivanov.scached.zio.{CacheImpl => ZIOCache}
import scala.concurrent.Future

class CacheSpec extends AnyFreeSpec with MockFactory with Matchers with ScalaFutures {

  case class CacheFixture[K, V](cacheFactory: Fetch[K, V] => Cache[K, V], cacheName: String)

  val fixtures = List(
    CacheFixture[String, Int](new AkkaCache(_), "Akka actors"),
    CacheFixture[String, Int](new ConcurrentCache(_), "ConcurrentHashMap"),
    CacheFixture[String, Int](new ZIOCache(_), "ZIO STM-based cache")
  )

  fixtures.foreach({
    case CacheFixture(cacheFactory, cacheName) => {
      s"$cacheName based cache" - {
        val key = "key1"
        val value = 42

        "should evaluate the key on the first access" in {
          val fetch = stubFunction[String, Future[Int]]
          fetch.when(key).returns(Future.successful(value))

          val cache = cacheFactory(fetch)
          cache.get(key).futureValue shouldEqual value
          fetch.verify(key).once()
        }

        "should not evaluate value for same key twice" in {
          val fetch = stubFunction[String, Future[Int]]
          fetch.when(key).returns(Future.successful(value))

          val cache = cacheFactory(fetch)
          cache.get(key).futureValue shouldEqual value

          //TODO: Use latches and more synchronization? How reliable is this?
          Thread.sleep(100)
          cache.get(key).futureValue shouldEqual value
          fetch.verify(key).once()
        }

        //TODO: Multiple keys
        //TODO: Many concurrent calls to cache happening at the same time: for same key, for different keys
        //TODO: Use an ExecutionContext with multiple threads: more realistic case

        //TODO: Add a benchmark for comparing caches?
      }
    }
  })
}
