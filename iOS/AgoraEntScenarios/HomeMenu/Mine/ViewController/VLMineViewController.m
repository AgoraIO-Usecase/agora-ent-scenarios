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
#import "VLAlert.h"
#import "KTVMacro.h"
#import <Photos/Photos.h>
@import AgoraRtcKit;
@import Masonry;

typedef NS_ENUM(NSUInteger, AVAuthorizationRequestType){
    photoLibrary = 0,
    camera = 1,
};

@interface VLMineViewController ()
<UINavigationControllerDelegate,UIImagePickerControllerDelegate,VLMineViewDelegate>

@property (nonatomic, strong) UILabel *versionLabel;
@property (nonatomic, strong) VLMineView *mineView;

@end

@implementation VLMineViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setBackgroundImage:@"home_bg_image"];
    [self setNaviTitleName:AGLocalizedString(@"声网")];
    [self setUpUI];
}

- (void)setUpUI {
    VLMineView *mineView = [[VLMineView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT-kBottomTabBarHeight) withDelegate:self];
    [mineView refreseUserInfo:VLUserCenter.user];
    [self.view addSubview:mineView];
    self.mineView = mineView;
    [self.view addSubview:self.versionLabel];
    [self.versionLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.mas_equalTo(self.view);
        make.bottom.mas_equalTo(-TabBarHeight - 20);
    }];
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
        case VLMineViewClickTypeAboutUS:
            [self pushWebView:kURLPathH5AboutUS];
            break;
        case VLMineViewClickTypeLogout:
            [self loadLogoutUserRequest];
            break;
        case VLMineViewClickTypeDestroyAccount:
            [self loadDestoryUserRequest];
            break;
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

- (void)userLogout {
    [[VLUserCenter center] logout];
//    [[VLGlobalHelper app] configRootViewController];
    [UIApplication.sharedApplication.delegate.window configRootViewController];
}

- (void)showUpdateNickNameAlert {
    NSArray *array = [[NSArray alloc]initWithObjects:AGLocalizedString(@"Cancel"),AGLocalizedString(@"Confirm"), nil];
    VL(weakSelf);
    [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:AGLocalizedString(@"修改昵称") message:@"" placeHolder:@"请输入昵称" type:ALERTYPETEXTFIELD buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
        if(text && flag == true){
            [weakSelf loadUpdateNickNameRequest:text];
        }
        [[VLAlert shared] dismiss];
    }];
}

- (void)showUploadPicAlter {
    kWeakSelf(self)
    UIAlertController *alertSheet = [UIAlertController alertControllerWithTitle:@"" message:@"" preferredStyle:UIAlertControllerStyleActionSheet];
    
    UIAlertAction *upload = [UIAlertAction actionWithTitle:AGLocalizedString(@"本地相册上传") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [weakself requestAuthorizationForPhotoLibrary];
    }];
    [alertSheet addAction:upload];
    
    UIAlertAction *ipadCanAction = nil;
    if([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        [alertSheet.popoverPresentationController setPermittedArrowDirections:0];//去掉arrow箭头
        alertSheet.popoverPresentationController.sourceView = self.view;
        alertSheet.popoverPresentationController.sourceRect = CGRectMake(0, self.view.height, self.view.width, self.view.height);
        
        ipadCanAction = [UIAlertAction actionWithTitle:AGLocalizedString(@"取消") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        }];
        [alertSheet addAction:ipadCanAction];
    }

    UIAlertAction *cancel = [UIAlertAction actionWithTitle:AGLocalizedString(@"取消") style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
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
        [VLToast toast:AGLocalizedString(@"相册不可用")];
        return ;
    }
    if (sourceType == UIImagePickerControllerSourceTypeCamera && ![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera]) {
        [VLToast toast:AGLocalizedString(@"相机不可用")];
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
            [VLToast toast:AGLocalizedString(@"修改成功")];
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
            [VLToast toast:AGLocalizedString(@"修改成功")];
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
    
    NSArray *array = [[NSArray alloc]initWithObjects:AGLocalizedString(@"注销"),AGLocalizedString(@"取消"), nil];
    VL(weakSelf);
    [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:AGLocalizedString(@"确定注销账号？") message:AGLocalizedString(@"注销账号后，您将暂时无法使用该账号体验我们的服务，真的要注销吗？") placeHolder:@"" type:ALERTYPENORMAL buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
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
    NSArray *array = [[NSArray alloc]initWithObjects:AGLocalizedString(@"退出"),AGLocalizedString(@"取消"), nil];
    VL(weakSelf);
    [[VLAlert shared] showAlertWithFrame:UIScreen.mainScreen.bounds title:AGLocalizedString(@"确定退出登录?") message:AGLocalizedString(@"退出登陆后，我们还会继续保留您的账户数据，记得再来体验哦～") placeHolder:@"" type:ALERTYPENORMAL buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
        if(flag == false){
            [weakSelf userLogout];
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

#pragma mark - Public Methods

- (void)configNavigationBar:(UINavigationBar *)navigationBar {
    [super configNavigationBar:navigationBar];
}
- (BOOL)preferredNavigationBarHidden {
    return true;
}

#pragma mark - Lazy

- (UILabel *)versionLabel {
    if (!_versionLabel) {
        _versionLabel = [[UILabel alloc] init];
        _versionLabel.text = [NSString stringWithFormat:AGLocalizedString(@"当前版本号 LTS%@(%@) SDK %@"),
                              [VLGlobalHelper appVersion],
                              [VLGlobalHelper appBuild],
                              [AgoraRtcEngineKit getSdkVersion]];
        _versionLabel.font = VLUIFontMake(12);
        _versionLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    }
    return _versionLabel;
}
    

#pragma mark for debug

- (void)motionEnded:(UIEventSubtype)motion withEvent:(UIEvent *)event {
    [super motionEnded:motion withEvent:event];
    
    UIActivityViewController *controller = [[UIActivityViewController alloc] initWithActivityItems:@[[NSURL fileURLWithPath:[AgoraEntLog cacheDir]]]
                                                                             applicationActivities:nil];

    [self presentViewController:controller animated:YES completion:nil];
}

@end
