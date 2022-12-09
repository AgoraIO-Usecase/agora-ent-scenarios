//
//  KTVSoloController.h
//  AgoraEntScenarios
//
//  Created by ZQZ on 2022/11/29.
//

#import <Foundation/Foundation.h>
@import AgoraRtcKit;

NS_ASSUME_NONNULL_BEGIN
typedef void (^sendStreamSuccess)(BOOL ifSuccess);
typedef enum : NSUInteger {
    KTVSongTypeSolo,
    KTVSongTypeChorus
} KTVSongType;
typedef enum : NSUInteger {
    KTVSingRoleMainSinger,
    KTVSingRoleCoSinger,
    KTVSingRoleAudience
} KTVSingRole;
typedef enum : NSUInteger {
    KTVPlayerTrackOrigin = 0,
    KTVPlayerTrackAcc = 1
} KTVPlayerTrackMode;
typedef enum : NSUInteger {
    KTVLoadSongStateOK,
    KTVLoadSongStateInProgress,
    KTVLoadSongStateNoLyricUrl,
    KTVLoadSongStatePreloadFail
} KTVLoadSongState;

@class KTVApi;
@protocol KTVApiDelegate <NSObject>

- (void)controller:(KTVApi*)controller song:(NSInteger)songCode didChangedToState:(AgoraMediaPlayerState)state;
- (void)controller:(KTVApi*)controller song:(NSInteger)songCode didChangedToPosition:(NSInteger)position;

@end

@interface KTVApi : NSObject

@property(nonatomic, weak)id<KTVApiDelegate> delegate;

-(id)initWithRtcEngine:(AgoraRtcEngineKit *)engine channel:(NSString*)channelName musicCenter:(AgoraMusicContentCenter*)musicCenter player:(nonnull id<AgoraMusicPlayerProtocol>)rtcMediaPlayer dataStreamId:(NSInteger)streamId delegate:(id<KTVApiDelegate>)delegate;
-(void)loadSong:(NSInteger)songCode withSongType:(KTVSongType)type asRole:(KTVSingRole)role withCallback:(void (^ _Nullable)(NSInteger songCode, NSString* lyricUrl, KTVSingRole role, KTVLoadSongState state))block;
-(void)playSong:(NSInteger)songCode withSongType:(KTVSongType)type asRole:(KTVSingRole)role;
-(void)stopSong;
-(void)resumePlay;
-(void)pausePlay;
-(void)selectTrackMode:(KTVPlayerTrackMode)mode;
-(void)sendStreamMessageWithDict:(NSDictionary *)dict
                         success:(_Nullable sendStreamSuccess)success;
@end

NS_ASSUME_NONNULL_END
