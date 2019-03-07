package im.ghasedak.server

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.AbstractBehavior

trait Processor[T] extends AbstractBehavior[T] {

  type Receive = PartialFunction[T, Behavior[T]]

  def onReceive: Receive

  override def onMessage(msg: T): Behavior[T] = onReceive(msg)

}
