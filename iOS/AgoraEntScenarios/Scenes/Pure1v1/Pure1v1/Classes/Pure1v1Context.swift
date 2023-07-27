
@objcMembers
public class Pure1v1Context: NSObject {
   public static func thumbnailInfo() -> [String: String] {
        return [
            "bgImgStr": UIImage.sceneImagePath(name: "Image/list_bg", bundleName: "Pure1v1")!,
            "iconImgStr": UIImage.sceneImagePath(name: "Image/list_bg_icon", bundleName: "Pure1v1")!,
            "titleStr": "user_list_title".pure1v1Localization(),
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
                                 userInfo: Pure1v1UserInfo) {
        let vc = Pure1v1UserListViewController()
        vc.userInfo = userInfo
        vc.appId = appId
        vc.appCertificate = appCertificate
        vc.hidesBottomBarWhenPushed = true
        viewController.navigationController?.pushViewController(vc, animated: true)
    }
    
//    public static func 
}
