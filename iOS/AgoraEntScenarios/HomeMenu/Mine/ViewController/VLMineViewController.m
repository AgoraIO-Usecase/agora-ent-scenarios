//
//  VLMineViewController.m
//  VoiceOnLine
//

#import "VLMineViewController.h"
#import <MobileCoreServices/MobileCoreServices.h>
//#import "AppDelegate+Config.h"
#import "UIWindow+Router.h"
#import "VLCommonWebViewController.h"
#import "VLMineView.h"
#import "VLUploadImageResModel.h"
#import "VLUserCenter.h"
#import "VLMacroDefine.h"
#import "VLURLPathConfig.h"
#import "VLFontUtils.h"
#import "VLToast.h"
#import "VLAPIRequest.h"
#import "VLGlobalHelper.h"
#import "MenuUtils.h"
#import <Photos/Photos.h>
#import "AgoraEntScenarios-Swift.h"
@import AgoraCommon;
@import Masonry;

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
    [self setBackgroundImage:@"home_bg_image"];
    [self setNaviTitleName:AGLocalizedString(@"agora")];
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
}

#pragma mark - VLMineViewDelegate

- (void)mineViewDidCick:(VLMineViewClickType)type {
    switch (type) {
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
        case VLMineViewClickTypeLogout:
            [self loadLogoutUserRequest];
            break;
        case VLMineViewClickTypeDestroyAccount:
            [self loadDestoryUserRequest];
            break;
        case VLMineViewClickTypeDebug:
            [self closeOffDebugMode];
        default:
            break;
    }
}

- (void)mineViewDidCickUser:(VLMineViewUserClickType)type {
    if (type == VLMineViewUserClickTypeNickName) {
        [self showUpdateNickNameAlert];
    } else if (type == VLMineViewUserClickTypeAvatar) {
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
    VC.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:VC animated:YES];
}

- (void)userLogout {
    [[VLUserCenter center] logout];
//    [[VLGlobalHelper app] configRootViewController];
    [UIApplication.sharedApplication.delegate.window configRootViewController];
}

- (void)showUpdateNickNameAlert {
    NSArray *array = [[NSArray alloc]initWithObjects:AGLocalizedString(@"cancel"),AGLocalizedString(@"confirm"), nil];
    VL(weakSelf);
    [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:AGLocalizedString(@"edit_name") message:@"" placeHolder:AGLocalizedString(@"input_edit_name") type:ALERTYPETEXTFIELD buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
        if(text && flag == true){
            [weakSelf loadUpdateNickNameRequest:text];
        }
        [[VLAlert shared] dismiss];
    }];
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
    UIAlertController *alertSheet = [UIAlertController alertControllerWithTitle:AGLocalizedString(@"app_upload_avatar") message:AGLocalizedString(@"take_photo_and_upload") preferredStyle:UIAlertControllerStyleActionSheet];
    
    UIAlertAction *upload = [UIAlertAction actionWithTitle:AGLocalizedString(@"local_upload") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [weakself requestAuthorizationForPhotoLibrary];
    }];
    [alertSheet addAction:upload];
    
    UIAlertAction *ipadCanAction = nil;
    if([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        [alertSheet.popoverPresentationController setPermittedArrowDirections:0];//去掉arrow箭头
        alertSheet.popoverPresentationController.sourceView = self.view;
        alertSheet.popoverPresentationController.sourceRect = CGRectMake(0, self.view.height, self.view.width, self.view.height);
        
        ipadCanAction = [UIAlertAction actionWithTitle:AGLocalizedString(@"cancel") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        }];
        [alertSheet addAction:ipadCanAction];
    }

    UIAlertAction *cancel = [UIAlertAction actionWithTitle:AGLocalizedString(@"cancel") style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
            NSLog(@"点击了取消");
    }];
    
    [alertSheet addAction:cancel];
    [self presentViewController:alertSheet animated:YES completion:nil];
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
    [VLAPIRequest getRequestURL:kURLPathGetUserInfo parameter:param showHUD:NO success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
//            VLLoginModel *userInfo = [VLLoginModel vj_modelWithDictionary:response.data];
        }
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
    }];
}

