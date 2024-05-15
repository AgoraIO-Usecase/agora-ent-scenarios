//
//  BEDynmicResourceConfig.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2024/3/11.
//

#import "BEDynmicResourceConfig.h"

static BEDynmicResourceConfig* instance = nil;
@implementation BEDynmicResourceConfig

+ (instancetype)shareInstance
{
    static dispatch_once_t onceToken ;
    dispatch_once(&onceToken, ^{
        instance = [[BEDynmicResourceConfig alloc] init];
    }) ;
    
    return instance;
}
@end
