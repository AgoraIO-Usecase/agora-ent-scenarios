//
//  NSDictionary+dictToModel.m
//  FT_iPhone
//
//  Created by 吴文海 on 2019/4/17.
//  Copyright © 2019 ChangDao. All rights reserved.
//

#import "NSDictionary+dictToModel.h"
#import "NSDictionary+JKSafeAccess.h"

@implementation NSDictionary (dictToModel)

+ (NSDictionary *)addObjToDictStartDic:(NSDictionary *)startDic lastDict:(NSMutableDictionary *)lastDic {
    
    [startDic enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
        
        if ([obj isKindOfClass:[NSDictionary class]]) {
            
            [NSDictionary addObjToDictStartDic:obj lastDict:lastDic];
        } else {
            
            [lastDic jk_setObj:obj forKey:key];
        }
    }];
    return lastDic;
}

@end
