//
//  AppContext+SBG.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/21.
//

#import "AppContext+SBG.h"
#import "AgoraEntScenarios-Swift.h"

NSString* sServiceImpKey = @"ServiceImpKey";
NSString* sAgoraSBGAPIKey = @"sAgoraSBGAPIKey";
@implementation AppContext (SBG)

#pragma mark mcc
+ (void)setupSbgConfig {
    [AppContext shared].sceneImageBundleName = @"sbgResource";
    [AppContext shared].sceneLocalizeBundleName = @"sbgResource";
}

- (void)setSbgAPI:(SBGApiImpl *)sbgAPI {
    [[AppContext shared].extDic setValue:sbgAPI forKey:sAgoraSBGAPIKey];
}

- (SBGApiImpl*)sbgAPI {
    return [[AppContext shared].extDic valueForKey:sAgoraSBGAPIKey];
}

#pragma mark service
+ (id<SBGServiceProtocol>)sbgServiceImp {
    id<SBGServiceProtocol> sbgServiceImp = [[AppContext shared].extDic valueForKey:sServiceImpKey];
    if (sbgServiceImp == nil) {
//        SBGServiceImp = [SBGServiceImp new];
        sbgServiceImp = [SBGSyncManagerServiceImp new];
        [[AppContext shared].extDic setValue:sbgServiceImp forKey:sServiceImpKey];
    }
    
    return sbgServiceImp;
}

+ (void)unloadServiceImp {
    [[AppContext shared].extDic removeAllObjects];
}

@end
