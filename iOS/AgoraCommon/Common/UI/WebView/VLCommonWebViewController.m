//
//  VLCommonWebViewController.m
//  VoiceOnLine
//

#import "VLCommonWebViewController.h"
#import <WebKit/WebKit.h>
#import "VLMacroDefine.h"
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
@end

@implementation VLCommonWebViewController

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
    [self setBackgroundImage:@"home_bg_image"];
    [self setBackBtn];
    [self config];
    [self setupViews];
}

- (void)config {
    self.view.backgroundColor = [UIColor whiteColor];
    if (@available(iOS 11.0,*)) {
        self.webView.scrollView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
    }else {
        self.automaticallyAdjustsScrollViewInsets = YES;
    }
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
    [self.navigationController.navigationBar addSubview:self.progressView];
    [self.navigationController.navigationBar bringSubviewToFront:self.progressView];
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

#pragma mark - Public Methods

- (void)configNavigationBar:(UINavigationBar *)navigationBar {
    [super configNavigationBar:navigationBar];
}
- (BOOL)preferredNavigationBarHidden {
    return NO;
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
//            self.navigationItem.title = _webView.title;
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
        _progressViewHeight = 8.0;
    }
    return _progressViewHeight;
}

- (UIProgressView *)progressView {
    if (!_progressView) {
        CGFloat height = CGRectGetHeight(self.navigationController.navigationBar.frame)- self.progressViewHeight;
        CGFloat width = CGRectGetWidth([UIScreen mainScreen].bounds);
        // y值差度待查
        CGRect frame = CGRectMake(0, height + 4, width, self.progressViewHeight);
        _progressView = [[UIProgressView alloc] initWithFrame:frame];
        _progressView.progressTintColor = self.progressViewColor;
        _progressView.trackTintColor = [UIColor clearColor];
    }
    return _progressView;
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
