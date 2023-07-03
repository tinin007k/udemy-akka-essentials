package selfpractice.part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import selfpractice.part2actors.ActorCapabilities.Person.LongLivePerson

object ActorCapabilities extends App{

  //BankAccount
  object Bank{
    case class Credit(amount:Int)
    case class Debit(amount:Int)
    case object Statement
    case class TransactionSuccess(message: String)
    case class TransactionFailure(message: String)
  }
  class Bank extends Actor {
    import Bank._
    var balance = 0
    override def receive: Receive = {
      case Credit(amount) => if(amount < 0) sender() ! TransactionFailure(s"Invalid credit amount: $amount") else {
        balance += amount
        sender() ! TransactionSuccess(s"Amount $amount successfully deposited to account")
      }
      case Debit(amount) => if(amount>balance)
        sender() ! TransactionFailure("Account balance cannot be < 0")
      else if(amount < 0) sender() ! TransactionFailure("Invalid debit amount: $amount")
      else {
        balance -= amount
        sender() ! TransactionSuccess(s"Amount $amount successfully debited from account")
      }
      case Statement => println(s"Balance is $balance")
    }
  }

  object Person{
    case class LongLivePerson(bankActor:ActorRef)
  }
  class Person extends Actor{
    import Person._
    import Bank._

    override def receive: Receive = {
      case LongLivePerson(bankActor) =>
        bankActor ! Credit(-11)
        bankActor ! Credit(1000)
        bankActor ! Debit(1100)
        bankActor ! Credit(10000)
        bankActor ! Debit(1100)
        bankActor ! Statement
      case TransactionSuccess(mesg) => println(s"message from [$sender()]: $mesg")
      case TransactionFailure(mesg) => println(s"message from [$sender()]: $mesg")
    }
  }

  val system = ActorSystem("actorSystem")
  val bankActor = system.actorOf(Props[Bank],"bank")
  val personActor = system.actorOf(Props[Person],"person")

  personActor ! LongLivePerson(bankActor)

}
