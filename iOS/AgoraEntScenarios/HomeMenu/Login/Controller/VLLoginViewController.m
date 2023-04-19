//
//  VLLoginViewController.m
//  VoiceOnLine
//

#import "VLLoginViewController.h"
#import "VLLoginInputPhoneView.h"
#import "VLLoginInputVerifyCodeView.h"
//#import "AppDelegate+Config.h"
#import "UIWindow+Router.h"
#import "VLPrivacyCustomView.h"
#import "VLCommonWebViewController.h"
#import "VLPopImageVerifyView.h"
#import "VLToast.h"
#import "LSTPopView.h"
#import "VLMacroDefine.h"
#import "VLFontUtils.h"
#import "VLURLPathConfig.h"
#import "VLAPIRequest.h"
#import "NSString+Helper.h"
#import "VLLoginModel.h"
#import "VLUserCenter.h"
#import "VLGlobalHelper.h"
#import "MenuUtils.h"
#import "KTVMacro.h"
#import "VLAlert.h"
#import "AttributedTextView.h"
@import Masonry;
@import YYCategories;
@import SDWebImage;

@interface VLLoginViewController () <VLLoginInputVerifyCodeViewDelegate, VLPrivacyCustomViewDelegate,VLPopImageVerifyViewDelegate, UITextViewDelegate>

@property (nonatomic, strong) UILabel *appNameLabel;
@property (nonatomic, strong) VLLoginInputPhoneView *phoneView;
@property (nonatomic, strong) VLLoginInputVerifyCodeView *verifyView;
@property (nonatomic, strong) VLPrivacyCustomView *privacyCustomView;
@property (nonatomic, strong) UIButton *loginButton;
@property (nonatomic, strong) UIButton *agreeButton;    // 同意按钮
@property (nonatomic, strong) LSTPopView *popView;
@property (nonatomic, assign) bool policyAgreed;
@property (nonatomic, strong) AttributedTextView *textView;
@end

@implementation VLLoginViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    _policyAgreed = NO;
    
    [self setBackgroundImage:@"home_bg_image"];
    [self setNaviTitleName:AGLocalizedString(@"声网")];
    [self setupViews];
    [self setupLayout];
}

- (void)viewDidAppear:(BOOL)animated
{
    [self showAlertPrivacyView];
}

- (void)showAlertPrivacyView
{
    //if(_policyAgreed == NO) {
     [self alertPrivacyAlertView:_policyAgreed];
    //}
}

- (void)setupViews {
    
    [self.view addSubview:self.appNameLabel];
    [self.view addSubview:self.phoneView];
    [self.view addSubview:self.verifyView];
    [self.view addSubview:self.agreeButton];
    [self addAttributeView];
    [self.view addSubview:self.loginButton];
}

- (void)setupLayout {
    
    [self.appNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(30);
        make.top.mas_equalTo(kStatusBarHeight + 125);

    }];
    [self.phoneView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self.appNameLabel);
        make.right.mas_equalTo(-30);
        make.top.mas_equalTo(self.appNameLabel.mas_bottom).offset(24);
    }];

    [self.verifyView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self.appNameLabel);
        make.right.mas_equalTo(-30);
        make.top.mas_equalTo(self.phoneView.mas_bottom).offset(24);
    }];

    [self.agreeButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(30);
        make.width.height.mas_equalTo(24);
        make.top.mas_equalTo(self.verifyView.mas_bottom).offset(20);
    }];
    
    [self.textView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self.agreeButton.mas_right).offset(5);
        make.right.mas_equalTo(self.view.mas_right).offset(-20);
        make.height.mas_equalTo(24);
        make.centerY.mas_equalTo(self.agreeButton).offset(-3);
    }];
    
    [self.loginButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(30);
        make.right.mas_equalTo(-30);
        make.height.mas_equalTo(48);
        make.top.mas_equalTo(self.verifyView.mas_bottom).offset(64);
    }];
}

