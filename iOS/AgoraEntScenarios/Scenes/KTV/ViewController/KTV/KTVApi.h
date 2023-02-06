//
//  KTVSoloController.h
//  AgoraEntScenarios
//
//  Created by ZQZ on 2022/11/29.
//

#import <Foundation/Foundation.h>
#import <AgoraLyricsScore-Swift.h>
@import AgoraRtcKit;

NS_ASSUME_NONNULL_BEGIN
typedef void (^sendStreamSuccess)(BOOL ifSuccess);
typedef enum : NSUInteger {
    KTVSongTypeUnknown,
    KTVSongTypeSolo,
    KTVSongTypeChorus
} KTVSongType;
typedef enum : NSUInteger {
    KTVSingRoleUnknown,
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
    KTVLoadSongStatePreloadFail,
    KTVLoadSongStateIdle
} KTVLoadSongState;

@interface KTVSongConfiguration : NSObject

@property(nonatomic, assign)KTVSongType type;
@property(nonatomic, assign)KTVSingRole role;
@property(nonatomic, assign)NSInteger songCode;
@property(nonatomic, assign)NSInteger mainSingerUid;
@property(nonatomic, assign)NSInteger coSingerUid;

+(KTVSongConfiguration*)configWithSongCode:(NSInteger)songCode;

@end

@class KTVApi;
@protocol KTVApiDelegate <NSObject>

- (void)controller:(KTVApi*)controller song:(NSInteger)songCode didChangedToState:(AgoraMediaPlayerState)state local:(BOOL)local;
- (void)controller:(KTVApi*)controller song:(NSInteger)songCode config:(KTVSongConfiguration*)config didChangedToPosition:(NSInteger)position local:(BOOL)local;

//歌词组件的滚动
-(void)didlrcViewDidScrolledWithCumulativeScore:(NSInteger)score totalScore:(NSInteger)totalScore;
-(void)didlrcViewDidScrollFinishedWithCumulativeScore:(NSInteger)score totalScore:(NSInteger)totalScore;
@end

@interface KTVApi : NSObject

@property(nonatomic, weak)id<KTVApiDelegate> delegate;
@property(nonatomic, weak) KaraokeView* karaokeView;

-(id)initWithRtcEngine:(AgoraRtcEngineKit *)engine channel:(NSString*)channelName musicCenter:(AgoraMusicContentCenter*)musicCenter player:(nonnull id<AgoraMusicPlayerProtocol>)rtcMediaPlayer dataStreamId:(NSInteger)streamId delegate:(id<KTVApiDelegate>)delegate;
-(void)loadSong:(NSInteger)songCode withConfig:(KTVSongConfiguration*)config withCallback:(void (^ _Nullable)(NSInteger songCode, NSString* lyricUrl, KTVSingRole role, KTVLoadSongState state))block;
-(void)playSong:(NSInteger)songCode;
-(void)stopSong;
-(void)resumePlay;
-(void)pausePlay;
-(void)selectTrackMode:(KTVPlayerTrackMode)mode;


- (void)mainRtcEngine:(AgoraRtcEngineKit *)engine didJoinedOfUid:(NSUInteger)uid elapsed:(NSInteger)elapsed;
- (void)mainRtcEngine:(AgoraRtcEngineKit *)engine
reportAudioVolumeIndicationOfSpeakers:(NSArray<AgoraRtcAudioVolumeInfo *> *)speakers
      totalVolume:(NSInteger)totalVolume;
- (void)mainRtcEngine:(AgoraRtcEngineKit * _Nonnull)engine
receiveStreamMessageFromUid:(NSUInteger)uid
         streamId:(NSInteger)streamId
             data:(NSData * _Nonnull)data;


@end

NS_ASSUME_NONNULL_END
