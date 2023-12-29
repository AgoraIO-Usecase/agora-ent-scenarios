//
//  BaseViewController.m
//  VoiceOnLine
//

#import "BaseViewController.h"
#import "VLMacroDefine.h"
#import "VLHotSpotBtn.h"
#import "AESMacro.h"

@interface BaseViewController ()

@property (nonatomic, strong) UIImageView *backGroundImgView;
@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) VLHotSpotBtn *backBtn;

@end

@implementation BaseViewController

- (void)dealloc {
    [self hideVLEmptyView];
    VLLog(@"控制器%@被销毁",self.class);
}

- (void)updateNavigationBarAppearance {
    
    UINavigationBar *navigationBar = self.navigationController.navigationBar;
    if (!navigationBar) return;
    
    [navigationBar setBackgroundColor:[UIColor whiteColor]];
    navigationBar.barTintColor = [UIColor whiteColor];
    navigationBar.barStyle = UIBarStyleDefault;
    navigationBar.tintColor = [UIColor blackColor];
//    self.titleView.tintColor = [UIColor blackColor];
}

#pragma mark - Life Cycle Methods
- (void)viewDidLoad {
    [super viewDidLoad];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
//    [self updateNavigationBarAppearance];
}

#pragma mark - Intial Methods
- (void)initSubviews {
//    [super initSubviews];
    [self.view setBackgroundColor:UIColorMakeWithHex(@"#FFFFFF")];
}
#pragma mark - Events
- (void)leftButtonDidClickAction {
    if ([self.navigationController.viewControllers count] > 1) {
        [self.navigationController popViewControllerAnimated:true];
    } else {
        [self dismissViewControllerAnimated:true completion:^{
        }];
    }
}

#pragma mark - Public Methods
- (void)hideVLEmptyView {
    if (_vlEmptyView && _vlEmptyView.superview) {
        [self.vlEmptyView removeFromSuperview];
    }
}

- (void)backBtnClickEvent {
    [self.navigationController popViewControllerAnimated:YES];
}

//背景图
- (void)setBackgroundImage:(NSString *)imageName {
    [self.view addSubview:self.backGroundImgView];
    self.backGroundImgView.image = UIImageMake(imageName) ? : [UIImage sceneImageWithName:imageName];
}

- (void)setNaviTitleName:(NSString *)titleStr {
    [self.view addSubview:self.titleLabel];
    self.titleLabel.text = titleStr;
}

- (void)setBackBtn {
    [self.view addSubview:self.backBtn];
}

- (void)configNavigationBar:(UINavigationBar *)navigationBar {
    
}
#pragma mark - Private Method
- (void)setupNavigationItems {
//    [super setupNavigationItems];
    [self configNavigationBar:self.navigationController.navigationBar];
}
- (void)accordingWithNaviBarBtn:(VLNavigationBarStatus)btnStatus
              withBarButtonItem:(UIBarButtonItem *)btnItem {
    
    if (btnStatus == VLNavigationBarStatusLeft) {
        [self.navigationItem setLeftBarButtonItem:btnItem];
    } else {
        [self.navigationItem setRightBarButtonItem:btnItem];
    }
}
#pragma mark - QMUINavigationControllerDelegate
//- (UIColor *)navigationBarTintColor {
//    return UIColorMakeWithHex(@"#0E0E0E");
//}
- (UIImage *)qmui_navigationBarBackgroundImage {
    return UIImageMake(@"navigationbar_background");
}
- (BOOL)shouldCustomizeNavigationBarTransitionIfHideable {
    return true;
}
- (BOOL)preferredNavigationBarHidden {
    return false;
}

- (UIImage *)navigationBarShadowImage {

    return [self navigationBarCuttingLine] ? UIImageMake(@"nav_line") : [UIImage imageWithColor:[UIColor clearColor]];
}
- (BOOL)navigationBarCuttingLine {
    return false;
}
// 是否允许手动滑回 @return true 是、 false否
- (BOOL)forceEnableInteractivePopGestureRecognizer {
    return true;
}
#pragma mark - HomeIndicator
- (BOOL)prefersHomeIndicatorAutoHidden {
    return false;
}
/**
 info.plist文件中，View controller-based status bar appearance项设为YES，
 则View controller对status bar的设置优先级高于application的设置。
 为NO则以application的设置为准，view controller的prefersStatusBarHidden方法无效，是根本不会被调用的。
 
 @return true false
 */
- (BOOL)prefersStatusBarHidden {
    return false;
}
- (UIStatusBarAnimation)preferredStatusBarUpdateAnimation {
    return UIStatusBarAnimationSlide;
}

//MARK: 屏幕
- (void)didInitialize {
//    [super didInitialize];
//    self.supportedOrientationMask = UIInterfaceOrientationMaskPortrait;
}

#pragma mark – Getters and Setters
- (UIImageView *)backGroundImgView {
    if (!_backGroundImgView) {
        _backGroundImgView = [[UIImageView alloc]initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height)];
        _backGroundImgView.contentMode = UIViewContentModeScaleAspectFill;
    }
    return _backGroundImgView;
}

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc]initWithFrame:CGRectMake((self.view.width-200)*0.5, kStatusBarHeight, 200, 40)];
        _titleLabel.textAlignment = NSTextAlignmentCenter;
        _titleLabel.font = [UIFont fontWithName:@"PingFangSC" size:18];
        _titleLabel.font = [UIFont systemFontOfSize:18 weight:UIFontWeightSemibold];
        _titleLabel.textColor = UIColorMakeWithHex(@"#040925");
    }
    return _titleLabel;
}

- (VLHotSpotBtn *)backBtn {
    if (!_backBtn) {
        _backBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(0, kStatusBarHeight, 44, 44)];
        [_backBtn setImage:UIImageMake(@"back") forState:UIControlStateNormal];
        [_backBtn addTarget:self action:@selector(backBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    }
    return _backBtn;
}

@end
