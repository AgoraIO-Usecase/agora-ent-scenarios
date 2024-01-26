//
//  VLMineViewController.m
//  VoiceOnLine
//

#import "VLMineViewController.h"
#import <MobileCoreServices/MobileCoreServices.h>
//#import "AppDelegate+Config.h"
#import "VLCommonWebViewController.h"
#import "VLMineView.h"
#import "VLUploadImageResModel.h"
#import "VLURLPathConfig.h"
#import "VLToast.h"
//#import "VLAPIRequest.h"
#import "VLGlobalHelper.h"
#import "MenuUtils.h"
#import <Photos/Photos.h>
#import "AgoraEntScenarios-Swift.h"
@import Masonry;
@import LEEAlert;
@import AgoraCommon;
typedef NS_ENUM(NSUInteger, AVAuthorizationRequestType){
    photoLibrary = 0,
    camera = 1,
};

@interface VLMineViewController ()
<UINavigationControllerDelegate,UIImagePickerControllerDelegate,VLMineViewDelegate>

@property (nonatomic, strong) VLMineView *mineView;

@end

@implementation VLMineViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setBackgroundImage:@"home_bg_image" bundleName:nil];
    [self setUpUI];
}

- (void)setUpUI {
    VLMineView *mineView = [[VLMineView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT-kBottomTabBarHeight) withDelegate:self];
    [mineView refreseUserInfo:VLUserCenter.user];
    [self.view addSubview:mineView];
    self.mineView = mineView;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self loadRequestUserInfoRequest];
    [self.mineView refreshTableView];
}

#pragma mark - VLMineViewDelegate

- (void)mineViewDidCick:(VLMineViewClickType)type {
    switch (type) {
        case VLMineViewClickTypeMyAccount: {
            VLMineAccountViewController *vc = [[VLMineAccountViewController alloc] init];
            [self.navigationController pushViewController:vc animated:YES];
        }
            break;
        case VLMineViewClickTypeUserProtocol:
            [self pushWebView:kURLPathH5UserAgreement];
            break;
        case VLMineViewClickTypePrivacyProtocol:
            [self pushWebView:kURLPathH5Privacy];
            break;
        case VLMineViewClickTypePersonInfo:
        {
            VLCommonWebViewController *webVC = [[VLCommonWebViewController alloc] init];
            NSString *url = [kURLPathH5PersonInfo stringByAppendingFormat:@"?userNo=%@&projectId=agora_ent_demo&appId=%@&token=%@",
                             VLUserCenter.user.userNo,
                             KeyCenter.AppId,
                             VLUserCenter.user.token];
            webVC.urlString = url;
            [self.navigationController pushViewController:webVC animated:YES];
        }
            break;
        case VLMineViewClickTypeThirdInfoShared:
        {
            VLCommonWebViewController *webVC = [[VLCommonWebViewController alloc] init];
            webVC.urlString = kURLPathH5ThirdInfoShared;
            [self.navigationController pushViewController:webVC animated:YES];
        }
            break;
        case VLMineViewClickTypeAboutUS:
            [self about];
            break;
        case VLMineViewClickTypeDebug:
            [self closeOffDebugMode];
            break;
        case VLMineViewClickTypSubmitFeedback:
        {
            VLFeedbackViewController *feedbackVC = [[VLFeedbackViewController alloc] init];
            [self.navigationController pushViewController:feedbackVC animated:YES];
        }
            break;
        default:
            break;
    }
}

- (void)mineViewDidCickUser:(VLMineViewUserClickType)type {
    if (type == VLMineViewUserClickTypeAvatar) {
        [self showUploadPicAlter];
    }
}

- (void)pushWebView:(NSString *)string {
    VLCommonWebViewController *webVC = [[VLCommonWebViewController alloc] init];
    webVC.urlString = string;
    [self.navigationController pushViewController:webVC animated:YES];
}

- (void)about {
    AboutAgoraEntertainmentViewController *VC = [[AboutAgoraEntertainmentViewController alloc] init];
    [self.navigationController pushViewController:VC animated:YES];
}

- (BOOL)getLibraryAccess {
    return [NSUserDefaults.standardUserDefaults boolForKey:@"LibraryAccess"];
}

- (void)setLibraryAccess:(BOOL)isOpen {
    [NSUserDefaults.standardUserDefaults setBool:isOpen forKey:@"LibraryAccess"];
}

