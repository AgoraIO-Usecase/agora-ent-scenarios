//
//  VLAlert.h
//  testAlert
//
//  Created by CP on 2023/1/6.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
typedef  NS_ENUM(NSUInteger, ALERTYPE) {
    ALERTYPENORMAL = 0,
    ALERTYPETEXTFIELD = 1,
    ALERTYPEATTRIBUTE = 2,
    ALERTYPECONFIRM = 3,
};
typedef void (^OnCallback)(bool flag, NSString * _Nullable text);
typedef void (^linkCallback)(NSString * _Nullable tag);

@interface VLAlert : UIView
+ (instancetype _Nonnull)shared;
-(void)showAlertWithFrame:(CGRect)frame title:(NSString *)title message:(NSString *_Nullable)message placeHolder:(NSString *_Nullable)placeHolder type:(ALERTYPE)type buttonTitles:(NSArray *)buttonTitles completion:(OnCallback _Nullable)completion;
-(void)showAttributeAlertWithFrame:(CGRect)frame title:(NSString * _Nullable)title text:(NSString *)text AttributedStringS:(NSArray *)strings ranges:(NSArray *)ranges textColor:(UIColor *)textColor attributeTextColor:(UIColor * )attributeTextColor buttonTitles:(NSArray *)buttonTitles completion:(OnCallback _Nullable)completion linkCompletion:(linkCallback _Nullable)linkCompletion;
-(void)dismiss;
@end

NS_ASSUME_NONNULL_END
