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
    KTVSingRoleMainSinger,
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

@class KTVSoloController;
@protocol KTVSoloControllerDelegate <NSObject>

- (void)controller:(KTVSoloController*)controller song:(NSInteger)songCode didChangedToState:(AgoraMediaPlayerState)state;
- (void)controller:(KTVSoloController*)controller song:(NSInteger)songCode didChangedToPosition:(NSInteger)position;

@end

@interface KTVSoloController : NSObject

@property(nonatomic, weak)id<KTVSoloControllerDelegate> delegate;

-(id)initWithRtcEngine:(AgoraRtcEngineKit *)engine musicCenter:(AgoraMusicContentCenter*)musicCenter player:(nonnull id<AgoraMusicPlayerProtocol>)rtcMediaPlayer dataStreamId:(NSInteger)streamId delegate:(id<KTVSoloControllerDelegate>)delegate;
-(void)loadSong:(NSInteger)songCode asRole:(KTVSingRole)role withCallback:(void (^ _Nullable)(NSInteger songCode, NSString* lyricUrl, KTVSingRole role, KTVLoadSongState state))block;
-(void)playSong:(NSInteger)songCode asRole:(KTVSingRole)role;
-(void)stopSong;
-(void)resumePlay;
-(void)pausePlay;
-(void)selectTrackMode:(KTVPlayerTrackMode)mode;
-(void)sendStreamMessageWithDict:(NSDictionary *)dict
                         success:(_Nullable sendStreamSuccess)success;
@end

NS_ASSUME_NONNULL_END
