package selfpractice.part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorPractice extends  App{

  object WordCounterMaster{
    case class Initialize(nChildren:Int)
    case class WordCountTask(taskId:Int,text:String)
    case class WordCountReply(taskId:Int,count:Int)
  }

  class WordCounterMaster extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(nChildren) =>
        val childRefs = for (x <- 1 to nChildren) yield context.actorOf(Props[WordCounterWorker], s"worker-${x}")
        context.become(countActor(childRefs,0,0,Map()))
    }

    /**
      * the params do not matter as params are handled for the first time call when the receive invokes the custom handler.
      * next time onwards its like recursive call while handling the return case
      * @param value
      * @return Unit
      */
    def countActor(childWorker:Seq[ActorRef],currentChildIndex:Int,currentTaskId:Int,requestMap: Map[Int, ActorRef]):Receive = {
      case text:String =>
        println(s"[master] received the count for text: $text, assigning it to the child worker")
        val newCurrentChildIndex=(currentChildIndex+1)%childWorker.size
        val newCurrentTaskId=currentTaskId+1
        val originalSender=sender()
        val newRequestMap=requestMap+(newCurrentTaskId -> originalSender)
        val wordCountTask=WordCountTask(newCurrentTaskId,text)
        childWorker(currentChildIndex) ! wordCountTask
        context.become(countActor(childWorker,newCurrentChildIndex,newCurrentTaskId,newRequestMap))
      case WordCountReply(taskId,count) =>
        val originalSender=requestMap(taskId)
        println(s"[master] returning count: $count for taskId: $taskId")
        originalSender ! count
        val newRequestMap=requestMap - taskId
        context.become(countActor(childWorker,currentChildIndex,currentTaskId,newRequestMap))
    }
  }



  class WordCounterWorker extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(id, text) =>
        println(s"${self.path} I have received task $id with $text")
        sender() ! WordCountReply(id, text.split(" ").length)
    }
  }

  class TestActor extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3)
        val texts = List("I love Akka", "Scala is super dope", "yes", "me too")
        texts.foreach(text => master ! text)
      case count: Int =>
        println(s"[test actor] I received a reply: $count")
    }
  }

  val system = ActorSystem("roundRobinWordCountExercise")
  val testActor = system.actorOf(Props[TestActor], "testActor")
  testActor ! "go"

}
