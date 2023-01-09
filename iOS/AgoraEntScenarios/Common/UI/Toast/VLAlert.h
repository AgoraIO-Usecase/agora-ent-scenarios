//
//  VLAlert.h
//  testAlert
//
//  Created by CP on 2023/1/6.
//

#import <UIKit/UIKit.h>
typedef  NS_ENUM(NSUInteger, ALERTYPE) {
    ALERTYPENORMAL = 0,
    ALERTYPETEXTFIELD = 1,
    ALERTYPEATTRIBUTE = 2,
    ALERTYPECONFIRM = 3,
};
typedef void (^OnCallback)(bool flag, NSString * _Nullable text);
typedef void (^linkCallback)(NSString *tag);
NS_ASSUME_NONNULL_BEGIN

@interface VLAlert : UIView
+ (instancetype)shared;
-(void)showAlertWithFrame:(CGRect)frame title:(NSString *)title message:(NSString *)message placeHolder:(NSString *)placeHolder type:(ALERTYPE)type buttonTitles:(NSArray *)buttonTitles completion:(OnCallback)completion;
-(void)showAttributeAlertWithFrame:(CGRect)frame title:(NSString *)title text:(NSString *)text AttributedStringS:(NSArray *)strings ranges:(NSArray *)ranges textColor:(UIColor *)textColor attributeTextColor:(UIColor *)attributeTextColor buttonTitles:(NSArray *)buttonTitles completion:(OnCallback)completion linkCompletion:(linkCallback)linkCompletion;
-(void)dismiss;
@end

NS_ASSUME_NONNULL_END
