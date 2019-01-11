package im.ghasedak.server.utils

object StringUtils {

  def validName(name: String): Option[String] = {
    val trimmed = name.trim
    if (trimmed.isEmpty) None
    else if (trimmed.length > 255) None // for database varchar
    else Some(trimmed)
  }

}
