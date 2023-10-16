//
//  AppContext+SBG.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/21.
//

#import "AgoraEntScenarios-Swift.h"
#import "SBGServiceProtocol.h"
@import AgoraRtcKit;
NS_ASSUME_NONNULL_BEGIN

@interface AppContext (SBG)

@property (nonatomic, nullable) SBGApiImpl* sbgAPI;
+ (void)setupSbgConfig;

/// get service imp instance,  thread unsafe
+ (id<SBGServiceProtocol>)sbgServiceImp;

/// free service imp instance, thread unsafe
+ (void)unloadServiceImp;


@end

NS_ASSUME_NONNULL_END