- (void)loadUpdateUserIconRequest:(NSString *)iconUrl image:(UIImage *)image{
    NSDictionary *param = @{
        @"userNo" : VLUserCenter.user.userNo ?: @"",
        @"headUrl" : iconUrl ?: @""
    };
    
    [VLAPIRequest postRequestURL:kURLPathUploadUserInfo parameter:param showHUD:YES success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            [VLToast toast:AGLocalizedString(@"app_edit_success")];
            [self.mineView refreseAvatar:image];
            VLUserCenter.user.headUrl = iconUrl;
            [[VLUserCenter center] storeUserInfo:VLUserCenter.user];
        }else{
            [VLToast toast:response.message];
        }
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
    }];
}


- (void)loadUpdateNickNameRequest:(NSString *)nickName {
    NSDictionary *param = @{
        @"userNo" : VLUserCenter.user.userNo ?: @"",
        @"name" : nickName ?: @""
    };
    [VLAPIRequest postRequestURL:kURLPathUploadUserInfo parameter:param showHUD:YES success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            [VLToast toast:AGLocalizedString(@"app_edit_success")];
            [self.mineView refreseNickName:nickName];
            VLUserCenter.user.name = nickName;
            [[VLUserCenter center] storeUserInfo:VLUserCenter.user];
        }else{
            [VLToast toast:response.message];
        }
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
    }];
}

// 注销账号
- (void)loadDestoryUserRequest {
    
    NSArray *array = [[NSArray alloc]initWithObjects:AGLocalizedString(@"app_logoff"),AGLocalizedString(@"cancel"), nil];
    VL(weakSelf);
    [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:AGLocalizedString(@"app_logoff_account") message:AGLocalizedString(@"logout_tips") placeHolder:@"" type:ALERTYPENORMAL buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
        if(flag == false){
            NSDictionary *param = @{@"userNo":VLUserCenter.user.userNo ?: @""};
            [VLAPIRequest getRequestURL:kURLPathDestroyUser parameter:param showHUD:YES success:^(VLResponseDataModel * _Nonnull response) {
                if (response.code == 0) {
                    [weakSelf userLogout];
                }
            } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
            }];
        }
        [[VLAlert shared] dismiss];
    }];
}

// 退出登录
- (void)loadLogoutUserRequest {
    NSArray *array = [[NSArray alloc]initWithObjects:AGLocalizedString(@"app_exit"),AGLocalizedString(@"cancel"), nil];
    VL(weakSelf);
    [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:AGLocalizedString(@"confirm_logout") message:AGLocalizedString(@"logout_tips") placeHolder:@"" type:ALERTYPENORMAL buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
        if(flag == false){
            [weakSelf userLogout];
        }
        [[VLAlert shared] dismiss];
    }];
}

-(void)closeOffDebugMode {
    NSArray *array = [[NSArray alloc]initWithObjects:AGLocalizedString(@"confirm"),AGLocalizedString(@"cancel"), nil];
    VL(weakSelf);
    [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:AGLocalizedString(@"app_exit_debug") message:AGLocalizedString(@"app_exit_debug_tip") placeHolder:@"" type:ALERTYPENORMAL buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
        if(flag == true){
            [AppContext shared].isDebugMode = NO;
            [weakSelf.mineView refreshTableView];
        }
        [[VLAlert shared] dismiss];
    }];
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
    }];
}

// 连续点击事件
- (void)didTapedVersionLabel {
    [AppContext shared].isDebugMode = YES;
    [self.mineView refreshTableView];
}

#pragma mark - Public Methods

- (void)configNavigationBar:(UINavigationBar *)navigationBar {
    [super configNavigationBar:navigationBar];
}
- (BOOL)preferredNavigationBarHidden {
    return true;
}
@end
