package io.github.antivanov.scached

import java.util.UUID

import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.actor.typed.scaladsl.Behaviors
import io.github.antivanov.scached.Cache.Fetch

import scala.concurrent.{ExecutionContext, Future}
import akka.actor.typed.scaladsl.AskPattern._
import akka.pattern.StatusReply
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object CacheActor {
  sealed trait CacheAction
  case class GetKeyValue[K, V](key: K, replyTo: ActorRef[StatusReply[V]]) extends CacheAction
  case class ReplyWithValueAndUpdateCache[K, V](key: K, value: V, replyTo: ActorRef[StatusReply[V]]) extends CacheAction
  case class ReplyWithError[K, V](key: K, error: Throwable, replyTo: ActorRef[StatusReply[V]]) extends CacheAction

  def apply[K, V](cache: Map[K, V], fetch: Fetch[K, V]): Behaviors.Receive[CacheAction] = Behaviors.receive { (context, message) =>
    message match {
      case GetKeyValue(key: K, replyTo: ActorRef[StatusReply[V]]) =>
        if (cache.contains(key))
          replyTo ! StatusReply.Success(cache(key))
        else
          context.pipeToSelf(fetch(key)) {
            case Success(value) =>
              ReplyWithValueAndUpdateCache(key, value, replyTo)
            case Failure(error) =>
              ReplyWithError(key, error, replyTo)
          }
        Behaviors.same
      case ReplyWithValueAndUpdateCache(key: K, value: V, replyTo: ActorRef[StatusReply[V]]) =>
        replyTo ! StatusReply.Success(value)
        CacheActor(cache.updated(key, value), fetch)
      case ReplyWithError(_, error: Throwable, replyTo: ActorRef[StatusReply[V]]) =>
        replyTo ! StatusReply.error(error)
        Behaviors.same
    }
  }
}

class Cache[K, V](fetch: Fetch[K, V]) {

  import CacheActor._
  val actorSystem = ActorSystem(CacheActor(Map(), fetch), UUID.randomUUID().toString)
  implicit val timeout = Timeout(5.seconds)
  implicit val scheduler: Scheduler = actorSystem.scheduler

  def get(key: K): Future[V] =
    actorSystem.askWithStatus((ref: ActorRef[StatusReply[V]]) => GetKeyValue(key, ref))
}

object Cache {
  type Fetch[K, V] = K => Future[V]
}
