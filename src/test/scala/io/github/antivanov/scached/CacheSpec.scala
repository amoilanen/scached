package io.github.antivanov.scached

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class CacheSpec extends AnyFreeSpec with MockFactory with Matchers with ScalaFutures {

  "Cache" - {
    val key = "key1"
    val value = 42

    "should evaluate the key on the first access" in {
      val fetch = stubFunction[String, Future[Int]]
      fetch.when(key).returns(Future.successful(value))

      val cache = new Cache[String, Int](fetch)
      cache.get(key).futureValue shouldEqual value
      fetch.verify(key).once()
    }

    "should not evaluate value for same key twice" in {
      val fetch = stubFunction[String, Future[Int]]
      fetch.when(key).returns(Future.successful(value))

      val cache = new Cache[String, Int](fetch)
      cache.get(key).futureValue shouldEqual value

      //TODO: Use latches and more synchronization? How reliable is this?
      Thread.sleep(100)
      cache.get(key).futureValue shouldEqual value
      fetch.verify(key).once()
    }

    //TODO: Multiple keys
  }
}
