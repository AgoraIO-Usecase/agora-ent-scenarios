//
//  AppContext+KTV.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/21.
//

#import "AppContext+DHCKTV.h"

NSString* dServiceImpKey = @"ServiceImpKey";
NSString* dAgoraKTVAPIKey = @"kAgoraKTVAPIKey";
@implementation AppContext (DHCKTV)

#pragma mark mcc
+ (void)setupKtvConfig {
    [AppContext shared].sceneImageBundleName = @"KtvResource";
    [AppContext shared].sceneLocalizeBundleName = @"KtvResource";
}

- (void)setKtvAPI:(KTVApiImpl *)ktvAPI {
    [[AppContext shared].extDic setValue:ktvAPI forKey:dAgoraKTVAPIKey];
}

- (KTVApiImpl*)ktvAPI {
    return [[AppContext shared].extDic valueForKey:dAgoraKTVAPIKey];
}

#pragma mark service
+ (id<KTVServiceProtocol>)ktvServiceImp {
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
