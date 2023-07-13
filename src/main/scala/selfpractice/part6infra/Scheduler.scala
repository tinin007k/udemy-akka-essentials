package selfpractice.part6infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, PoisonPill, Props}

import scala.concurrent.duration.{DurationDouble, DurationInt}
import scala.language.postfixOps

object Scheduler extends App{

  class SelfClosingActor extends Actor with ActorLogging{
    override def receive: Receive = {
      case "stop" =>
        log.info("Got stop so taking the poison pill.")
        self ! PoisonPill
      case message =>
        log.info(s"Got message: $message, timer reset to 1 second")
        val routine = context.system.scheduler.scheduleOnce(1 second){
        self ! "stop"
      }(context.system.dispatcher)
        context.become(handleAliveMessage(routine))
    }

    def handleAliveMessage(routine: Cancellable):Receive = {
      case "stop" =>
        log.info("Got stop so taking the poison pill.")
        self ! PoisonPill
      case message =>
        routine.cancel()
        log.info(s"Got message: $message, timer reset to 1 second")
        val newRoutine=context.system.scheduler.scheduleOnce(1 second){
          self ! "stop"
        }(context.system.dispatcher)
        context.become(handleAliveMessage(newRoutine))
    }
  }

  val system = ActorSystem("ActorSystem");
  val selfCloseActor = system.actorOf(Props[SelfClosingActor],"actor1")
  import system.dispatcher


  selfCloseActor ! "first message"

  system.scheduler.scheduleOnce(.5 second){
    selfCloseActor ! "some message"
  }

}
