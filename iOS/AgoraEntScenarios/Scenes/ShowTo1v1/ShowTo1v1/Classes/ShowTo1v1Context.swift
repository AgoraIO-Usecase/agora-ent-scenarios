import SwiftyBeaver

let pure1v1Logger: SwiftyBeaver.Type = AgoraEntLog.createLog(config: AgoraEntLogConfig.init(sceneName: "ShowTo1v1"))

func showTo1v1Print(_ message: String, context: String = "ShowTo1v1") {
    pure1v1Logger.info(message, context: context)
}

func showTo1v1Warn(_ message: String, context: String = "ShowTo1v1") {
    pure1v1Logger.warning(message, context: context)
}

func showTo1v1Error(_ message: String, context: String = "ShowTo1v1") {
    pure1v1Logger.error(message, context: context)
}

@objcMembers
public class ShowTo1v1Context: NSObject {
   public static func thumbnailInfo() -> [String: String] {
        return [
            "bgImgStr": UIImage.sceneImagePath(name: "Image/list_bg", bundleName: "ShowTo1v1")!,
            "iconImgStr": UIImage.sceneImagePath(name: "Image/list_bg_icon", bundleName: "ShowTo1v1")!,
            "titleStr": "user_list_title".showTo1v1Localization(),
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
                                 userInfo: ShowTo1v1UserInfo) {
        let vc = RoomListViewController()
        vc.userInfo = userInfo
        vc.appId = appId
        vc.appCertificate = appCertificate
        vc.hidesBottomBarWhenPushed = true
        viewController.navigationController?.pushViewController(vc, animated: true)
    }
    
//    public static func 
}
