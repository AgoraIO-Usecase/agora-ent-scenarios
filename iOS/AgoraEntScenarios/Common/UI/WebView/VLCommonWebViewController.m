//
//  VLCommonWebViewController.m
//  VoiceOnLine
//

#import "VLCommonWebViewController.h"
#import <WebKit/WebKit.h>
#import "VLMacroDefine.h"
#import "AESMacro.h"

@import Masonry;

#pragma mark - KVO KEY
NSString *const USBaseWebViewController_KVO_EstimatedProgress = @"estimatedProgress";
NSString *const USBaseWebViewController_KVO_CanGoBack = @"canGoBack";
NSString *const USBaseWebViewController_KVO_Title = @"title";

@interface VLCommonWebViewController () <WKNavigationDelegate,WKUIDelegate,WKScriptMessageHandler,UINavigationControllerDelegate>

@property(nonatomic,strong) UIProgressView *progressView;
@property(nonatomic,strong) UIColor *progressViewColor;
@property(nonatomic,assign) CGFloat progressViewHeight;
@property(nonatomic,strong) WKWebView *webView;
@property(nonatomic,strong) UIButton *systemWebButton;
@property(nonatomic,strong) UIView *lineView;
@end

@implementation VLCommonWebViewController

- (void)setIsShowSystemWebButton:(BOOL)isShowSystemWebButton {
    _isShowSystemWebButton = isShowSystemWebButton;
    self.systemWebButton.hidden = !isShowSystemWebButton;
}

- (instancetype)init {
    if (self = [super init]) {
        [self.webView addObserver:self forKeyPath:USBaseWebViewController_KVO_EstimatedProgress options:NSKeyValueObservingOptionNew context:NULL];
        [self.webView addObserver:self forKeyPath:USBaseWebViewController_KVO_CanGoBack options:NSKeyValueObservingOptionNew context:NULL];
        [self.webView addObserver:self forKeyPath:USBaseWebViewController_KVO_Title options:NSKeyValueObservingOptionNew context:NULL];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self config];
    [self setupViews];
}

- (void)config {
    [self hiddenBackgroundImage];
    [self setBackBtn];
    self.view.backgroundColor = [UIColor whiteColor];
}

- (void)injectMethod:(NSString *)method {
    [self.webView.configuration.userContentController addScriptMessageHandler:self name:method];
}

- (void)evaluateJS: (NSString *)js {
    [self.webView evaluateJavaScript:js completionHandler:^(id _Nullable obj, NSError * _Nullable error) {
        NSLog(@"obj == %@", obj);
    }];
}

- (void)setupViews {
    [self.view addSubview:self.webView];
    [self.webView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(kTopNavHeight);
        make.bottom.mas_equalTo(-kSafeAreaBottomHeight);
        make.left.right.mas_equalTo(self.view);
    }];
    
    [self.view addSubview:self.lineView];
    [self.lineView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor].active = YES;
    [self.lineView.topAnchor constraintEqualToAnchor:self.view.topAnchor constant:kTopNavHeight].active = YES;
    [self.lineView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor].active = YES;
    [self.lineView.heightAnchor constraintEqualToConstant:0.5].active = YES;
    
    [self.view addSubview:self.systemWebButton];
    [self.systemWebButton.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-16].active = YES;
    [self.systemWebButton.topAnchor constraintEqualToAnchor:self.view.topAnchor constant:kStatusBarHeight + 10].active = YES;
    
    [self.view addSubview:self.progressView];
    [self.progressView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor].active = YES;
    [self.progressView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor].active = YES;
    [self.progressView.heightAnchor constraintEqualToConstant:self.progressViewHeight].active = YES;
    [self.progressView.topAnchor constraintEqualToAnchor:self.view.topAnchor constant:kTopNavHeight].active = YES;
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    if (_progressView) {
        [_progressView removeFromSuperview];
        _progressView = nil;
    }
}

- (void)setUrlString:(NSString *)urlString {
    _urlString = urlString;
    NSURL *url = [NSURL URLWithString:urlString];
    VLLog(@"加载urlString:--%@\nurl:--%@",urlString,url);
    NSURLRequest *request = [NSURLRequest requestWithURL:url cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData timeoutInterval:10];
    [self.webView loadRequest:request];
}

#pragma mark - Override

- (void)backBtnClickEvent {
    if (_webView.canGoBack) {
        [self.webView goBack];
    }else {
        [self.navigationController popViewControllerAnimated:YES];
    }
}

- (void)systemButtonClickEvent {
    if (self.isShowSystemWebButton == NO) {
        return;
    }
    if ([[UIApplication sharedApplication] canOpenURL:self.webView.URL]) {
        [[UIApplication sharedApplication] openURL:self.webView.URL options:@{} completionHandler:nil];
    }
}

