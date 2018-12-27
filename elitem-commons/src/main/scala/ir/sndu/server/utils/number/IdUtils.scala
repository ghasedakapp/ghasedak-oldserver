package ir.sndu.server.utils.number

import ir.sndu.server.utils.ThreadLocalSecureRandom

object IdUtils {
  def nextIntId(): Int = nextIntId(ThreadLocalSecureRandom.current())

  def nextIntId(rnd: ThreadLocalSecureRandom): Int = {
    val min = 1000
    min + rnd.nextInt(Int.MaxValue - min + 1)
  }

  def nextLongId(): Long = ThreadLocalSecureRandom.current().nextLong()

  def nextAuthId(): Long = nextAuthId(ThreadLocalSecureRandom.current())

  def nextAuthId(rng: ThreadLocalSecureRandom): Long = {
    val candidate = rng.nextLong()
    if (candidate == 0L) nextAuthId(rng) else candidate
  }
}