- (void)showAlert {
    UIAlertController *vc = [UIAlertController alertControllerWithTitle:AGLocalizedString(@"app_need_request_photo")
                                                                message:AGLocalizedString(@"app_need_request_photo and upload avatar")
                                                         preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *action1 = [UIAlertAction actionWithTitle:AGLocalizedString(@"not_allowed")
                                                      style:UIAlertActionStyleDefault
                                                    handler:^(UIAlertAction * _Nonnull action) {
        [self setLibraryAccess:NO];
    }];
    UIAlertAction *action2 = [UIAlertAction actionWithTitle:AGLocalizedString(@"requset_ok")
                                                      style:UIAlertActionStyleDefault
                                                    handler:^(UIAlertAction * _Nonnull action) {
        [self setLibraryAccess:YES];
        [self presentviewcontrollerWithSourceType:UIImagePickerControllerSourceTypePhotoLibrary];
    }];
    [vc addAction:action1];
    [vc addAction:action2];
    [self.navigationController presentViewController:vc
                                            animated:YES
                                          completion:nil];
}

- (void)showUploadPicAlter {
    kWeakSelf(self)
    [LEEAlert actionsheet].config
    .LeeAddAction(^(LEEAction * _Nonnull action) {
        action.type = LEEActionTypeDefault;
        action.title = AGLocalizedString(@"app_upload_avatar");
        action.height = 20;
        action.titleColor = [UIColor whiteColor];
        action.font = [UIFont systemFontOfSize:14];
    })
    .LeeAddAction(^(LEEAction * _Nonnull action) {
        action.type = LEEActionTypeDefault;
        action.title = AGLocalizedString(@"take_photo_and_upload");
        action.clickBlock = ^{
            [weakself requestAuthorizationForCamera];
        };
    })
    .LeeAddAction(^(LEEAction * _Nonnull action) {
        action.type = LEEActionTypeDefault;
        action.title = AGLocalizedString(@"local_upload");
        action.clickBlock = ^{
            [weakself requestAuthorizationForPhotoLibrary];
        };
    })
    .LeeAddAction(^(LEEAction * _Nonnull action) {
        action.type = LEEActionTypeCancel;
        action.title = AGLocalizedString(@"cancel");
        action.clickBlock = ^{
        };
    })
    .LeeShow();
}

- (void)requestAuthorizationForPhotoLibrary {
    [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if (status == PHAuthorizationStatusAuthorized) {
                //操作图片
                [self presentviewcontrollerWithSourceType: UIImagePickerControllerSourceTypePhotoLibrary];
            }else{
                [self showAlertWithMessage:@"相册权限未设置,请开启相册权限"];
            }
        });
    }];
}

- (void)requestAuthorizationForCamera{
    [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if(granted == true){
                [self presentviewcontrollerWithSourceType:UIImagePickerControllerSourceTypeCamera];
            } else {
                [self showAlertWithMessage:@"相机权限未设置,请开启相机权限"];
            }
        });
    }];
}

-(void)showAlertWithMessage:(NSString *)mes {
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"提示" message:mes preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [[UIApplication sharedApplication]openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString] options:nil completionHandler:nil];
    }];
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleDefault handler:nil];
    [alertController addAction:cancelAction];
    [alertController addAction:okAction];
    [self presentViewController:alertController animated:YES completion:nil];
}

- (void)presentviewcontrollerWithSourceType:(UIImagePickerControllerSourceType)sourceType {
    if (sourceType == UIImagePickerControllerSourceTypePhotoLibrary && ![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypePhotoLibrary]) {
        [VLToast toast:AGLocalizedString(@"comm_permission_leak_sdcard_title")];
        return ;
    }
    if (sourceType == UIImagePickerControllerSourceTypeCamera && ![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera]) {
        [VLToast toast:AGLocalizedString(@"comm_permission_leak_camera_title")];
        return ;
    }
    UIImagePickerController *controller = [[UIImagePickerController alloc]init];
    [controller.navigationBar setTintColor:UIColorMakeWithHex(@"#ff6535")];
    controller.delegate = self;
    controller.allowsEditing = YES;
    controller.sourceType = sourceType;
    NSMutableArray *mediaTypes = [[NSMutableArray alloc] init];
    [mediaTypes addObject:(__bridge NSString *)kUTTypeImage];
    controller.mediaTypes = mediaTypes;
    [self presentViewController:controller animated:YES completion:^(void){
    }];
}

#pragma mark for debug
- (void)motionEnded:(UIEventSubtype)motion withEvent:(UIEvent *)event {
    [super motionEnded:motion withEvent:event];
    
    UIActivityViewController *controller = [[UIActivityViewController alloc] initWithActivityItems:@[[NSURL fileURLWithPath:[AgoraEntLog cacheDir]]]
                                                                             applicationActivities:nil];

    [self presentViewController:controller animated:YES completion:nil];
}


#pragma mark - UIImagePickerControllerDelegate
- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    [picker dismissViewControllerAnimated:YES completion:^{
        UIImage *image = info[UIImagePickerControllerEditedImage];
        [self uploadHeadImageWithImage:image];
    }];
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    [picker dismissViewControllerAnimated:YES completion:^(){
    }];
}


