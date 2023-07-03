package selfpractice.part2actors

import akka.actor.{Actor, ActorRef, Props}
import selfpractice.part2actors.ChildActorPractice.WordCountWorker.Task

object ChildActorPractice extends  App{

  object WordCounterMaster{
    case class Initialize(nChildren:Int)
    case class WordCountTask(text:String)
    case class WordCountReply(count:Int)
  }

  class WordCounterMaster extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(nChildren) =>
        val childRefs = for (x <- 1 to nChildren) yield context.actorOf(Props[WordCountWorker], s"worker-${x}")
        context.become(countActor(childRefs))
    }

    def countActor(value: IndexedSeq[ActorRef]):Receive = {
      case text:String =>
    }
  }



  object WordCountWorker{
    case class Task(text:String)
  }
  class WordCountWorker extends Actor{
    override def receive: Receive = ???
  }


}
