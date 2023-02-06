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

+ (instancetype)modelWithItemImg:(NSString *)itemImgStr title:(NSString *)title style: (VLMineCellStyle) style {
    VLMineCellModel *model = [VLMineCellModel new];
    model.itemImgStr = itemImgStr;
    model.titleStr = title;
    model.style = style;
    return  model;
}

@end
