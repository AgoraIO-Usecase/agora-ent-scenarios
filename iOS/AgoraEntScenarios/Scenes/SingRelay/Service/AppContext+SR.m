//
//  AppContext+KTV.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/21.
//

#import "AppContext+SR.h"
#import "AgoraEntScenarios-Swift.h"

NSString* rServiceImpKey = @"ServiceImpKey";
NSString* rAgoraSRAPIKey = @"kAgoraSRAPIKey";
@implementation AppContext (KTV)

#pragma mark mcc
+ (void)setupSrConfig {
    [AppContext shared].sceneImageBundleName = @"SRResource";
    [AppContext shared].sceneLocalizeBundleName = @"SRResource";
}

- (void)setSrAPI:(SRApiImpl *)srAPI {
    [[AppContext shared].extDic setValue:srAPI forKey:rAgoraSRAPIKey];
}

- (SRApiImpl*)srAPI {
    return [[AppContext shared].extDic valueForKey:rAgoraSRAPIKey];
}

#pragma mark service
+ (id<SRServiceProtocol>)srServiceImp {
    id<SRServiceProtocol> srServiceImp = [[AppContext shared].extDic valueForKey:rServiceImpKey];
    if (srServiceImp == nil) {
//        ktvServiceImp = [KTVServiceImp new];
        srServiceImp = [SRSyncManagerServiceImp new];
        [[AppContext shared].extDic setValue:srServiceImp forKey:rServiceImpKey];
    }
    
    return srServiceImp;
}

+ (void)unloadServiceImp {
    [[AppContext shared].extDic removeAllObjects];
}

@end
