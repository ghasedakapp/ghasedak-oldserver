package ir.sndu.server.utils

import com.typesafe.config.ConfigFactory
import ir.sndu.server.utils.number.PhoneNumberUtils._

import scala.util.Try

object CodeGen {

  private val config = ConfigFactory.load()

  private val testPhoneNumberEnable = config.getBoolean("module.auth.test-phone-number.enable")

  def genPhoneCode(phone: Long): String =
    if (isTestPhone(phone) && testPhoneNumberEnable) {
      val strPhone = phone.toString
      Try("1" + strPhone(4).toString * 2 + strPhone(5).toString * 2) getOrElse genCode()
    } else genCode()

  def genCode(): String = ThreadLocalSecureRandom.current().nextLong().toString.dropWhile(c ⇒ c == '0' || c == '-').take(5)

}