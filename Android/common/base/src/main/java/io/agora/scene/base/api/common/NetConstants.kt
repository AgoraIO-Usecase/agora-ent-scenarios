package io.agora.scene.base.api.common

//常量
object NetConstants {
  const val INTEGER = 1
  const val LONG = 2
  const val BOOLEAN = 3
  const val FLOAT = 4
  const val STRING = 5
  const val STRING_SET = 6
  const val BOOLEAN_TRUE = 7

  const val HEADER_PROJECT_NAME = "appProject"
  const val HEADER_DEVICE_Id = "deviceId"
  const val HEADER_APP_OS = "appOs"
  const val HEADER_APP_VER = "appVer"
  const val HEADER_VERSION_CODE = "versionCode"
  const val HEADER_VERSION_NAME = "versionName"
  const val TIME_STAMP = "timeStamp"
  const val SIGNATURE_METHOD = "signatureMethod"
  const val REQUEST_ID = "requestId"
  const val NONCE_STR = "nonceStr"
  const val ACCESS_TOKEN = "x-access-token"
  const val SIGNATURE = "signature"
  const val AUTHORIZATION = "Authorization"

  const val NOT_UPLOAD = 9990//未上传
  const val UPLOAD_FAILE = 9991//上传失败
  const val UPLOAD_SUCESS = 9992 //上传成功

  object NetworkType {
    const val NO_NETWORK: Int = 0
    const val MOBILE_NETWORK: Int = 1
    const val WIFI_NETWORK: Int = 2
  }

}
