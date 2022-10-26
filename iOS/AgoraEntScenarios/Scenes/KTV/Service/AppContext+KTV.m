//
//  AppContext+KTV.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/21.
//

#import "AppContext+KTV.h"
#import "KTVServiceImp.h"
#import "AgoraEntScenarios-Swift.h"

static id<KTVServiceProtocol> _ktvServiceImp = nil;
@implementation AppContext (KTV)

+ (void)setupKtvConfig {
    [AppContext shared].sceneImageBundleName = @"KtvResource";
    [AppContext shared].sceneLocalizeBundleName = @"KtvResource";
}

+ (id<KTVServiceProtocol>)ktvServiceImp {
    if (_ktvServiceImp == nil) {
//        _ktvServiceImp = [KTVServiceImp new];
        _ktvServiceImp = [KTVSyncManagerServiceImp new];
    }
    
    return _ktvServiceImp;
}

+ (void)unloadServiceImp {
    _ktvServiceImp = nil;
}

@end
