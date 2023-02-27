//
//  KTVDebugInfo.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/17.
//

#import "KTVDebugInfo.h"


NSString* const kProfileKey = @"KTVDebugProfile";

const NSString* const kTitleKey = @"title";
const NSString* const kSelectedParamKey = @"selected_param";
const NSString* const kUnselectedParamKey = @"unselected_param";
@implementation KTVDebugInfo
+ (NSArray*)debugDataArray {
    return @[
        @{
            kTitleKey: @"dump enable",
            kSelectedParamKey: @[@"{\"rtc.debug.enable\": true}",
                                 @"{\"che.audio.frame_dump\":{\"location\":\"all\",\"action\":\"start\",\"max_size_bytes\":\"120000000\",\"uuid\":\"123456789\",\"duration\":\"1200000\"}}"],
            kUnselectedParamKey: @[@"{\"rtc.debug.enable\": false}"]
        }
    ];
}

+ (NSDictionary*)allProfiles {
    NSDictionary* dic = [[NSUserDefaults standardUserDefaults] objectForKey:kProfileKey];
    
    return dic;
}

+ (void)updateProfiles:(NSDictionary*)profiles {
    [[NSUserDefaults standardUserDefaults] setObject:profiles forKey:kProfileKey];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

+ (BOOL)getSelectedStatusForKey:(NSString*)key {
    NSDictionary* dic = [self allProfiles];
    return [dic[key] boolValue];
}

+ (void)setSelectedStatus:(BOOL)status forKey:(NSString*)key {
    NSDictionary* dic = [self allProfiles];
    NSMutableDictionary* allProfiles = [NSMutableDictionary dictionaryWithDictionary:dic ? : @{}];
    [allProfiles setValue:@(status) forKey:key];
    [self updateProfiles:allProfiles];
}
@end