#pragma mark - Request
/// 获取用户信息
- (void)loadRequestUserInfoRequest {
    NSDictionary *param = @{@"userNo":VLUserCenter.user.userNo ?: @""};
//    [VLAPIRequest getRequestURL:kURLPathGetUserInfo parameter:param showHUD:NO success:^(VLResponseDataModel * _Nonnull response) {
//        if (response.code == 0) {
////            VLLoginModel *userInfo = [VLLoginModel vj_modelWithDictionary:response.data];
//        }
//    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
//
//    }];
    
    VLGetUserInfoNetworkModel *model = [VLGetUserInfoNetworkModel new];
    model.userNo = VLUserCenter.user.userNo ?: @"";
    [model requestWithCompletion:^(NSError * _Nullable error, id _Nullable data) {
        VLResponseData *response = data;
        if (response.code && response.code.integerValue == 0) {
            NSLog(@"获取成功");
            [[VLUserCenter center] storeUserInfo:VLUserCenter.user];
            [self.mineView refreseUserInfo:VLUserCenter.user];
        }else{
            [VLToast toast:response.message];
        }
    }];
}

- (void)loadUpdateUserIconRequest:(NSString *)iconUrl image:(UIImage *)image{
//    NSDictionary *param = @{
//        @"userNo" : VLUserCenter.user.userNo ?: @"",
//        @"headUrl" : iconUrl ?: @""
//    };
    
//    [VLAPIRequest postRequestURL:kURLPathUploadUserInfo parameter:param showHUD:YES success:^(VLResponseDataModel * _Nonnull response) {
//        if (response.code == 0) {
//            [VLToast toast:AGLocalizedString(@"app_edit_success")];
//            [self.mineView refreseAvatar:image];
//            VLUserCenter.user.headUrl = iconUrl;
//            [[VLUserCenter center] storeUserInfo:VLUserCenter.user];
//        }else{
//            [VLToast toast:response.message];
//        }
//    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
//    }];
    
    VLUploadUserInfoNetworkModel *model = [VLUploadUserInfoNetworkModel new];
    model.userNo = VLUserCenter.user.userNo ?: @"";
    model.headUrl = iconUrl ?: @"";
    [model requestWithCompletion:^(NSError * _Nullable error, id _Nullable data) {
        VLResponseData *response = data;
        if (response.code && response.code.integerValue == 0) {
            [VLToast toast:AGLocalizedString(@"app_edit_success")];
            [self.mineView refreseAvatar:image];
            VLUserCenter.user.headUrl = iconUrl;
            [[VLUserCenter center] storeUserInfo:VLUserCenter.user];
        }else{
            [VLToast toast:response.message];
        }
    }];
}

- (void)closeOffDebugMode {
    [LEEAlert alert].config
    .LeeAddTitle(^(UILabel *label) {
        label.text = AGLocalizedString(@"app_exit_debug");
        label.textColor = UIColorMakeWithHex(@"#040925");
        label.font = UIFontBoldMake(16);
    })
    .LeeContent(AGLocalizedString(@"app_exit_debug_tip"))
    .LeeAddAction(^(LEEAction *action) {
        VL(weakSelf);
        action.type = LEEActionTypeCancel;
        action.title = AGLocalizedString(@"confirm");
        action.titleColor = UIColorMakeWithHex(@"#000000");
        action.backgroundColor = UIColorMakeWithHex(@"#EFF4FF");
        action.borderColor = UIColorMakeWithHex(@"#EFF4FF");
        action.cornerRadius = 20;
        action.height = 40;
        action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
        action.font = UIFontBoldMake(16);
        action.clickBlock = ^{
            [AppContext shared].isDebugMode = NO;
            [self.mineView refreshTableView];
        };
    })
    .LeeAddAction(^(LEEAction *action) {
        action.type = LEEActionTypeCancel;
        action.title = AGLocalizedString(@"cancel");
        action.titleColor = UIColorMakeWithHex(@"#FFFFFF");
        action.backgroundColor = UIColorMakeWithHex(@"#2753FF");
        action.cornerRadius = 20;
        action.height = 40;
        action.font = UIFontBoldMake(16);
        action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
        action.borderColor = UIColorMakeWithHex(@"#2753FF");
        action.clickBlock = ^{
            // 取消点击事件Block
        };
    })
    .LeeShow();
}
 
/// 上传图片
/// @param image 图片
- (void)uploadHeadImageWithImage:(UIImage *)image {
    [VLAPIRequest uploadImageURL:kURLPathUploadImage showHUD:YES appendKey:@"file" images:@[image] success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            VLUploadImageResModel *model = [VLUploadImageResModel vj_modelWithDictionary:response.data];
            [self loadUpdateUserIconRequest:model.url image:image];
        }
        else {
            [VLToast toast:response.message];
        }
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        [VLToast toast:error.localizedDescription];
    }];
}

#pragma mark - Public Methods

- (void)configNavigationBar:(UINavigationBar *)navigationBar {
    [super configNavigationBar:navigationBar];
}
- (BOOL)preferredNavigationBarHidden {
    return true;
}
@end
