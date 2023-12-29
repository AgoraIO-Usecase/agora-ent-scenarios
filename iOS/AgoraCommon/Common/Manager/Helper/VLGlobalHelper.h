//
//  VLGlobalHelper.h
//  VoiceOnLine
//

#import <Foundation/Foundation.h>
//#import "AppDelegate.h"

NS_ASSUME_NONNULL_BEGIN

@interface VLGlobalHelper : NSObject

//+ (AppDelegate *)app;

+ (NSString *)appVersion;

+ (NSString *)appBuild;

+ (NSData *)compactDictionaryToData:(NSDictionary *)dict;

+ (NSDictionary *)dictionaryForJsonData:(NSData *)jsonData;

+ (NSInteger)getAgoraPlayerUserId:(NSString *)userId;

+ (NSInteger)getAgoraMicUserId:(NSString *)userId;

@end

NS_ASSUME_NONNULL_END
