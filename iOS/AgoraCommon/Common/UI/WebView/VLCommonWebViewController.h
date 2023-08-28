//
//  VLCommonWebViewController.h
//  VoiceOnLine
//

#import "BaseViewController.h"

NS_ASSUME_NONNULL_BEGIN

@interface VLCommonWebViewController : BaseViewController

@property (nonatomic, copy) NSString *urlString;

- (void)injectMethod:(NSString *)method;

- (void)evaluateJS: (NSString *)js;

@end

NS_ASSUME_NONNULL_END
