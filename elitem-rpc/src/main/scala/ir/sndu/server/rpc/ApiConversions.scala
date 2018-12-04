package ir.sndu.server.rpc

import ir.sndu.server.model.user.Sex
import ir.sndu.api.user.ApiSex

object ApiConversions {
  implicit def sexToApi(sex: Sex): ApiSex = ApiSex.fromValue(sex.toInt)

  implicit def apiToSex(sex: ApiSex): Sex = Sex.fromInt(sex.value)
}
