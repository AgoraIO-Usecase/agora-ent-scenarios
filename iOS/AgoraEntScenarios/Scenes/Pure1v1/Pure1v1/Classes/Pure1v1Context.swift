import SwiftyBeaver
import AgoraCommon

public class Pure1v1Logger: NSObject {
    
    public static let kLogKey = "Pure1v1"
    
    public static func info(_ text: String, tag: String? = nil) {
        agoraDoMainThreadTask {
            AgoraEntLog.getSceneLogger(with: kLogKey).info(text, context: tag)
        }
    }

    public static func warn(_ text: String, tag: String? = nil) {
        agoraDoMainThreadTask {
            AgoraEntLog.getSceneLogger(with: kLogKey).warning(text, context: tag)
        }
    }

    public static func error(_ text: String, tag: String? = nil) {
        agoraDoMainThreadTask {
            AgoraEntLog.getSceneLogger(with: kLogKey).error(text, context: tag)
        }
    }
}

@objcMembers
public class Pure1v1Context: NSObject {
    
    
    public static func thumbnailInfo() -> [String: String] {
        return [
            "bgImgStr": UIImage.sceneImagePath(name: "Image/list_bg", bundleName: "Pure1v1")!,
            "iconImgStr": UIImage.sceneImagePath(name: "Image/list_bg_icon", bundleName: "Pure1v1")!,
            "titleStr": "user_list_title".pure1v1Localization(),
            "subTitleStr": ""
        ]
    }
    
    public static func showScene(viewController: UIViewController,
                                 userInfo: Pure1v1UserInfo) {
        let vc = Pure1v1UserListViewController()
        vc.userInfo = userInfo
        viewController.navigationController?.pushViewController(vc, animated: true)
    }
}
