//
//  SBGDebugInfo.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/17.
//

#import "SBGDebugInfo.h"


NSString* const sProfileKey = @"SBGDebugProfile";

const NSString* const sTitleKey = @"title";
const NSString* const sSelectedParamKey = @"selected_param";
const NSString* const sUnselectedParamKey = @"unselected_param";
@implementation SBGDebugInfo
+ (NSArray*)debugDataArray {
    return @[
        @{
            sTitleKey: @"dump enable",
            sSelectedParamKey: @[@"{\"rtc.debug.enable\": true}",
                                 @"{\"che.audio.frame_dump\":{\"location\":\"all\",\"action\":\"start\",\"max_size_bytes\":\"120000000\",\"uuid\":\"123456789\",\"duration\":\"1200000\"}}"],
            sUnselectedParamKey: @[@"{\"rtc.debug.enable\": false}"]
        }
    ];
}

+ (NSDictionary*)allProfiles {
    NSDictionary* dic = [[NSUserDefaults standardUserDefaults] objectForKey:sProfileKey];
    
    return dic;
}

+ (void)updateProfiles:(NSDictionary*)profiles {
    [[NSUserDefaults standardUserDefaults] setObject:profiles forKey:sProfileKey];
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
