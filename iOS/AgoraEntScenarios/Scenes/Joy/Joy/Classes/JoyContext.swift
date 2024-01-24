import SwiftyBeaver

let kSceneName = "Joy"

let joyLogger: SwiftyBeaver.Type = AgoraEntLog.createLog(config: AgoraEntLogConfig.init(sceneName: kSceneName))

func joyPrint(_ message: String, context: String = kSceneName) {
    joyLogger.info(message, context: context)
}

func joyWarn(_ message: String, context: String = kSceneName) {
    joyLogger.warning(message, context: context)
}

func joyError(_ message: String, context: String = kSceneName) {
    joyLogger.error(message, context: context)
}

var joyAppId: String = ""
var joyAppCertificate: String = ""

@objcMembers
public class JoyContext: NSObject {
   public static func thumbnailInfo() -> [String: String] {
        return [
            "bgImgStr": UIImage.sceneImagePath(name: "Image/list_bg", bundleName: kSceneName)!,
            "iconImgStr": UIImage.sceneImagePath(name: "Image/list_bg_icon", bundleName: kSceneName)!,
            "titleStr": "user_list_title".joyLocalization(),
            "subTitleStr": ""
        ]
//        @{
//            @"bgImg":UIImageMake(@"home_live_bg"),
//            @"iconImg":UIImageMake(@"home_live_icon"),
//            @"titleStr":NSLocalizedString(@"app_show_live", nil),
//            @"subTitleStr":@""
//        }
    }
    
    public static func showScene(viewController: UIViewController,
                                 appId: String,
                                 appCertificate: String,
                                 userInfo: JoyUserInfo) {
        joyAppId = appId
        joyAppCertificate = appCertificate
        let vc = RoomListViewController(userInfo: userInfo)
        vc.hidesBottomBarWhenPushed = true
        viewController.navigationController?.pushViewController(vc, animated: true)
    }
}
