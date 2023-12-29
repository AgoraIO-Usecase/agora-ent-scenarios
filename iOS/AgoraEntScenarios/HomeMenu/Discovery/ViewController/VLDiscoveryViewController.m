//
//  VLDiscoveryViewController.m
//  VoiceOnLine
//

#import "VLDiscoveryViewController.h"
#import <WebKit/WebKit.h>
#import "VLLocalWebViewController.h"
#import "VLMacroDefine.h"
#import "AESMacro.h"

@interface VLDiscoveryViewController ()<WKNavigationDelegate>

@property (nonatomic, strong) UIProgressView *progressView;
@property (nonatomic, strong) WKWebView *webViewWK;

@end

@implementation VLDiscoveryViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setUpUI];
}

- (void)setUpUI {
    self.view.backgroundColor = UIColorMakeWithHex(@"#F7FAFE");
    WKWebViewConfiguration * configuration = [[WKWebViewConfiguration alloc]init];
    configuration.allowsInlineMediaPlayback = YES;
    configuration.mediaTypesRequiringUserActionForPlayback = WKAudiovisualMediaTypeAll;
    CGFloat y = IPHONE_X ? 15 : 0;
    const int clear_cut = 10;
    self.webViewWK = [[WKWebView alloc]initWithFrame:CGRectMake(0, kStatusBarHeight - y + clear_cut, SCREEN_WIDTH, SCREEN_HEIGHT - kSafeAreaBottomHeight - y + clear_cut) configuration:configuration];
    self.webViewWK.scrollView.backgroundColor = UIColorMakeWithHex(@"#F7FAFE");
    self.webViewWK.backgroundColor = UIColorMakeWithHex(@"#F7FAFE");
    self.webViewWK.navigationDelegate=self;

    if (@available(iOS 11.0, *)) {
        self.webViewWK.scrollView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
    }
    [self.view addSubview:self.webViewWK];
    [self makeProgressView];
    [self loadLocalHtml];
}

- (void)loadLocalHtml {
    NSString *path = [[NSBundle mainBundle] bundlePath];
    NSURL *baseURL = [NSURL fileURLWithPath:path];
    NSString *htmlPath = [[NSBundle mainBundle] pathForResource:@"index" ofType:@"html"];
    NSString *html = [NSString stringWithContentsOfFile:htmlPath encoding:NSUTF8StringEncoding error:nil];
    [self.webViewWK loadHTMLString:html baseURL:baseURL];
}

#pragma mark - Public Methods
- (void)configNavigationBar:(UINavigationBar *)navigationBar {
    [super configNavigationBar:navigationBar];
    
}
- (BOOL)preferredNavigationBarHidden {
    return true;
}

// 创建进度条
-(void)makeProgressView
{
    [self.webViewWK addObserver:self forKeyPath:@"estimatedProgress" options:NSKeyValueObservingOptionNew context:nil];
    CGFloat y = IPHONE_X ? (kStatusBarHeight - 12) : kStatusBarHeight;
    self.progressView = [[UIProgressView alloc]initWithFrame:CGRectMake(0, y, CGRectGetWidth(self.view.frame),2)];
    [self.view addSubview:self.progressView];
}

#pragma mark - KVO代理方法
- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context{
    if ([keyPath isEqual: @"estimatedProgress"] && object == self.webViewWK) {
        [self.progressView setAlpha:1.0f];
        [self.progressView setProgress:self.webViewWK.estimatedProgress animated:YES];
        if(self.webViewWK.estimatedProgress >= 1.0f)
        {
            [UIView animateWithDuration:0.3 delay:0.3 options:UIViewAnimationOptionCurveEaseOut animations:^{
                [self.progressView setAlpha:0.0f];
            } completion:^(BOOL finished) {
                [self.progressView setProgress:0.0f animated:NO];
            }];
        }
    }else {
        [super observeValueForKeyPath:keyPath ofObject:object change:change context:context];
    }
}

- (void)dealloc {
    //移除观察者
    if (self.progressView) {
        [self.webViewWK removeObserver:self forKeyPath:@"estimatedProgress"];
    }
}
- (void)webView:(WKWebView *)webView decidePolicyForNavigationAction:(WKNavigationAction *)navigationAction decisionHandler:(void (^)(WKNavigationActionPolicy))decisionHandler {
    NSURLRequest *request        = navigationAction.request;
    NSString     *relativePath         = [request.URL relativePath];
    NSArray  *array = [relativePath componentsSeparatedByString:@"/"];
    NSString *queryStr = [request.URL query];
    if (array != nil) {
        if (queryStr != nil && [array lastObject] != nil) {
            VLLocalWebViewController *vc = [[VLLocalWebViewController alloc]init];
            vc.pathStr = queryStr;
            vc.nameStr = [array lastObject];
            [self.navigationController pushViewController:vc animated:YES];
            decisionHandler(WKNavigationActionPolicyCancel);
            return;
        }
    }


    decisionHandler(WKNavigationActionPolicyAllow);
}

@end
