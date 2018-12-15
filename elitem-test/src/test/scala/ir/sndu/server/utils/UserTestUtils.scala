package ir.sndu.server.utils

trait UserTestUtils {

  def generatePhoneNumber(): Long = {
    75550000000L + scala.util.Random.nextInt(999999)
  }

}
