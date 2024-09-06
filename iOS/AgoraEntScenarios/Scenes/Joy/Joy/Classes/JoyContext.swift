import SwiftyBeaver
import AgoraCommon

public class JoyLogger: NSObject {
    
    public static let kLogKey = "Joy"
    
    public static func info(_ text: String, context: String? = nil) {
        AgoraEntLog.getSceneLogger(with: kLogKey).info(text, context: context)
    }

    public static func warn(_ text: String, context: String? = nil) {
        AgoraEntLog.getSceneLogger(with: kLogKey).warning(text, context: context)
    }

    public static func error(_ text: String, context: String? = nil) {
        AgoraEntLog.getSceneLogger(with: kLogKey).error(text, context: context)
    }
}

let kSceneName = "Joy"

var joyAppId: String = ""
var joyAppCertificate: String = ""
var joyHost: String = ""

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
                                 host: String,
                                 appCertificate: String,
                                 userInfo: JoyUserInfo) {
        joyAppId = appId
        joyHost = host
        joyAppCertificate = appCertificate
        let vc = RoomListViewController(userInfo: userInfo)
        vc.hidesBottomBarWhenPushed = true
        viewController.navigationController?.pushViewController(vc, animated: true)
    }
}
