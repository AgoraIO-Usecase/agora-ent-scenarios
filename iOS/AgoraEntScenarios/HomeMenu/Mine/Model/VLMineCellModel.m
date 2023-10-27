//
//  VLMineCellModel.m
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2023/2/6.
//

#import "VLMineCellModel.h"

@implementation VLMineCellModel

+ (instancetype)modelWithItemImg:(NSString *)itemImgStr title:(NSString *)title {
    return [self modelWithItemImg:itemImgStr title:title style:VLMineCellStyleDefault];
}

+ (instancetype)modelWithItemImg:(NSString *)itemImgStr title:(NSString *)title style: (VLMineCellStyle)style {
    return [self modelWithItemImg:itemImgStr title:title style:style clickType:(VLMineViewClickTypeNone)];
}

+ (instancetype)modelWithItemImg:(NSString *)itemImgStr title:(NSString *)title style: (VLMineCellStyle)style clickType:(VLMineViewClickType)clickType {
    VLMineCellModel *model = [VLMineCellModel new];
    model.itemImgStr = itemImgStr;
    model.titleStr = title;
    model.style = style;
    model.clickType = clickType;
    return  model;
}

+ (instancetype)modelWithItemImg:(NSString *)itemImgStr title:(NSString *)title clickType:(VLMineViewClickType)clickType {
    VLMineCellModel *model = [VLMineCellModel new];
    model.itemImgStr = itemImgStr;
    model.titleStr = title;
    model.style = VLMineCellStyleDefault;
    model.clickType = clickType;
    return  model;
}

@end