-(void)addAttributeView {
    NSString *textString = @"我已阅读并同意 用户协议 及 隐私政策 ";
    NSRange range1 = NSMakeRange(8, 4);
    NSRange range2 = NSMakeRange(15, 4);
    NSArray *array = [[NSArray alloc]initWithObjects:@"用户协议",@"隐私政策", nil];
    NSMutableArray *ranges = [[NSMutableArray alloc]init];
    [ranges addObject:[NSValue valueWithRange:range1]];
    [ranges addObject:[NSValue valueWithRange:range2]];
    _textView = [[AttributedTextView alloc]initWithFrame:CGRectZero text: textString AttributedStringS:array ranges:ranges textColor:UIColorMakeWithHex(@"#6C7192") attributeTextColor:UIColorMakeWithHex(@"#009FFF")];
    _textView .delegate = self;
    [self.view addSubview:_textView];
}

- (void)alertPrivacyAlertView:(int)pass {
   [self showPrivacyViewWith:pass];
}

#pragma mark - Public Methods

- (void)configNavigationBar:(UINavigationBar *)navigationBar {
    [super configNavigationBar:navigationBar];
}
- (BOOL)preferredNavigationBarHidden {
    return YES;
}

#pragma mark - Request

- (void)loadVerifyCodeRequest {
    
}

- (void)privacyCustomViewDidChange:(bool)agree {
    if(agree == true){
        _policyAgreed = NO;
    } else {
        _policyAgreed = YES;
    }
}

#pragma mark - VLLoginInputVerifyCodeViewDelegate

- (void)verifyCodeViewDidClickSendVerifyCode:(UIButton *)sender{
    if (![self checkPhoneNo]) return;

    NSDictionary *param = @{
        @"phone":self.phoneView.phoneNo
    };
    [VLAPIRequest getRequestURL:kURLPathVerifyCode parameter:param showHUD:YES success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            [self.verifyView startTime:sender];
        } else {
            [VLToast toast:response.message];
        }
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
    }];
}

#pragma mark - Action

- (void)loginClick:(UIButton *)button {
    if (![self checkPhoneNo]) return;
    if (![self checkPrivacyAgree]) return;
    if (![self checkVerifyCode]) return;
    
    [self popImageVerifyView];
}

-(BOOL)checkVerifyCode {
    
    if (_verifyView.isVerifyCodeSent == NO || [_verifyView.verifyCode isEqualToString:@""] || !_verifyView.verifyCode) {
        [VLToast toast:AGLocalizedString(@"请输入验证码")];
        return NO;
    }
    return YES;
}

-(BOOL)checkPrivacyAgree {
    if (!self.agreeButton.selected) {
        [VLToast toast:AGLocalizedString(@"请您认真阅读用户协议及隐私政策的条款内容，同意后可开始使用我们的服务")];
        return NO;
    }
    return YES;
}

-(BOOL)checkPhoneNo {
    if ([_phoneView.phoneNo isEqualToString:@""] || !_phoneView.phoneNo) {
        [VLToast toast:AGLocalizedString(@"请输入手机号")];
        return NO;
    }
    
    if (![NSString isValidateTelNumber:_phoneView.phoneNo]) {
        [VLToast toast:AGLocalizedString(@"手机号码格式错误")];
        return NO;
    }
    
    return YES;
}

- (void)agreeButtonAction {
    self.agreeButton.selected = !self.agreeButton.selected;
}

- (void)navigatorToWebviewOfUserProtocol{
    [self pushToWebView:kURLPathH5UserAgreement];
}

- (void)navigatorToWebviewOfPrivacyProtocol{
    [self pushToWebView:kURLPathH5Privacy];
}

- (void)pushToWebView:(NSString *)url {
    VLCommonWebViewController *webVC = [[VLCommonWebViewController alloc] init];
    webVC.urlString = url;
    if (!self.navigationController) {
        [self presentViewController:webVC animated:YES completion:nil];
    } else {
        [self.navigationController pushViewController:webVC animated:YES];
    }
}

- (void)popImageVerifyView {
    
    VLPopImageVerifyView *imageVerifyView = [[VLPopImageVerifyView alloc]initWithFrame:CGRectMake(40, 0, SCREEN_WIDTH-80, 198+(SCREEN_WIDTH-120)*0.65) withDelegate:self];
    
    LSTPopView *popView = [LSTPopView initWithCustomView:imageVerifyView parentView:self.view popStyle:LSTPopStyleFade dismissStyle:LSTDismissStyleFade];
    popView.hemStyle = LSTHemStyleCenter;
    popView.popDuration = 0.5;
    popView.dismissDuration = 0.5;
    popView.cornerRadius = 16;
    self.popView = popView;
    popView.isClickFeedback = NO;
    [popView pop];
}

