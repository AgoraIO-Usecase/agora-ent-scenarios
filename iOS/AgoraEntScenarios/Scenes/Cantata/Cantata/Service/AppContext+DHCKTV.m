//
//  AppContext+KTV.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/21.
//

#import "AppContext+DHCKTV.h"

NSString* dServiceImpKey = @"dServiceImpKey";
NSString* dAgoraKTVAPIKey = @"dAgoraKTVAPIKey";
@implementation AppContext (DHCKTV)

#pragma mark mcc
+ (void)setupDhcConfig {
    [AppContext shared].sceneImageBundleName = @"KtvResource";
    [AppContext shared].sceneLocalizeBundleName = @"KtvResource";
}

- (void)setDhcAPI:(KTVApiImpl *)dhcAPI {
    [[AppContext shared].extDic setValue:dhcAPI forKey:dAgoraKTVAPIKey];
}

- (KTVApiImpl*)dhcAPI {
    return [[AppContext shared].extDic valueForKey:dAgoraKTVAPIKey];
}

#pragma mark service
+ (id<KTVServiceProtocol>)dhcServiceImp {
    id<KTVServiceProtocol> ktvServiceImp = [[AppContext shared].extDic valueForKey:dServiceImpKey];
    if (ktvServiceImp == nil) {
//        ktvServiceImp = [KTVServiceImp new];
        ktvServiceImp = [KTVSyncManagerServiceImp new];
        [[AppContext shared].extDic setValue:ktvServiceImp forKey:dServiceImpKey];
    }
    
    return ktvServiceImp;
}

+ (void)unloadServiceImp {
    [[AppContext shared].extDic removeAllObjects];
}

@end
