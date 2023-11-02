//
//  BeautyLoad.m
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/1/13.
//

#import "BeautyLoad.h"
#import "AgoraEntScenarios-Swift.h"

@implementation BeautyLoad

+(void)load {
#if __has_include("st_mobile_common.h")
    BeautyModel.beautyType = BeautyFactoryTypeSense;
#elif __has_include("bef_effect_ai_api.h")
    BeautyModel.beautyType = BeautyFactoryTypeByte;
#elif __has_include(<FURenderKit/FURenderKit.h>)
    BeautyModel.beautyType = BeautyFactoryTypeFu;
#endif
}

@end
