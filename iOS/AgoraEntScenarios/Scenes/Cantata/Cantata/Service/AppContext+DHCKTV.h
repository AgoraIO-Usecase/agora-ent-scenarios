//
//  AppContext+KTV.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/21.

@import AgoraRtcKit;
@import UIKit;
@import AgoraCommon;
NS_ASSUME_NONNULL_BEGIN

@protocol KTVApiDelegate;
@protocol KTVServiceProtocol;
@interface AppContext (DHCKTV)

@property (nonatomic, nullable) id<KTVApiDelegate> dhcAPI;
+ (void)setupDhcConfig;

/// get service imp instance,  thread unsafe
+ (id<KTVServiceProtocol>)dhcServiceImp;

/// free service imp instance, thread unsafe
+ (void)unloadServiceImp;


@end

NS_ASSUME_NONNULL_END
