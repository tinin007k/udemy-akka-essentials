package selfpractice.part2actors

import akka.actor.{ActorRef, ActorSystem, Props, Terminated}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import selfpractice.part2actors.SupervisorStrategy.{Child, Supervisor}

class FaultHandlingDocSpec(_system: ActorSystem)
    extends TestKit(_system)
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  def this() =
    this(
      ActorSystem(
        "FaultHandlingDocSpec",
        ConfigFactory.parseString("""
      akka {
        loggers = ["akka.testkit.TestEventListener"]
        loglevel = "WARNING"
      }
      """)))

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A supervisor" must {
    "the child will be terminated" in {
      // code here
      val supervisor = system.actorOf(Props[Supervisor](), "supervisor")
      supervisor ! Props[Child]()
      val child = expectMsgType[ActorRef] // retrieve answer from TestKit’s testActor
      watch(child) // have testActor watch “child”
      child ! new IllegalArgumentException // break it
      expectMsgPF() { case Terminated(`child`) => () }
    }
  }

  "A exception" must{
    "escalate to the user guardian" in {
      val supervisor = system.actorOf(Props[Supervisor](), "supervisor")
      supervisor ! Props[Child]() // create new child
      val child2 = expectMsgType[ActorRef]
      watch(child2)
      child2 ! "get" // verify it is alive
      expectMsg(0)

      child2 ! new Exception("CRASH") // escalate failure
      expectMsgPF() {
        case t @ Terminated(`child2`) if t.existenceConfirmed => ()
      }
    }
  }

}