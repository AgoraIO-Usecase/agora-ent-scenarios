//
//  SRDebugInfo.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/17.
//

#import "SRDebugInfo.h"


NSString* const rProfileKey = @"SRDebugProfile";

const NSString* const rTitleKey = @"title";
const NSString* const rSelectedParamKey = @"selected_param";
const NSString* const rUnselectedParamKey = @"unselected_param";
@implementation SRDebugInfo
+ (NSArray*)debugDataArray {
    return @[
        @{
            rTitleKey: @"dump enable",
            rSelectedParamKey: @[@"{\"rtc.debug.enable\": true}",
                                 @"{\"che.audio.frame_dump\":{\"location\":\"all\",\"action\":\"start\",\"max_size_bytes\":\"120000000\",\"uuid\":\"123456789\",\"duration\":\"1200000\"}}"],
            rUnselectedParamKey: @[@"{\"rtc.debug.enable\": false}"]
        }
    ];
}

+ (NSDictionary*)allProfiles {
    NSDictionary* dic = [[NSUserDefaults standardUserDefaults] objectForKey:rProfileKey];
    
    return dic;
}

+ (void)updateProfiles:(NSDictionary*)profiles {
    [[NSUserDefaults standardUserDefaults] setObject:profiles forKey:rProfileKey];
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
