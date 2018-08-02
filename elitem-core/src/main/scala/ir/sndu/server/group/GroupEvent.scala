package ir.sndu.server.group

import java.time.Instant

import ir.sndu.server.cqrs.TaggedEvent

trait GroupEvent extends TaggedEvent {
  val ts: Instant
  override def tags: Set[String] = Set("group")
}