#pragma mark --delegate
- (void)closeBtnAction {
    [self.popView dismiss];
}

- (void)slideSuccessAction {
    [self.popView dismiss];
    
    NSDictionary *param = @{
        @"phone" : self.phoneView.phoneNo,
        @"code": self.verifyView.verifyCode
    };
    [VLAPIRequest getRequestURL:kURLPathLogin parameter:param showHUD:YES success:^(VLResponseDataModel * _Nonnull response) {
        if (response.code == 0) {
            VLLoginModel *model = [VLLoginModel vj_modelWithDictionary:response.data];
            [[VLUserCenter center] storeUserInfo:model];
            [UIApplication.sharedApplication.delegate.window configRootViewController];
        }
        else {
            dispatch_main_async_safe(^{
                [VLToast toast:AGLocalizedString(@"验证码校验失败，请重试")];
            })
        }
    } failure:^(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task) {
        dispatch_main_async_safe(^{
            [VLToast toast:AGLocalizedString(@"验证码校验失败，请重试")];
        })
    }];
}

#pragma mark - Lazy

- (UILabel *)appNameLabel {
    if (!_appNameLabel) {
        _appNameLabel = [[UILabel alloc] init];
        _appNameLabel.font = VLUIFontBoldMake(26);
        _appNameLabel.textColor = UIColorMakeWithHex(@"#040925");
        _appNameLabel.text = AGLocalizedString(@"欢迎体验声网服务");
    }
    return _appNameLabel;
}

- (VLLoginInputPhoneView *)phoneView {
    if (!_phoneView) {
        _phoneView = [[VLLoginInputPhoneView alloc] init];
    }
    return _phoneView;
}

- (VLLoginInputVerifyCodeView *)verifyView {
    if (!_verifyView) {
        _verifyView = [[VLLoginInputVerifyCodeView alloc] init];
        _verifyView.delegate = self;
    }
    return _verifyView;
}

- (UIButton *)agreeButton {
    if (!_agreeButton) {
        _agreeButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_agreeButton setImage:[UIImage imageNamed:@"online_create_screatNormalIcon"] forState:UIControlStateNormal];
        [_agreeButton setImage:[UIImage imageNamed:@"online_create_screatSelIcon"] forState:UIControlStateSelected];
        [_agreeButton addTarget:self action:@selector(agreeButtonAction) forControlEvents:UIControlEventTouchUpInside];
    }
    return _agreeButton;
}

- (UIButton *)loginButton {
    if (!_loginButton) {
        _loginButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_loginButton setTitle:AGLocalizedString(@"登录") forState:UIControlStateNormal];
        [_loginButton addTarget:self action:@selector(loginClick:) forControlEvents:UIControlEventTouchUpInside];
        [_loginButton setBackgroundColor:UIColorMakeWithHex(@"#345DFF")];
        CAGradientLayer *gl = [CAGradientLayer layer];
        gl.frame = CGRectMake(30,445,315,48);
        gl.startPoint = CGPointMake(0.43, 0.01);
        gl.endPoint = CGPointMake(0.43, 1);
        gl.colors = @[(__bridge id)[UIColor colorWithRed:33/255.0 green:155/255.0 blue:255/255.0 alpha:1.0].CGColor, (__bridge id)[UIColor colorWithRed:52/255.0 green:93/255.0 blue:255/255.0 alpha:1.0].CGColor];
        gl.locations = @[@(0), @(1.0f)];
        _loginButton.layer.cornerRadius = 24;
        _loginButton.layer.shadowColor = [UIColor colorWithRed:0/255.0 green:139/255.0 blue:250/255.0 alpha:0.2000].CGColor;
        _loginButton.layer.shadowOffset = CGSizeMake(0,4);
        _loginButton.layer.shadowOpacity = 1;
        _loginButton.layer.shadowRadius = 8;
    }
    return _loginButton;
}

