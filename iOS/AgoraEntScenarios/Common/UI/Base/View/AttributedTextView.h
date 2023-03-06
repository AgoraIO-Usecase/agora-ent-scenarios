//
//  AttributedTextView.h
//  text
//
//  Created by CP on 2023/1/4.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface AttributedTextView : UITextView
-(instancetype)initWithFrame:(CGRect)frame text:(NSString *)text AttributedStringS:(NSArray *)strings ranges:(NSArray *)ranges textColor:(UIColor *)textColor attributeTextColor:(UIColor *)attributeTextColor;
@end

NS_ASSUME_NONNULL_END
