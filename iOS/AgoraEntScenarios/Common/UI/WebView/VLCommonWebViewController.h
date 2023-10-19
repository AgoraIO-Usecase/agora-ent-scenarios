//
//  VLCommonWebViewController.h
//  VoiceOnLine
//

#import "VLBaseViewController.h"

NS_ASSUME_NONNULL_BEGIN

@interface VLCommonWebViewController : VLBaseViewController

@property (nonatomic, copy) NSString *urlString;

- (void)injectMethod:(NSString *)method;

- (void)evaluateJS: (NSString *)js;

@end

NS_ASSUME_NONNULL_END
