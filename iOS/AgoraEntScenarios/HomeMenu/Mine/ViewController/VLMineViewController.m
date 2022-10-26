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
#import <AgoraRtcKit/AgoraRtcKit.h>
#import "VLUserCenter.h"
#import "VLMacroDefine.h"
#import "VLURLPathConfig.h"
#import "VLFontUtils.h"
#import "VLToast.h"
#import "VLAPIRequest.h"
#import "VLGlobalHelper.h"
@import Masonry;
@import LEEAlert;

@interface VLMineViewController ()
<UINavigationControllerDelegate,UIImagePickerControllerDelegate,VLMineViewDelegate>

@property (nonatomic, strong) UILabel *versionLabel;
@property (nonatomic, strong) VLMineView *mineView;

@end

@implementation VLMineViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setBackgroundImage:@"home_bg_image"];
    [self setNaviTitleName:NSLocalizedString(@"声网", nil)];
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

//    VL(weakSelf);
    __block UITextField *TF = nil;

    [LEEAlert alert].config
    .LeeTitle(NSLocalizedString(@"修改昵称", nil))
    .LeeAddTextField(^(UITextField *textField) {
        textField.placeholder = NSLocalizedString(@"请输入昵称", nil);
        textField.textColor = UIColorBlack;
        textField.clearButtonMode=UITextFieldViewModeWhileEditing;
        textField.font = UIFontMake(15);
        if (VLUserCenter.user.name.length > 0) {
            textField.text = VLUserCenter.user.name;
        }
        [textField becomeFirstResponder];
        TF = textField; //赋值
    })
    .LeeAddAction(^(LEEAction *action) {
        action.type = LEEActionTypeCancel;
        action.title = @"取消";
        action.titleColor = UIColorMakeWithHex(@"#000000");
        action.backgroundColor = UIColorMakeWithHex(@"#EFF4FF");
        action.cornerRadius = 20;
        action.height = 40;
        action.font = UIFontBoldMake(16);
        action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
        action.borderColor = UIColorMakeWithHex(@"#EFF4FF");
        action.clickBlock = ^{
            
        };
    })
    .LeeAddAction(^(LEEAction *action) {
        VL(weakSelf);
        action.type = LEEActionTypeCancel;
        action.title = NSLocalizedString(@"确认", nil);
        action.titleColor = UIColorMakeWithHex(@"#FFFFFF");
        action.backgroundColor = UIColorMakeWithHex(@"#2753FF");
        action.cornerRadius = 20;
        action.height = 40;
        action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
        action.font = UIFontBoldMake(16);
        action.clickBlock = ^{
            [weakSelf loadUpdateNickNameRequest:TF.text];
        };
    })
    .leeShouldActionClickClose(^(NSInteger index){
        // 是否可以关闭回调, 当即将关闭时会被调用 根据返回值决定是否执行关闭处理
        // 这里演示了与输入框非空校验结合的例子
        BOOL result = ![TF.text isEqualToString:@""];
        result = index == 1 ? result : YES;
        return result;
    })
    .LeeShow();
}


- (BOOL)getLibraryAccess {
    return [NSUserDefaults.standardUserDefaults boolForKey:@"LibraryAccess"];
}

- (void)setLibraryAccess:(BOOL)isOpen {
    [NSUserDefaults.standardUserDefaults setBool:isOpen forKey:@"LibraryAccess"];
}

- (void)showAlert {
    UIAlertController *vc = [UIAlertController alertControllerWithTitle:NSLocalizedString(@"声动互娱”想访问您的相册", nil)
                                                                message:NSLocalizedString(@"声网需要您开启相册访问功能，读取照片上传头像", nil)
                                                         preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *action1 = [UIAlertAction actionWithTitle:NSLocalizedString(@"不允许", nil)
                                                      style:UIAlertActionStyleDefault
                                                    handler:^(UIAlertAction * _Nonnull action) {
        [self setLibraryAccess:NO];
    }];
    UIAlertAction *action2 = [UIAlertAction actionWithTitle:NSLocalizedString(@"好", nil)
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
        action.title = NSLocalizedString(@"上传头像", nil);
        action.height = 20;
        action.titleColor = [UIColor whiteColor];
        action.font = VLUIFontMake(14);
    })
//    .LeeAddAction(^(LEEAction * _Nonnull action) {
//        action.type = LEEActionTypeDefault;
//        action.title = NSLocalizedString(@"拍照上传", nil);
//        action.clickBlock = ^{
//            [weakself presentviewcontrollerWithSourceType:UIImagePickerControllerSourceTypeCamera];
//        };
//    })
    .LeeAddAction(^(LEEAction * _Nonnull action) {
        action.type = LEEActionTypeDefault;
        action.title = NSLocalizedString(@"本地相册上传", nil);
        action.clickBlock = ^{
            if ([weakself getLibraryAccess] == NO) {
                [weakself showAlert];
                return;
            }
            
            [weakself presentviewcontrollerWithSourceType:UIImagePickerControllerSourceTypePhotoLibrary];
        };
    })
    .LeeAddAction(^(LEEAction * _Nonnull action) {
        action.type = LEEActionTypeCancel;
        action.title = NSLocalizedString(@"取消", nil);
        action.clickBlock = ^{
        };
    })
    .LeeShow();
}