- (VLPrivacyCustomView *)getPrivacyCustomView:(int)pass {
    if (!_privacyCustomView) {
        _privacyCustomView = [[VLPrivacyCustomView alloc] initWithPass:pass];
        if(pass == 0) {
            _privacyCustomView.frame = CGRectMake(0, 0, 250, 260);
        }
        else {
            _privacyCustomView.frame = CGRectMake(0, 0, 250, 120);
        }
        _privacyCustomView.backgroundColor = [UIColor whiteColor];
        _privacyCustomView.delegate = self;
    }
    return _privacyCustomView;
}

-(void)showPrivacyViewWith:(int)pass {
    NSString *textString =  pass == 0 ?  AGLocalizedString(@"声动互娱软件是一款用于向声网客户展示产品使用效果的测试产品，仅用于测试产品的功能、性能和可用性，而非提供给大众使用的正式产品。\n1.我们将依据《用户协议》及《隐私政策》来帮助您了解我们在收集、使用、存储您个人信息的情况以及您享有的相关权利。\n2.在您使用本测试软件时，我们将收集您的设备信息、日志信息等，同时根据不同使用场景，你可以授予我们获取您设备的麦克风权限、摄像头权限等信息。\n\n您可通过阅读完整的《用户协议》及《隐私政策》来了解详细信息。") : AGLocalizedString(@"同意 用户协议 及 隐私政策 后，声动互娱才能为您提供协作服务。");
            NSRange range1 = pass == 0 ? NSMakeRange(72, 4) : NSMakeRange(3, 4);
            NSRange range2 = pass == 0 ? NSMakeRange(79, 4) : NSMakeRange(10, 4);
            NSRange range3 = NSMakeRange(203, 4);
            NSRange range4 = NSMakeRange(210, 4);
    NSArray *arrayTitles = pass == 0 ? [[NSArray alloc]initWithObjects:@"用户协议",@"隐私政策",@"用户协议",@"隐私政策", nil] : [[NSArray alloc]initWithObjects:@"用户协议",@"隐私政策", nil];
            NSMutableArray *ranges = [[NSMutableArray alloc]init];
    if(pass == 0){
        [ranges addObject:[NSValue valueWithRange:range1]];
        [ranges addObject:[NSValue valueWithRange:range2]];
        [ranges addObject:[NSValue valueWithRange:range3]];
        [ranges addObject:[NSValue valueWithRange:range4]];
    } else {
        [ranges addObject:[NSValue valueWithRange:range1]];
        [ranges addObject:[NSValue valueWithRange:range2]];
    }
    
    CGRect rect = CGRectMake(0, 0, UIScreen.mainScreen.bounds.size.width, pass == 0 ? 260 : 120);
    NSString *confirmTitle, *cancelTitle;
    if(pass == 0) {
        cancelTitle = AGLocalizedString(@"不同意");
        confirmTitle = AGLocalizedString(@"同意");
    }
    else {
        cancelTitle = AGLocalizedString(@"不同意并退出");
        confirmTitle = AGLocalizedString(@"同意并继续");
    }
    NSArray *array = [[NSArray alloc]initWithObjects:cancelTitle,confirmTitle, nil];
    kWeakSelf(self)
    [[VLAlert shared] showAttributeAlertWithFrame:rect title:@"个人信息保护指引" text:textString AttributedStringS:arrayTitles ranges:ranges textColor:UIColorMakeWithHex(@"#6C7192") attributeTextColor:UIColorMakeWithHex(@"#009FFF") buttonTitles:array completion:^(bool flag, NSString * _Nullable text) {
        [[VLAlert shared] dismiss];
        if(pass == 0){
            [weakself privacyCustomViewDidChange:flag];
        } else {
            if(flag == false){
                exit(0);
            }
        }
        
    } linkCompletion:^(NSString *tag) {
        [[VLAlert shared] dismiss];
        if([tag isEqualToString:@"0"] || [tag isEqualToString:@"2"]){
            [weakself navigatorToWebviewOfUserProtocol];
        } else {
            [weakself navigatorToWebviewOfPrivacyProtocol];
        }
    }];
    
}

- (BOOL)textView:(UITextView *)textView shouldInteractWithURL:(NSURL *)URL inRange:(NSRange)characterRange interaction:(UITextItemInteraction)interaction {
    NSURL *url = [NSURL URLWithString:@"0"];
    if([url isEqual:URL]){
        [self navigatorToWebviewOfUserProtocol];
    } else {
        [self navigatorToWebviewOfPrivacyProtocol];
    }
    return YES;
}

@end
