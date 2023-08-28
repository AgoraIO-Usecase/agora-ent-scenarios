//
//  NSDictionary+dictToModel.h
//  FT_iPhone
//
//  Created by 吴文海 on 2019/4/17.
//  Copyright © 2019 ChangDao. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NSDictionary (dictToModel)

/**
 用于转Model的时候数据解析, 将后台返回的JSON的数据全部提取到最外层

 @param startDic 后台返回的原始JSON数据
 @param lastDic 处理后的数据
 @return 处理后的数据
 */
+ (NSDictionary *)addObjToDictStartDic:(NSDictionary *)startDic lastDict:(NSMutableDictionary *)lastDic;
@end

NS_ASSUME_NONNULL_END