- (void)presentviewcontrollerWithSourceType:(UIImagePickerControllerSourceType)sourceType {
    if (sourceType == UIImagePickerControllerSourceTypePhotoLibrary && ![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypePhotoLibrary]) {
        [VLToast toast:NSLocalizedString(@"相册不可用", nil)];
        return ;
    }
    if (sourceType == UIImagePickerControllerSourceTypeCamera && ![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera]) {
        [VLToast toast:NSLocalizedString(@"相机不可用", nil)];
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
            [VLToast toast:NSLocalizedString(@"修改成功", nil)];
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
            [VLToast toast:NSLocalizedString(@"修改成功", nil)];
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
    
    [LEEAlert alert].config
    .LeeAddTitle(^(UILabel *label) {
        label.text = NSLocalizedString(@"确定注销账号？", nil);
        label.textColor = UIColorMakeWithHex(@"#040925");
        label.font = UIFontBoldMake(16);
    })
    .LeeContent(NSLocalizedString(@"注销账号后，您将暂时无法使用该账号体验我们的服务，真的要注销吗？", nil))
    .LeeAddAction(^(LEEAction *action) {
        VL(weakSelf);
        action.type = LEEActionTypeCancel;
        action.title = NSLocalizedString(@"注销", nil);
        action.titleColor = UIColorMakeWithHex(@"#000000");
        action.backgroundColor = UIColorMakeWithHex(@"#EFF4FF");
        action.borderColor = UIColorMakeWithHex(@"#EFF4FF");
        action.cornerRadius = 20;
        action.height = 40;
        action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
        action.font = UIFontBoldMake(16);
        action.clickBlock = ^{
            NSDictionary *param = @{@"userNo":VLUserCenter.user.userNo ?: @""};
            [VLAPIRequest getRequestURL:kURLPathDestroyUser parameter:param showHUD:YES success:^(VLResponseDataModel * _Nonnull response) {
                if (response.code == 0) {
                    [weakSelf userLogout];
                }
            } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
            }];
        };
    })
    .LeeAddAction(^(LEEAction *action) {
        action.type = LEEActionTypeCancel;
        action.title = NSLocalizedString(@"取消", nil);
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

// 退出登录
- (void)loadLogoutUserRequest {
    [LEEAlert alert].config
    .LeeAddTitle(^(UILabel *label) {
        label.text = NSLocalizedString(@"确定退出登录?", nil);
        label.textColor = UIColorMakeWithHex(@"#040925");
        label.font = UIFontBoldMake(16);
    })
    .LeeContent(NSLocalizedString(@"退出登陆后，我们还会继续保留您的账户数据，记得再来体验哦～", nil))
    .LeeAddAction(^(LEEAction *action) {
        VL(weakSelf);
        action.type = LEEActionTypeCancel;
        action.title = NSLocalizedString(@"退出", nil);
        action.titleColor = UIColorMakeWithHex(@"#000000");
        action.backgroundColor = UIColorMakeWithHex(@"#EFF4FF");
        action.borderColor = UIColorMakeWithHex(@"#EFF4FF");
        action.cornerRadius = 20;
        action.height = 40;
        action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
        action.font = UIFontBoldMake(16);
        action.clickBlock = ^{
            [weakSelf userLogout];
            return;
//            NSDictionary *param = @{@"userNo" : VLUserCenter.user.userNo ?: @""};
//            [VLAPIRequest getRequestURL:kURLPathLogout parameter:param showHUD:YES success:^(VLResponseDataModel * _Nonnull response) {
//                if (response.code == 0) {
//                    [self userLogout];
//                }
//            } failure:^(NSError * _Nullable error) {
//            }];
        };
    })
    .LeeAddAction(^(LEEAction *action) {
        action.type = LEEActionTypeCancel;
        action.title = NSLocalizedString(@"取消", nil);
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
        _versionLabel.text = [NSString stringWithFormat:NSLocalizedString(@"当前版本号 LTS%@(%@) SDK %@", nil),[VLGlobalHelper appVersion], [VLGlobalHelper appBuild], [AgoraRtcEngineKit getSdkVersion]];
        _versionLabel.font = VLUIFontMake(12);
        _versionLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    }
    return _versionLabel;
}

@end
