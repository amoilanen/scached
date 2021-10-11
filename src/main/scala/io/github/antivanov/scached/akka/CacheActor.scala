package io.github.antivanov.scached.akka

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import io.github.antivanov.scached.Cache.Fetch
import akka.pattern.StatusReply
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
