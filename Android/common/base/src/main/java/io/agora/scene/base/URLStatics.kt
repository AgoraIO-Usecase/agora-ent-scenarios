package io.agora.scene.base

object URLStatics {
    const val userAgreementURL = "https://fullapp.oss-cn-beijing.aliyuncs.com/scenarios/service.html"
    const val privacyAgreementURL = "https://fullapp.oss-cn-beijing.aliyuncs.com/scenarios/privacy.html"
    const val thirdDataSharingURL = "https://fullapp.oss-cn-beijing.aliyuncs.com/scenarios/libraries.html"

    @JvmStatic
    val collectionChecklistURL: String
        get() {
            return if (ServerConfig.envRelease) {
                "https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/pages/manifest/index.html"
            } else {
                "https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/pages/manifest-dev/index.html"
            }
        }

    @JvmStatic
    val findBannerURL: String
        get() {
            return if (ServerConfig.envRelease) {
                "https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/pages/discover/index.html"
            } else {
                "https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/pages/discover-dev/index.html"
            }
        }

    const val findKtvSearchURL = "https://www.shengwang.cn/ktv_feedback/"
    const val findScenarioKtvURL = "https://www.shengwang.cn/solution/online-karaoke/"
    const val findDocKtvURL = "https://doc.shengwang.cn/doc/online-ktv/android/ktv-scenario/landing-page"
    const val findScenarioChatroomURL = "https://www.shengwang.cn/solution/voicechat/"
    const val findDocChatroomURL = "https://doc.shengwang.cn/doc/chatroom/android/sdk/landing-page"
    const val findScenarioLiveURL = "https://www.shengwang.cn/solution/hd-video/"
    const val findDocLiveURL = "https://doc.shengwang.cn/doc/showroom/android/landing-page"
    const val findAIDenoiseURL = "https://www.shengwang.cn/AI-denoiser/"
    const val findAISpatialURL = "https://www.shengwang.cn/3D-spatial/"
    const val findScenario1v1URL = "https://www.shengwang.cn/solution/social/"
    const val findDoc1v1URL = "https://doc.shengwang.cn/doc/one-to-one-live/android/rtm/landing-page"
    const val findDocVirtualSoundDoc = "https://www.shengwang.cn/VirtualSoundCard/"
}