//
//  VLBaseModel.m
//  VoiceOnLine
//

#import "VLBaseModel.h"

@implementation VLBaseModel

+ (instancetype)vj_modelWithDictionary:(NSDictionary *)dict {
    return [self yy_modelWithDictionary:dict];
}

- (void)encodeWithCoder:(NSCoder *)aCoder {
    [self yy_modelEncodeWithCoder:aCoder];
}
- (id)initWithCoder:(NSCoder *)aDecoder {
    self = [super init];
    return [self yy_modelInitWithCoder:aDecoder];
}

- (id)copyWithZone:(NSZone *)zone {
    return [self yy_modelCopy];
}

- (NSUInteger)hash {
    return [self yy_modelHash];
}
- (BOOL)isEqual:(id)object {
    return [self yy_modelIsEqual:object];
}

- (NSString *)description {
    return  [self yy_modelDescription];
}

+ (NSDictionary *)vj_modelDictionaryWithJson:(id)json {
    return [NSDictionary yy_modelDictionaryWithClass:[self class] json:json];
}

+ (NSArray *)vj_modelArrayWithJson:(id)json {
    return [NSArray yy_modelArrayWithClass:[self class] json:json];
}

- (void)setValue:(id)value forUndefinedKey:(nonnull NSString *)key {
    NSString *error = [NSString stringWithFormat:@"%@设置了不存在key = %@",[self class],key];
    NSAssert(nil, error);
}

- (id)valueForUndefinedKey:(NSString *)key {
    NSString *error = [NSString stringWithFormat:@"%@获取了不存在key = %@",[self class],key];
    NSAssert(nil, error);
    return @"";
}
/**
 *  过滤空值, 如果字典里的是 "null","<null>",""," ","  ","   " 以及对象空 这个字段将从字典移除, 如果参数不为字典那么它不能转换为模型,返回nil 防止崩溃
 *
 *  @param dic 要转换的字典
 *
 *  @return 过滤后的字典
 */
- (NSDictionary *)modelCustomWillTransformFromDictionary:(NSDictionary *)dic {
    if ([dic isKindOfClass:[NSDictionary class]]) {
        NSMutableDictionary *filterDic = [[NSMutableDictionary alloc] init];
        [[dic allKeys] enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            id value = [dic objectForKey:obj];
            if ([value isKindOfClass:[NSNull class]] && (value  == [NSNull null])) {
                ;
            } else if (([value isKindOfClass:[NSString class]]) &&
                     ([[dic objectForKey:obj] isEqualToString:@"<null>"] ||
                      [value isEqualToString:@"null"] ||
                      [value isEqualToString:@""] ||
                      [value isEqualToString:@" "] ||
                      [value isEqualToString:@"  "] ||
                      [value isEqualToString:@"   "])) {
                ;
            } else if (!value) {
                ;
            } else {
                [filterDic setObject:value forKey:obj];
            }
        }];
        return filterDic;
    }
    return nil;
}
@end
