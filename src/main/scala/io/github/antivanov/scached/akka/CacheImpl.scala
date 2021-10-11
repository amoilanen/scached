package io.github.antivanov.scached.akka

import java.util.UUID

import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import io.github.antivanov.scached.Cache.Fetch
import akka.actor.typed.scaladsl.AskPattern._
import akka.pattern.StatusReply
import akka.util.Timeout
import io.github.antivanov.scached.Cache

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class CacheImpl[K, V](fetch: Fetch[K, V]) extends Cache[K, V] {

  import CacheActor._
  val actorSystem = ActorSystem(CacheActor(Map(), fetch), UUID.randomUUID().toString)
  implicit val timeout = Timeout(5.seconds)
  implicit val scheduler: Scheduler = actorSystem.scheduler

  def get(key: K): Future[V] =
    actorSystem.askWithStatus((ref: ActorRef[StatusReply[V]]) => GetKeyValue(key, ref))
}
