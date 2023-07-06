//
//  VLKTVAlert.h
//  testAlert
//
//  Created by CP on 2023/1/6.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
typedef void (^OnCallback)(bool flag, NSString * _Nullable text);

@interface VLKTVAlert : UIView
+ (instancetype _Nonnull)shared;
-(void)showKTVToastWithFrame:(CGRect)frame image:(UIImage *)image message:(NSString *_Nullable)message buttonTitle:(NSString *)buttonTitle completion:(OnCallback _Nullable)completion;
-(void)dismiss;
@end

NS_ASSUME_NONNULL_END
