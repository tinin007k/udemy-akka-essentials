package selfpractice.part2actors
import akka.actor.{Actor, Props}

import scala.concurrent.duration._
import scala.language.postfixOps

object SupervisorStrategy extends App {

  class Supervisor extends Actor {
    import akka.actor.OneForOneStrategy
    import akka.actor.SupervisorStrategy._
    import scala.concurrent.duration._

    override val supervisorStrategy: OneForOneStrategy =
      OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
        case _: ArithmeticException      => Resume
        case _: NullPointerException     => Restart
        case _: IllegalArgumentException => Stop
        case _: Exception                => Escalate
      }

    def receive: Receive = {
      case p: Props => sender() ! context.actorOf(p)
    }
  }

  class Child extends Actor {
    var state = 0
    def receive = {
      case ex: Exception => throw ex
      case x: Int        => state = x
      case "get"         => sender() ! state
    }
  }
}
