//
//  AppContext+KTV.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/21.
//

#import "AgoraEntScenarios-Swift.h"
#import "KTVServiceProtocol.h"

NS_ASSUME_NONNULL_BEGIN

@interface AppContext (KTV)

+ (void)setupKtvConfig;

/// get service imp instance,  thread unsafe
+ (id<KTVServiceProtocol>)ktvServiceImp;

/// free service imp instance, thread unsafe
+ (void)unloadServiceImp;

@end

NS_ASSUME_NONNULL_END
