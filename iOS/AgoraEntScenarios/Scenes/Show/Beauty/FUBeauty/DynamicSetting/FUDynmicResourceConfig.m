//
//  FUDynmicResourceConfig.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2024/3/12.
//

#import "FUDynmicResourceConfig.h"

static FUDynmicResourceConfig* instance = nil;
@implementation FUDynmicResourceConfig

+ (instancetype)shareInstance
{
    static dispatch_once_t onceToken ;
    dispatch_once(&onceToken, ^{
        instance = [[FUDynmicResourceConfig alloc] init];
    }) ;
    
    return instance;
}

@end
