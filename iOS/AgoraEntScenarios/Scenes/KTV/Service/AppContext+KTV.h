//
//  AppContext+KTV.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/21.
//

#import "AgoraEntScenarios-Swift.h"
@import AgoraRtcKit;
NS_ASSUME_NONNULL_BEGIN

@interface AppContext (KTV)

@property (nonatomic, nullable) KTVApiImpl* ktvAPI;
+ (void)setupKtvConfig;

/// get service imp instance,  thread unsafe
+ (id<KTVServiceProtocol>)ktvServiceImp;

/// free service imp instance, thread unsafe
+ (void)unloadKtvServiceImp;


+ (NSDictionary<NSString*, VLRoomSeatModel*>* __nullable)ktvSeatMap;
+ (NSArray<VLRoomSelSongModel*>* __nullable)ktvSongList;
+ (NSArray<KTVChoristerModel*>* __nullable)ktvChoristerList;
+ (BOOL)isKtvRoomOwnerWithSeat:(VLRoomSeatModel*)seat;
+ (BOOL)isKtvChorusingWithSeat:(VLRoomSeatModel*)seat;
+ (BOOL)isKtvChorusingWithUserId:(NSString*)userId;
+ (BOOL)isKtvPlayingSongOwnerWithSeat:(VLRoomSeatModel*)seat;
+ (BOOL)isKtvSongOwnerWithUserId:(NSString*)userId;
@end

NS_ASSUME_NONNULL_END
