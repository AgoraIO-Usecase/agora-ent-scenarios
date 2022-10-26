//
//  ZQTCustomSwitch.h
//  ZQTCustomSwitch
//
//  Created by 赵群涛 on 16/5/25.
//  Copyright © 2016年 ZQT. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ZQTCustomSwitch : UIControl<UIGestureRecognizerDelegate>
@property (nonatomic, assign, getter = isOn) BOOL on;

@property (nonatomic, strong) UIColor *onTintColor;
@property (nonatomic, strong) UIColor *tintColor;
@property (nonatomic, strong) UIColor *thumbTintColor;
@property (nonatomic, assign) NSInteger switchKnobSize;
@property (nonatomic, strong) UIColor *textColor;
@property (nonatomic, strong) UIFont *textFont;

@property (nonatomic, strong) NSString *onText;
@property (nonatomic, strong) NSString *offText;

- (void)setOn:(BOOL)on animated:(BOOL)animated;
- (void)setOn:(BOOL)on animated:(BOOL)animated ignoreControlEvents:(BOOL)ignoreControlEvents;
- (id)initWithFrame:(CGRect)frame onColor:(UIColor *)onColor offColor:(UIColor *)offColor font:(UIFont *)font ballSize:(NSInteger )ballSize;


@end
