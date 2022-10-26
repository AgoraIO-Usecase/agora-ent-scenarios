//
//  NSString+Helper.h
//  dajiaochong
//
//  Created by kidstone_test on 16/4/20.
//  Copyright © 2016年 王春景. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSString(Helper)
+ (NSString *)base64StringFromText:(NSString *)text;
+ (BOOL)isBlankString:(NSString *)string;

// 格式化时间转时间戳
+ (NSString *)intervalWithTimeString:(NSString *)timeStr;

- (NSString *)md5;

//获取当前的时间
+ (NSString *)getCurrentTimes;

+ (NSString *)vj_timeInterval:(NSString *)start;

+ (NSInteger)getNumberFromString:(NSString *)string;

+ (NSString *)timeFormatted:(int)totalSeconds;

#pragma mark 账号密码本地检测
// 是否手机号码
+ (BOOL)isValidateTelNumber:(NSString *)number;

// 邮箱格式是否正确
+ (BOOL)checkEmailIsValue:(NSString *)emailStr;

// 密码格式是否正确
+ (BOOL)checkPassWordIsValue:(NSString *)passWordStr;

+ (NSString *)getCurrentDeviceModel;

+ (NSMutableAttributedString*)changeLabelWithText:(NSString*)needText;

+ (NSString *)convertToJsonData:(NSDictionary*)dict;

+ (BOOL)stringContainsEmoji:(NSString *)string;
+ (BOOL)isNineKeyBoard:(NSString *)string;
+ (BOOL)hasEmoji:(NSString*)string;

+ (BOOL)isPureInt:(NSString *)string;
+ (BOOL)isLegalCharacter:(NSString *)string;
+ (NSString *)decodeString:(NSString*)encodedString;
+ (NSString *)getNormalStringFilterHTMLString:(NSString *)htmlStr;
+ (NSString *)formatFloat:(float)f;
@end
