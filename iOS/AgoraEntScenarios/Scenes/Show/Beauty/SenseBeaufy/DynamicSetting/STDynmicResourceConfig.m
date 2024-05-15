//
//  STDynmicResourceConfig.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2024/3/11.
//

#import "STDynmicResourceConfig.h"

static STDynmicResourceConfig* instance = nil;
@implementation STDynmicResourceConfig

+ (instancetype)shareInstance
{
    static dispatch_once_t onceToken ;
    dispatch_once(&onceToken, ^{
        instance = [[STDynmicResourceConfig alloc] init];
    }) ;
    
    return instance;
}
@end
