//
//  NSString+Emoji.h
//  Ulife_Service
//
//  Created by tcnj on 15/11/9.
//  Copyright © 2015年 UHouse. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSString (Emoji)
// ios 判断禁止输入emoji表情

//把表情转换为空格
+ (NSString *)disable_emoji:(NSString *)text;

//判断字符串是否包含表情
+ (BOOL)isContainsEmoji:(NSString *)string;
/// 判断是否包含某个子字符串
- (BOOL)qmui_includesString:(NSString *)string;

@end
