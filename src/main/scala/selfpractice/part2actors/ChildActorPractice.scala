package selfpractice.part2actors

import akka.actor.{Actor, Props}
import selfpractice.part2actors.ChildActorPractice.WordCountWorker.Task

object ChildActorPractice extends  App{

  object WordCounterMaster{
    case class Initialize(nChildren:Int)
    case class WordCountTask(text:String)
    case class WordCountReply(count:Int)
  }

  class WordCounterMaster extends Actor{
    import WordCounterMaster._
    var nWorker=0
    override def receive: Receive = {
      case Initialize(nChildren) =>
        nWorker=nChildren
        for(x <- 0 to nChildren) {
          context.actorOf(Props[WordCountWorker], s"worker-${x}")
        }
      case WordCountTask(text) =>
        val sentences=text.split("\\n")
        for(x <- 0 until sentences.size){
          context.actorSelection(s"/user/master/worker-${sentences.size%nWorker}") ! Task(sentences(x))
        }
      case WordCountReply(count) =>
        }
    }

  object WordCountWorker{
    case class Task(text:String)
  }
  class WordCountWorker extends Actor{
    override def receive: Receive = ???
  }


}
