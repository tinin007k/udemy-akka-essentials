package selfpractice.part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChangingActorBehavior.{AggregateVotes, Citizen, Vote, VoteAggregator, VoteStatusReply, VoteStatusRequest, alice, bob, charlie, daniel, system}
import selfpractice.part2actors.ActorBehavior.system

object ActorBehaviorPractice extends App{

  val system = ActorSystem("actorSystem")
  // DOMAIN of the counter
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

 /* class Counter extends Actor {
    import Counter._

    var count = 0

    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => println(s"[counter] My current count is $count")
    }
  }

  import Counter._
  val counter = system.actorOf(Props[Counter], "myCounter")

  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 3).foreach(_ => counter ! Decrement)
  counter ! Print*/


  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteReply(option: Option[String])
  class Citizen extends Actor{
    override def receive: Receive = {
      case Vote(candidate) => context.become(candidateVote(candidate))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }

    def candidateVote(candidate:String):Receive = {
      case VoteStatusRequest =>
        println(s" $self has voted for $candidate.[candidateVote] handling request after vote")
        sender() ! VoteReply(Some(candidate))
    }
  }

  case class AggregateVotes(citizens:Set[ActorRef])
  class VoteAggregator extends Actor{
    override def receive: Receive = {
      case AggregateVotes(citizens) =>
      citizens.foreach( x => x ! VoteStatusRequest)
      context.become(handleVoteCount(citizens,Map()))
    }

    def handleVoteCount(stillWaiting:Set[ActorRef],voteMap:Map[String,Int]):Receive = {
      case VoteReply(None) =>
        sender() ! VoteStatusRequest
      case VoteReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val voteCountMap = voteMap + (candidate -> (voteMap.getOrElse(candidate,0)+1))
        if(newStillWaiting.isEmpty){
          println(s"[VoteResult] is: $voteCountMap")
        } else {
          println(s"still waiting for reply from [$newStillWaiting]")
          context.become(handleVoteCount(newStillWaiting,voteCountMap))
        }
    }
  }

  val alice = system.actorOf(Props[Citizen],"alice")
  val bob = system.actorOf(Props[Citizen],"bob")
  val charlie = system.actorOf(Props[Citizen],"charlie")
  val daniel = system.actorOf(Props[Citizen],"daniel")

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = system.actorOf(Props[VoteAggregator],"VoteAggregator")
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))

}
