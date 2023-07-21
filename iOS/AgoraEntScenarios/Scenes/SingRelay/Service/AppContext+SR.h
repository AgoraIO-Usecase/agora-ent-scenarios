//
//  AppContext+KTV.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/21.
//

#import "AgoraEntScenarios-Swift.h"
@import AgoraRtcKit;
NS_ASSUME_NONNULL_BEGIN

@interface AppContext (SR)

@property (nonatomic, nullable) SRApiImpl* srAPI;
+ (void)setupSrConfig;

/// get service imp instance,  thread unsafe
+ (id<SRServiceProtocol>)srServiceImp;

/// free service imp instance, thread unsafe
+ (void)unloadServiceImp;


@end

NS_ASSUME_NONNULL_END
