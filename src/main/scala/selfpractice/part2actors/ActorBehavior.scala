package selfpractice.part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import selfpractice.part2actors.ActorBehavior.Mom.StartMom

object ActorBehavior extends App{

  object FussyKid{
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }
  class FussyKid extends Actor{
    import FussyKid._
    import Mom._
    override def receive: Receive = happyReceive

    /*
    handle state with become(recv,false) & unbecome() instead of become(recv) in which case we need to again call become(recv)
    1. Happy -> Default
    2. Sad
    3. Sad -> Sad (interim)
    4. Happy

     */
    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive,false)
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept
    }

    def sadReceive:Receive = {
      case Food(CHOCOLATE) => context.unbecome()
      case Food(VEGETABLE) => context.become(sadReceive,false)
      case Ask(_) => sender() ! KidReject
    }
  }

  object Mom{
    case class Food(food:String)
    val VEGETABLE="vegetable"
    val CHOCOLATE="chocolate"

    case class StartMom(kidRef: ActorRef)
    case class Ask(message: String) // do you want to play?
  }
  class Mom extends Actor{
    import FussyKid._
    import Mom._

    override def receive: Receive = {
      case StartMom(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Ask("Do you want to play?") //still sad as stack had 2 sads
        println("still sad as stack had 2 sads")
        kidRef ! Food(CHOCOLATE)
        kidRef ! Ask("Do you want to play?")
      case KidAccept => println(s"[$self] My kid is happy!!!")
      case KidReject => println(s"[$self] My kid is sad but healthy!!!")
    }
  }

  val system = ActorSystem("actorSystem")
  val fussyKid = system.actorOf(Props[FussyKid])
  val mom = system.actorOf(Props[Mom])

  mom ! StartMom(fussyKid)

}
