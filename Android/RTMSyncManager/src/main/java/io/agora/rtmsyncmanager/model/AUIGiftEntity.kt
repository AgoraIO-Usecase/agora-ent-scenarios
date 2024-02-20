package io.agora.rtmsyncmanager.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AUIGiftEntity constructor(
    @SerializedName("giftId") var giftId:String?="",
    @SerializedName("giftName")var giftName:String?="",
    @SerializedName("giftPrice")var giftPrice:String?="",
    @SerializedName("giftIcon")var giftIcon:String?="",
    // Description 开发者可以上传服务器一个匹配礼物id的特效  特效名称为礼物的id
    // sdk会进入房间时拉取礼物资源并下载对应礼物id的特效，如果收到的礼物这个值为true
    // 则会找到对应的特效全屏播放加广播，礼物资源以及特效资源下载服务端可做一个web页面供用户使用，
    // 每个app启动后加载场景之前预先去下载礼物资源缓存到磁盘供UIKit取用
    @SerializedName("giftEffect")var giftEffect:String?="",
    @SerializedName("effectMD5")var effectMD5:String?="",
    @SerializedName("sendUser")var sendUser: AUIUserThumbnailInfo? = null,
    var selected:Boolean=false,
    var giftCount:Int = 1,
): Serializable

