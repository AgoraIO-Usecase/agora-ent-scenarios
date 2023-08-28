//
//  VLGlobalHelper.m
//  VoiceOnLine
//

#import "VLGlobalHelper.h"

@implementation VLGlobalHelper

//+ (AppDelegate *)app {
//    return (AppDelegate *)[[UIApplication sharedApplication] delegate];
//}

+ (NSString *)appVersion {
    NSDictionary *infoDictionary = [[NSBundle mainBundle] infoDictionary];
    return [infoDictionary objectForKey:@"CFBundleShortVersionString"];
}

+ (NSString *)appBuild {
    NSDictionary *infoDictionary = [[NSBundle mainBundle] infoDictionary];
    return [infoDictionary objectForKey:@"CFBundleVersion"];
}

+ (NSData *)compactDictionaryToData:(NSDictionary *)dict {
    if (![dict isKindOfClass:[NSDictionary class]]) {
        return nil;
    }
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict options:0 error:nil];
    if (![jsonData isKindOfClass:[NSData class]]) {
        return nil;
    }
    return jsonData;
}

+ (NSDictionary *)dictionaryForJsonData:(NSData *)jsonData{
    if (![jsonData isKindOfClass:[NSData class]] || jsonData.length < 1) {
        return nil;
    }
    id jsonObj = [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingAllowFragments error:nil];
    
    if (![jsonObj isKindOfClass:[NSDictionary class]]) {
        return nil;
    }
    return [NSDictionary dictionaryWithDictionary:(NSDictionary *)jsonObj];
}

+ (NSInteger)getAgoraMicUserId:(NSString *)userId {
    return [userId integerValue] * 10;
}

+ (NSInteger)getAgoraPlayerUserId:(NSString *)userId {
    return [userId integerValue] * 10 + 1;
}

@end
