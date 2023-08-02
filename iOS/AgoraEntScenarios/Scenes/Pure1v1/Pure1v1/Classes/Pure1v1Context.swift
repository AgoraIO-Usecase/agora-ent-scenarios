import SwiftyBeaver

let pure1v1Logger: SwiftyBeaver.Type = AgoraEntLog.createLog(config: AgoraEntLogConfig.init(sceneName: "Pure1v1"))

func pure1v1Print(_ message: String) {
    pure1v1Logger.info(message, context: "Pure1v1")
}

func pure1v1Warn(_ message: String) {
    pure1v1Logger.warning(message, context: "Pure1v1")
}

func pure1v1Error(_ message: String) {
    pure1v1Logger.error(message, context: "Pure1v1")
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
                                 appId: String,
                                 appCertificate: String,
                                 userInfo: Pure1v1UserInfo) {
        let vc = Pure1v1UserListViewController()
        vc.userInfo = userInfo
        vc.appId = appId
        vc.appCertificate = appCertificate
        vc.hidesBottomBarWhenPushed = true
        viewController.navigationController?.pushViewController(vc, animated: true)
    }
}