#pragma mark - KVO

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary<NSString *,id> *)change context:(void *)context {
    if (object != _webView) {
        [super observeValueForKeyPath:keyPath ofObject:object change:change context:context];
    }else {
        if ([keyPath isEqualToString:USBaseWebViewController_KVO_EstimatedProgress]) {
            if (_progressView.alpha != 1.0f) {
                [_progressView setAlpha:1.0f];
            }
            [_progressView setProgress:_webView.estimatedProgress animated:YES];
            kWeakSelf(self)
            if(_webView.estimatedProgress == 1.0f) {
                [UIView animateWithDuration:0.3 delay:0.3 options:UIViewAnimationOptionCurveEaseOut animations:^{
                    [weakself.progressView setAlpha:0.0f];
                } completion:^(BOOL finished) {
                    [weakself.progressView setProgress:0.0f animated:NO];
                }];
            }
        }else if ([keyPath isEqualToString:USBaseWebViewController_KVO_CanGoBack]) {
//            NSNumber *new = (NSNumber *)change[@"new"];
//            _popButton.customView.hidden = !new.boolValue;
        }else if ([keyPath isEqualToString:USBaseWebViewController_KVO_Title]) {
            [self setNaviTitleName:_webView.title];
        }
    }
}

#pragma mark - Lazy

- (WKWebView *)webView {
    if(!_webView) {
        // js配置
        WKUserContentController *userContentController = [[WKUserContentController alloc] init];
        // WKWebView的配置
        WKWebViewConfiguration *configuration = [[WKWebViewConfiguration alloc] init];
        configuration.userContentController = userContentController;
        _webView = [[WKWebView alloc] initWithFrame:CGRectZero configuration:configuration];
        _webView.backgroundColor = [UIColor whiteColor];
        _webView.navigationDelegate = self;
        _webView.UIDelegate = self;
    }
    return _webView;
}

- (UIColor *)progressViewColor {
    if (!_progressViewColor) {
        _progressViewColor = [UIColor blueColor];
    }
    return _progressViewColor;
}

- (CGFloat)progressViewHeight {
    if (!_progressViewHeight) {
        _progressViewHeight = 2.0;
    }
    return _progressViewHeight;
}

- (UIProgressView *)progressView {
    if (!_progressView) {
        _progressView = [[UIProgressView alloc] init];
        _progressView.progressTintColor = self.progressViewColor;
        _progressView.trackTintColor = [UIColor clearColor];
        _progressView.translatesAutoresizingMaskIntoConstraints = NO;
    }
    return _progressView;
}

- (UIButton *)systemWebButton {
    if (_systemWebButton == nil) {
        _systemWebButton = [[UIButton alloc] init];
        [_systemWebButton setImage:[UIImage imageNamed:@"system_web_icon"] forState:(UIControlStateNormal)];
        _systemWebButton.translatesAutoresizingMaskIntoConstraints = NO;
        _systemWebButton.hidden = YES;
        [_systemWebButton addTarget:self action:@selector(systemButtonClickEvent) forControlEvents:(UIControlEventTouchUpInside)];
    }
    return _systemWebButton;
}
- (UIView *)lineView {
    if (_lineView == nil) {
        _lineView = [[UIView alloc] init];
        _lineView.backgroundColor = [UIColorMakeWithHex(@"#CCCCCC") colorWithAlphaComponent:0.8];
        _lineView.translatesAutoresizingMaskIntoConstraints = NO;
    }
    return _lineView;
}


- (void)dealloc {
    [_webView removeObserver:self forKeyPath:USBaseWebViewController_KVO_EstimatedProgress];
    [_webView removeObserver:self forKeyPath:USBaseWebViewController_KVO_CanGoBack];
    [_webView removeObserver:self forKeyPath:USBaseWebViewController_KVO_Title];
}

- (void)userContentController:(nonnull WKUserContentController *)userContentController didReceiveScriptMessage:(nonnull WKScriptMessage *)message {
    NSLog(@"js调用的方法:%@",message.name);
    NSLog(@"js传过来的数据:%@",message.body);
}

- (void)encodeWithCoder:(nonnull NSCoder *)coder {
    
}

- (void)traitCollectionDidChange:(nullable UITraitCollection *)previousTraitCollection {
    
}

- (void)preferredContentSizeDidChangeForChildContentContainer:(nonnull id<UIContentContainer>)container {
    
}

- (CGSize)sizeForChildContentContainer:(nonnull id<UIContentContainer>)container withParentContainerSize:(CGSize)parentSize {
    return CGSizeZero;
}

- (void)systemLayoutFittingSizeDidChangeForChildContentContainer:(nonnull id<UIContentContainer>)container {

}

- (void)viewWillTransitionToSize:(CGSize)size withTransitionCoordinator:(nonnull id<UIViewControllerTransitionCoordinator>)coordinator {
    
}

- (void)willTransitionToTraitCollection:(nonnull UITraitCollection *)newCollection withTransitionCoordinator:(nonnull id<UIViewControllerTransitionCoordinator>)coordinator {
    
}

- (void)didUpdateFocusInContext:(nonnull UIFocusUpdateContext *)context withAnimationCoordinator:(nonnull UIFocusAnimationCoordinator *)coordinator {
    
}

- (void)setNeedsFocusUpdate {
    
}

- (BOOL)shouldUpdateFocusInContext:(nonnull UIFocusUpdateContext *)context {
    return NO;
}

- (void)updateFocusIfNeeded {
    
}


@end
