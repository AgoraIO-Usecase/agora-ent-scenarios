//
//  VLBaseModel.h
//  VoiceOnLine
//

#import <Foundation/Foundation.h>
@import YYModel;

NS_ASSUME_NONNULL_BEGIN

@interface VLBaseModel : NSObject <YYModel,NSCoding,NSCopying>

+ (NSArray *)vj_modelArrayWithJson:(id)json;

+ (NSDictionary *)vj_modelDictionaryWithJson:(id)json;

+ (instancetype)vj_modelWithDictionary:(NSDictionary *)dict;

@end

NS_ASSUME_NONNULL_END
