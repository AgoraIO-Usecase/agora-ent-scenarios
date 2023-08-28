//
//  NSObject+Extension.m
//  dajiaochong
//
//  Created by  qizuwang on 15/8/14.
//  Copyright (c) 2015年 王春景. All rights reserved.
//

#import "NSObject+Extension.h"
#import <objc/runtime.h>
@implementation NSObject(Extension)
-(void)setUserInfo:(NSDictionary *)newUserInfo
{
    objc_setAssociatedObject(self, @"userInfo", newUserInfo, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}
-(id)userInfo
{
    return objc_getAssociatedObject(self, @"userInfo");
}

- (void)setBaseURL:(NSString *)newbaseURL
{
    objc_setAssociatedObject(self, @"baseURL", newbaseURL, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}
-(id)baseURL
{
    return objc_getAssociatedObject(self, @"baseURL");
}
@end
