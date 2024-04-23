package io.agora.rtmsyncmanager.model

import com.google.gson.annotations.SerializedName
import io.agora.rtmsyncmanager.model.AUIGiftEntity
import java.io.Serializable

data class AUIGiftTabEntity constructor(
    // Description 对应哪个tab index
    @SerializedName("tabId") var tabId:Int=0,
    // Description 显示名称
    @SerializedName("displayName") var displayName: String?,
    // Description tab下礼物数据
    @SerializedName("gifts") var gifts: List<AUIGiftEntity?>
): Serializable
