package ir.sndu.server.utils

object StringUtils {

  def validName(name: String): Option[String] = {
    val trimmed = name.trim
    if (trimmed.isEmpty) None
    else Some(trimmed)
  }

}
