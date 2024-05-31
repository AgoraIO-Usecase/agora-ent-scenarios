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
        ktvServiceImp = [[KTVSyncManagerServiceImp alloc] initWithUser:VLUserCenter.user];
      //  ktvServiceImp = [KTVSyncManagerServiceImp new];
        [[AppContext shared].extDic setValue:ktvServiceImp forKey:kServiceImpKey];
    }
    
    return ktvServiceImp;
}

+ (void)unloadKtvServiceImp {
    KTVSyncManagerServiceImp* ktvServiceImp = (KTVSyncManagerServiceImp*)[self ktvServiceImp];
    if ([ktvServiceImp isKindOfClass:[KTVSyncManagerServiceImp class]]) {
        [ktvServiceImp destroy];
    }
    [[AppContext shared].extDic removeAllObjects];
}

+ (NSDictionary<NSString*, VLRoomSeatModel*>* __nullable)ktvSeatMap {
    KTVSyncManagerServiceImp* ktvServiceImp = (KTVSyncManagerServiceImp*)[self ktvServiceImp];
    if (![ktvServiceImp isKindOfClass:[KTVSyncManagerServiceImp class]]) {
        return nil;
    }
    return [ktvServiceImp seatMap];
}

+ (NSArray<VLRoomSelSongModel*>* __nullable)ktvSongList {
    KTVSyncManagerServiceImp* ktvServiceImp = (KTVSyncManagerServiceImp*)[self ktvServiceImp];
    if (![ktvServiceImp isKindOfClass:[KTVSyncManagerServiceImp class]]) {
        return nil;
    }
    return [ktvServiceImp songList];
}

+ (NSArray<KTVChoristerModel*>* __nullable)ktvChoristerList {
    KTVSyncManagerServiceImp* ktvServiceImp = (KTVSyncManagerServiceImp*)[self ktvServiceImp];
    if (![ktvServiceImp isKindOfClass:[KTVSyncManagerServiceImp class]]) {
        return nil;
    }
    return [ktvServiceImp choristerList];
}


+ (BOOL)isKtvRoomOwnerWithSeat:(VLRoomSeatModel*)seat {
    KTVSyncManagerServiceImp* ktvServiceImp = (KTVSyncManagerServiceImp*)[self ktvServiceImp];
    if (![ktvServiceImp isKindOfClass:[KTVSyncManagerServiceImp class]]) {
        return NO;
    }
    
    if ([ktvServiceImp.room.owner.userId length] == 0 && [seat.owner.userId length] == 0) {
        return NO;
    }
    
    return [ktvServiceImp.room.owner.userId isEqualToString:seat.owner.userId];
}


+ (BOOL)isKtvChorusingWithSeat:(VLRoomSeatModel*)seat {
    KTVSyncManagerServiceImp* ktvServiceImp = (KTVSyncManagerServiceImp*)[self ktvServiceImp];
    if (![ktvServiceImp isKindOfClass:[KTVSyncManagerServiceImp class]]) {
        return NO;
    }
    
    NSArray<KTVChoristerModel*>* choristerList = [self ktvChoristerList];
    for (KTVChoristerModel* chorister in choristerList) {
        if ([seat.owner.userId isEqualToString:chorister.userId]) {
            return YES;
        }
    }
    
    return NO;
}

+ (BOOL)isKtvChorusingWithUserId:(NSString*)userId {
    KTVSyncManagerServiceImp* ktvServiceImp = (KTVSyncManagerServiceImp*)[self ktvServiceImp];
    if (![ktvServiceImp isKindOfClass:[KTVSyncManagerServiceImp class]]) {
        return NO;
    }
    
    NSArray<KTVChoristerModel*>* choristerList = [self ktvChoristerList];
    for (KTVChoristerModel* chorister in choristerList) {
        if ([userId isEqualToString:chorister.userId]) {
            return YES;
        }
    }
    
    return NO;
}

+ (BOOL)isKtvSongOwnerWithSeat:(VLRoomSeatModel*)seat {
    VLRoomSelSongModel* song = [[self ktvSongList] firstObject];
    BOOL isSongOwner = [song.owner.userId isEqualToString:NullToString(seat.owner.userId)];
    BOOL isPlaying = [song status] == VLSongPlayStatusPlaying;
    return isSongOwner && isPlaying;
}
@end
