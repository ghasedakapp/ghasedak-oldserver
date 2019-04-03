package im.ghasedak.server.model

import java.time.{ Instant, LocalDateTime, ZoneOffset }

import com.google.protobuf.timestamp.Timestamp

object TimeConversions {
  implicit def localDateToTimestamp(time: LocalDateTime): Timestamp =
    Timestamp(time.toEpochSecond(ZoneOffset.UTC), time.getNano)

  implicit def localDateToTimestampOpt(timeOpt: Option[LocalDateTime]): Option[Timestamp] =
    timeOpt map localDateToTimestamp

  implicit def timestampToLocalDate(timestamp: Timestamp): LocalDateTime =
    LocalDateTime.ofEpochSecond(timestamp.seconds, timestamp.nanos, ZoneOffset.UTC)

  implicit def timestampToLocalDateOpt(timestampOpt: Option[Timestamp]): Option[LocalDateTime] =
    timestampOpt map timestampToLocalDate

  implicit def instantToTimestamp(instant: Instant): Timestamp =
    Timestamp(instant.getEpochSecond, instant.getNano)

  implicit def instantToTimestampOpt(instantOpt: Option[Instant]): Option[Timestamp] =
    instantOpt map instantToTimestamp

  implicit def timestampToInstant(timestamp: Timestamp): Instant =
    Instant.ofEpochSecond(timestamp.seconds, timestamp.nanos)

  implicit def timestampToInstantOpt(timestampOpt: Option[Timestamp]): Option[Instant] =
    timestampOpt map timestampToInstant

}
