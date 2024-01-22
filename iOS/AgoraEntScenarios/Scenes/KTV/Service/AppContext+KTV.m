//
//  AppContext+KTV.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/21.
//

#import "AppContext+KTV.h"
#import "AgoraEntScenarios-Swift.h"

NSString* kServiceImpKey = @"ServiceImpKey";
NSString* kAgoraKTVAPIKey = @"kAgoraKTVAPIKey";
@implementation AppContext (KTV)

#pragma mark mcc
+ (void)setupKtvConfig {
    [AppContext shared].sceneImageBundleName = @"KtvResource";
    [AppContext shared].sceneLocalizeBundleName = @"KtvResource";
}

- (void)setKtvAPI:(KTVApiImpl *)ktvAPI {
    [[AppContext shared].extDic setValue:ktvAPI forKey:kAgoraKTVAPIKey];
}

- (KTVApiImpl*)ktvAPI {
    return [[AppContext shared].extDic valueForKey:kAgoraKTVAPIKey];
}

#pragma mark service
+ (id<KTVServiceProtocol>)ktvServiceImp {
    id<KTVServiceProtocol> ktvServiceImp = [[AppContext shared].extDic valueForKey:kServiceImpKey];
    if (ktvServiceImp == nil) {
//        ktvServiceImp = [KTVServiceImp new];
        ktvServiceImp = [KTVSyncManagerServiceImp new];
        [[AppContext shared].extDic setValue:ktvServiceImp forKey:kServiceImpKey];
    }
    
    return ktvServiceImp;
}

+ (void)unloadServiceImp {
    [[AppContext shared].extDic removeAllObjects];
}

@end
