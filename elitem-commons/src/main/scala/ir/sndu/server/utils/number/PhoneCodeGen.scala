package ir.sndu.server.utils.number

import ir.sndu.server.utils.ThreadLocalSecureRandom
import ir.sndu.server.utils.number.PhoneNumberUtils._

import scala.util.Try

object PhoneCodeGen {

  def genPhoneCode(phone: Long): String =
    if (isTestPhone(phone)) {
      val strPhone = phone.toString
      Try(strPhone(4).toString * 4) getOrElse strPhone
    } else genCode()

  def genCode() = ThreadLocalSecureRandom.current().nextLong().toString.dropWhile(c ⇒ c == '0' || c == '-').take(5)

}