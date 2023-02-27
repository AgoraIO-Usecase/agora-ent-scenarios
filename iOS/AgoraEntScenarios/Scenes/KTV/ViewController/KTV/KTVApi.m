//
//  KTVSoloController.m
//  AgoraEntScenarios
//
//  Created by ZQZ on 2022/11/29.
//

#import "KTVApi.h"
#import "VLMacroDefine.h"
#import "KTVMacro.h"
#import <AgoraLyricsScore-Swift.h>
#import "AppContext+KTV.h"
#import "VLGlobalHelper.h"
#import "AgoraEntScenarios-Swift.h"

typedef void (^LyricCallback)(NSString* lyricUrl);
typedef void (^LoadMusicCallback)(AgoraMusicContentCenterPreloadStatus);


time_t uptime(void) {
    if (@available(iOS 10.0, *)) {
        return clock_gettime_nsec_np(CLOCK_MONOTONIC_RAW) / 1000000;
    } else {
        return CFAbsoluteTimeGetCurrent() * 1000;
    }
}


@implementation KTVSongConfiguration

+(KTVSongConfiguration*)configWithSongCode:(NSInteger)songCode
{
    KTVSongConfiguration* configs = [KTVSongConfiguration new];
    configs.songCode = songCode;
    return configs;
}

@end

@interface KTVApi ()<
AgoraRtcMediaPlayerDelegate,
AgoraMusicContentCenterEventDelegate,
AgoraRtcEngineDelegate,
KaraokeDelegate,
AgoraAudioFrameDelegate,
AgoraLrcDownloadDelegate
>

@property(nonatomic, weak)AgoraRtcEngineKit* engine;
@property(nonatomic, weak)AgoraMusicContentCenter* musicCenter;
@property(nonatomic, weak)id<AgoraMusicPlayerProtocol> rtcMediaPlayer;
@property (nonatomic, strong) NSMutableDictionary<NSString*, LyricCallback>* lyricCallbacks;
@property (nonatomic, strong) NSMutableDictionary<NSString*, LoadMusicCallback>* musicCallbacks;
@property (nonatomic, strong) NSMutableDictionary<NSString*, NSNumber*>* loadDict;
@property (nonatomic, strong) NSMutableDictionary<NSString*, NSString*>* lyricUrlDict;
@property (nonatomic, strong) AgoraRtcConnection* subChorusConnection;
@property (atomic, assign) BOOL pushDirectAudioEnable;
@property (nonatomic, strong) NSString* channelName;
@property (nonatomic, assign) NSInteger localPlayerPosition;
@property (nonatomic, assign) NSInteger remotePlayerPosition;
@property (nonatomic, assign) NSInteger remotePlayerDuration;
@property (nonatomic, assign) NSInteger audioPlayoutDelay;
@property (nonatomic, assign) NSInteger dataStreamId;
@property (nonatomic, strong) KTVSongConfiguration* config;

@property (nonatomic, assign) NSInteger playerDuration;

@property (nonatomic, assign) int playoutVolume;
@property (nonatomic, assign) int publishSignalVolume;
@property (nonatomic, assign) int chorusRemoteUserVolume;

@property (nonatomic, assign) AgoraMediaPlayerState playerState;
@property (nonatomic, assign) NSInteger totalCount;
@property (nonatomic, strong) AgoraDownLoadManager *downLoadManager;
@property (nonatomic, strong) NSTimer *timer;
@property (nonatomic, strong) LyricModel *lyricModel;
@property (nonatomic, assign) BOOL hasSendPreludeEndPosition;
@property (nonatomic, assign) BOOL hasSendEndPosition;
@property (nonatomic, assign) NSInteger totalLines;
@property (nonatomic, assign) double totalScore;
@property (nonatomic, assign) BOOL isPause;
@property (assign, nonatomic) double voicePitch;

@property (nonatomic, strong) NSString *currentLoadUrl;//当前正在请求的歌词url
@property (nonatomic, assign) AgoraAudioScenario audioScenario;
@end

@implementation KTVApi

-(id)initWithRtcEngine:(AgoraRtcEngineKit *)engine channel:(NSString*)channelName musicCenter:(AgoraMusicContentCenter*)musicCenter player:(nonnull id<AgoraMusicPlayerProtocol>)rtcMediaPlayer dataStreamId:(NSInteger)streamId delegate:(nonnull id<KTVApiDelegate>)delegate
{
    if (self = [super init]) {
        self.delegate = delegate;
        self.chorusRemoteUserVolume = 15;
        self.lyricCallbacks = [NSMutableDictionary dictionary];
        self.musicCallbacks = [NSMutableDictionary dictionary];
        self.loadDict = [NSMutableDictionary dictionary];
        self.lyricUrlDict = [NSMutableDictionary dictionary];
        
        // 调节本地播放音量。0-100
        [self adjustPlayoutVolume:100];
        // 调节远端用户听到的音量。0-400
        [self adjustPublishSignalVolume:100];
        
        self.engine = engine;
        self.channelName = channelName;
        self.dataStreamId = streamId;
        self.musicCenter = musicCenter;
        self.rtcMediaPlayer = rtcMediaPlayer;
        self.localPlayerPosition = uptime();
        
        //为了 尽量不超时 设置了1000ms
        [self.engine setParameters:@"{\"rtc.ntp_delay_drop_threshold\":1000}"];
        [self.engine setParameters:@"{\"rtc.enable_nasa2\": false}"];
//        [self.engine setParameters:@"{\"che.audio.agc.enable\": true}"];
        [self.engine setParameters:@"{\"rtc.video.enable_sync_render_ntp\": true}"];
        [self.engine setParameters:@"{\"rtc.net.maxS2LDelay\": 800}"];
        //        [self.engine setParameters:@"{\"che.audio.custom_bitrate\":128000}"];
        //        [self.engine setParameters:@"{\"che.audio.custom_payload_type\":78}"];
        
        //        [self.rtcMediaPlayer setPlayerOption:@"play_pos_change_callback" value:100];
        
        [[AppContext shared] registerEventDelegate:self];
        [[AppContext shared] registerPlayerEventDelegate:self];
        //        [self.engine setDirectExternalAudioSource:YES];
        //        [self.engine setAudioFrameDelegate:self];
        [engine setDirectExternalAudioSource:true];
        [engine setRecordingAudioFrameParametersWithSampleRate:48000 channel:2 mode:0 samplesPerCall:960];
        [engine setAudioFrameDelegate:self];
        self.lyricModel = nil;
        self.downLoadManager = [[AgoraDownLoadManager alloc]init];
        self.downLoadManager.delegate = self;
    }
    return self;
}

-(void)initTimer {
    if(self.timer){
        return;
    }
    kWeakSelf(self)
    self.timer = [NSTimer scheduledTimerWithTimeInterval: 0.02 block:^(NSTimer * _Nonnull timer) {
        NSInteger current = [weakself getPlayerCurrentTime];
        printf("recv1 current:   %ld  %ld %ld\n", current, self.playerState, self.config.songCode);
        [self setProgressWith:current];
        if(self.config.role == KTVSingRoleMainSinger){
            //如果超过前奏时间 自动隐藏前奏View
            if(!weakself.lyricModel){return;}
            if(current + 500 >= weakself.lyricModel.preludeEndPosition && weakself.hasSendPreludeEndPosition == false){
                if([weakself.delegate respondsToSelector:@selector(didSkipViewShowPreludeEndPosition)]){
                    [weakself.delegate didSkipViewShowPreludeEndPosition];
                    weakself.hasSendPreludeEndPosition = true;
                }
            } else if (current >= weakself.lyricModel.duration && weakself.hasSendEndPosition == false) {
                if([weakself.delegate respondsToSelector:@selector(didSkipViewShowEndDuration)]){
                    [weakself.delegate didSkipViewShowEndDuration];
                    weakself.hasSendEndPosition = true;
                }
            }
        }
    } repeats:true];
    
}

-(void)startTimer {
    if(self.isPause == false){
        [self.timer fire];
        NSLog(@"timer: startTimer---%ld", self.playerState);
    } else {
        [self resumeTimer];
    }
}

//恢复定时器
- (void)resumeTimer {
    if(self.isPause == false){return;};
    self.isPause = false;
    [self.timer setFireDate:[NSDate date]];
    NSLog(@"timer: resumeTimer---%ld", self.playerState);
}

//暂停定时器
-(void)pauseTimer {
    if(self.isPause){return;};
    self.isPause = true;
    [self.timer setFireDate:[NSDate distantFuture]];
    NSLog(@"timer: pauserTimer---%ld", self.playerState);
}

//释放定时器
-(void)freeTimer {
    if(self.timer){
        [self.timer invalidate];
        self.timer = nil;
    }
}

-(void)dealloc
{
    [self cancelAsyncTasks];
    [[AppContext shared] unregisterEventDelegate:self];
    [[AppContext shared] unregisterPlayerEventDelegate:self];
    [self.engine setAudioFrameDelegate:nil];
}

-(void)loadSong:(NSInteger)songCode withConfig:(nonnull KTVSongConfiguration *)config withCallback:(void (^ _Nullable)(NSInteger songCode, NSString* lyricUrl, KTVSingRole role, KTVLoadSongState state))block
{
    self.config = config;
    KTVSingRole role = config.role;
    [self initTimer];
    NSNumber* loadHistory = [self.loadDict objectForKey:[self songCodeString:songCode]];
    if(loadHistory) {
        KTVLoadSongState state = [loadHistory intValue];
        KTVLogInfo(@"song %ld load state exits %ld", songCode, state);
        if(state == KTVLoadSongStateOK) {
            VL(weakSelf);
            
            return [self setLrcLyric:[self cachedLyricUrl:songCode] withCallback:^(NSString *lyricUrl) {
                return block(songCode, [weakSelf cachedLyricUrl:songCode], role, state);
            }];
        } else if(state == KTVLoadSongStateInProgress) {
            //overwrite callback
            //TODO
            return;
        }
    }
    
    [self.loadDict setObject:[NSNumber numberWithInt:KTVLoadSongStateInProgress] forKey:[self songCodeString:songCode]];
    
    dispatch_group_t group = dispatch_group_create();
    __block KTVLoadSongState state = KTVLoadSongStateInProgress;
    
    VL(weakSelf);
    if(role == KTVSingRoleMainSinger) {
        dispatch_group_enter(group);
        dispatch_group_async(group, dispatch_get_main_queue(), ^{
            [weakSelf loadLyric:songCode withCallback:^(NSString *lyricUrl) {
                if (lyricUrl == nil) {
                    [weakSelf.loadDict removeObjectForKey:[weakSelf songCodeString:songCode]];
                    state = KTVLoadSongStateNoLyricUrl;
                    return dispatch_group_leave(group);
                }
                [weakSelf.lyricUrlDict setObject:lyricUrl forKey:[weakSelf songCodeString:songCode]];
                [weakSelf setLrcLyric:lyricUrl withCallback:^(NSString *lyricUrl) {
                    return dispatch_group_leave(group);
                }];
            }];
        });
        
        dispatch_group_enter(group);
        dispatch_group_async(group, dispatch_get_main_queue(), ^{
            [weakSelf loadMusic:songCode withCallback:^(AgoraMusicContentCenterPreloadStatus status){
                if (status != AgoraMusicContentCenterPreloadStatusOK) {
                    [weakSelf.loadDict removeObjectForKey:[weakSelf songCodeString:songCode]];
                    state = KTVLoadSongStatePreloadFail;
                    return dispatch_group_leave(group);
                }
                return dispatch_group_leave(group);
            }];
        });
    } else if(role == KTVSingRoleCoSinger) {
        dispatch_group_enter(group);
        dispatch_group_async(group, dispatch_get_main_queue(), ^{
            [weakSelf loadLyric:songCode withCallback:^(NSString *lyricUrl) {
                if (lyricUrl == nil) {
                    [weakSelf.loadDict removeObjectForKey:[weakSelf songCodeString:songCode]];
                    state = KTVLoadSongStateNoLyricUrl;
                    return dispatch_group_leave(group);
                }
                [weakSelf.lyricUrlDict setObject:lyricUrl forKey:[weakSelf songCodeString:songCode]];
                [weakSelf setLrcLyric:lyricUrl withCallback:^(NSString *lyricUrl) {
                    return dispatch_group_leave(group);
                }];
            }];
        });
        
        dispatch_group_enter(group);
        dispatch_group_async(group, dispatch_get_main_queue(), ^{
            [weakSelf loadMusic:songCode withCallback:^(AgoraMusicContentCenterPreloadStatus status){
                if (status != AgoraMusicContentCenterPreloadStatusOK) {
                    [weakSelf.loadDict removeObjectForKey:[weakSelf songCodeString:songCode]];
                    state = KTVLoadSongStatePreloadFail;
                    return dispatch_group_leave(group);
                }
                return dispatch_group_leave(group);
            }];
        });
    } else if(role == KTVSingRoleAudience) {
        dispatch_group_enter(group);
        dispatch_group_async(group, dispatch_get_main_queue(), ^{
            [weakSelf loadLyric:songCode withCallback:^(NSString *lyricUrl) {
                if (lyricUrl == nil) {
                    [weakSelf.loadDict removeObjectForKey:[weakSelf songCodeString:songCode]];
                    state = KTVLoadSongStateNoLyricUrl;
                    return dispatch_group_leave(group);
                }
                [weakSelf.lyricUrlDict setObject:lyricUrl forKey:[weakSelf songCodeString:songCode]];
                [weakSelf setLrcLyric:lyricUrl withCallback:^(NSString *lyricUrl) {
                    return dispatch_group_leave(group);
                }];
            }];
        });
    }
    
    
    dispatch_group_notify(group, dispatch_get_main_queue(), ^{
        if(state == KTVLoadSongStateInProgress) {
            [weakSelf.loadDict setObject:[NSNumber numberWithInt:KTVLoadSongStateOK] forKey:[weakSelf songCodeString:songCode]];
            state = KTVLoadSongStateOK;
            return block(songCode, [self cachedLyricUrl:songCode], role, state);
        }
        
        return block(songCode, [self cachedLyricUrl:songCode], role, state);
    });
}

-(void)playSong:(NSInteger)songCode
{
    KTVSingRole role = self.config.role;
    KTVSongType type = self.config.type;
    if(type == KTVSongTypeSolo) {
        if(role == KTVSingRoleMainSinger) {
            AgoraRtcChannelMediaOptions* options = [AgoraRtcChannelMediaOptions new];
            options.autoSubscribeAudio = YES;
            options.autoSubscribeVideo = YES;
            options.publishMediaPlayerId = [self.rtcMediaPlayer getMediaPlayerId];
            options.publishMediaPlayerAudioTrack = YES;
            [self.engine updateChannelWithMediaOptions:options];
            self.audioScenario = AgoraAudioScenarioChorus;
            
            KTVLogInfo(@"loadSong open music1 %ld", songCode);
            [self.rtcMediaPlayer openMediaWithSongCode:songCode startPos:0];
            [self.rtcMediaPlayer adjustPlayoutVolume:50];
            [self.rtcMediaPlayer adjustPublishSignalVolume:50];
        } else {
            AgoraRtcChannelMediaOptions* options = [AgoraRtcChannelMediaOptions new];
            options.autoSubscribeAudio = YES;
            options.autoSubscribeVideo = YES;
            options.publishMediaPlayerAudioTrack = NO;
            [self.engine updateChannelWithMediaOptions:options];
        }
    } else {
        if(role == KTVSingRoleMainSinger) {
            AgoraRtcChannelMediaOptions* options = [AgoraRtcChannelMediaOptions new];
            options.autoSubscribeAudio = YES;
            options.autoSubscribeVideo = YES;
            options.publishMediaPlayerId = [self.rtcMediaPlayer getMediaPlayerId];
            options.publishMediaPlayerAudioTrack = YES;
            options.publishMicrophoneTrack = YES;
            options.enableAudioRecordingOrPlayout = YES;
            [self.engine updateChannelWithMediaOptions:options];
            [self joinChorus2ndChannel];
            
            KTVLogInfo(@"loadSong open music2 %ld", songCode);
            [self.rtcMediaPlayer openMediaWithSongCode:songCode startPos:0];
            [self.rtcMediaPlayer adjustPlayoutVolume:50];
            [self.rtcMediaPlayer adjustPublishSignalVolume:50];
        } else if(role == KTVSingRoleCoSinger) {
            AgoraRtcChannelMediaOptions* options = [AgoraRtcChannelMediaOptions new];
            options.autoSubscribeAudio = YES;
            options.autoSubscribeVideo = YES;
            //co singer do not publish media player
            options.publishMicrophoneTrack = YES;
            options.publishMediaPlayerAudioTrack = NO;
            [self.engine updateChannelWithMediaOptions:options];
            [self joinChorus2ndChannel];
            
            //mute main Singer player audio
            [self.engine muteRemoteAudioStream:self.config.mainSingerUid mute:YES];
            
            KTVLogInfo(@"loadSong open music3 %ld", songCode);
            [self.rtcMediaPlayer openMediaWithSongCode:songCode startPos:0];
            [self.rtcMediaPlayer adjustPlayoutVolume:50];
            [self.rtcMediaPlayer adjustPublishSignalVolume:50];
        } else {
            AgoraRtcChannelMediaOptions* options = [AgoraRtcChannelMediaOptions new];
            options.autoSubscribeAudio = YES;
            options.autoSubscribeVideo = YES;
            options.publishMediaPlayerAudioTrack = NO;
            [self.engine updateChannelWithMediaOptions:options];
        }
    }
}

-(void)resumePlay
{
    if ([self.rtcMediaPlayer getPlayerState] == AgoraMediaPlayerStatePaused) {
        [self.rtcMediaPlayer resume];
    } else {
        [self.rtcMediaPlayer play];
    }
    
}

-(void)pausePlay
{
    [self.rtcMediaPlayer pause];
}

-(void)stopSong
{
    KTVLogInfo(@"stop song");
    [self.rtcMediaPlayer stop];
    [self cancelAsyncTasks];
    if(self.config.type == KTVSongTypeChorus) {
        [self leaveChorus2ndChannel];
    }
    [self.karaokeView reset];
    self.config = nil;
    [self.engine setAudioScenario:AgoraAudioScenarioGameStreaming];
    self.audioScenario = AgoraAudioScenarioGameStreaming;
}

-(void)Skip {
    if(self.rtcMediaPlayer.getPosition < self.lyricModel.preludeEndPosition - 500){//跳过前奏
        [self SkipPrelude];
    } else if (self.rtcMediaPlayer.getPosition > self.lyricModel.duration - 500) { //跳过前奏
        [self SkipTheEpilogue];
    }
}

//跳过前奏
-(void)SkipPrelude{
    [self.rtcMediaPlayer seekToPosition:self.lyricModel.preludeEndPosition];
}

//跳过前奏
-(void)SkipTheEpilogue{
    [self.rtcMediaPlayer seekToPosition:self.rtcMediaPlayer.getDuration - 500];
}

-(void)setProgressWith:(NSInteger)progress {
    kWeakSelf(self)
    dispatch_async(dispatch_get_main_queue(), ^{
        [weakself.karaokeView setPitchWithPitch:self.voicePitch progress:progress];
    });
}

-(void)selectTrackMode:(KTVPlayerTrackMode)mode
{
    [self.rtcMediaPlayer selectAudioTrack:mode == KTVPlayerTrackOrigin ? 0 : 1];
    //    [self syncTrackMode:mode];
}

- (void)adjustPlayoutVolume:(int)volume {
    self.playoutVolume = volume;
    [self.rtcMediaPlayer adjustPlayoutVolume:volume];
}

- (void)adjustPublishSignalVolume:(int)volume {
    self.publishSignalVolume = volume;
    [self.rtcMediaPlayer adjustPublishSignalVolume:volume];
}

- (void)adjustChorusRemoteUserPlaybackVoulme:(int)volume {
    self.chorusRemoteUserVolume = volume;
    
    [self updateRemotePlayBackVolumeIfNeed];
}

#pragma mark private
- (void)updateCosingerPlayerStatusIfNeed {
    if (self.config.type == KTVSongTypeChorus && self.config.role == KTVSingRoleCoSinger) {
        switch (self.playerState) {
            case AgoraMediaPlayerStatePaused:
                [self pausePlay];
                break;
            case AgoraMediaPlayerStateStopped:
                //                case AgoraMediaPlayerStatePlayBackAllLoopsCompleted:
                [self stopSong];
                break;
            case AgoraMediaPlayerStatePlaying:
                [self resumePlay];
                break;
            default:
                break;
        }
    }
}

- (void)updateRemotePlayBackVolumeIfNeed {
    if (self.config.role == KTVSongTypeUnknown || self.config.role == KTVSingRoleAudience) {
        KTVLogInfo(@"updateRemotePlayBackVolumeIfNeed: %d, role: %ld", 100, self.config.role);
        [self.engine adjustPlaybackSignalVolume:100];
        return;
    }
    
    /*
     合唱的时候，建议把接受远端人声的音量降低（建议是25或者50，后续这个值可以根据 端到端延迟来自动确认，比如150ms内可以50。 否则音量25），相应的api是adjustUserPlaybackSignalVolume(remoteUid, volume)；一是可以解决在aec nlp等级降低的情况下出现小音量回声问题，二是可以减小远端固有延迟的合唱者声音给本地k歌带来的影响
     */
    int volume = self.playerState == AgoraMediaPlayerStatePlaying ? self.chorusRemoteUserVolume : 100;
    KTVLogInfo(@"updateRemotePlayBackVolumeIfNeed: %d, role: %ld", volume, self.config.role);
    if (self.config.role == KTVSingRoleMainSinger) {
        [self.engine adjustPlaybackSignalVolume:volume];
    } else if (self.config.role == KTVSingRoleCoSinger) {
        [self.engine adjustPlaybackSignalVolume:volume];
        //        if (self.subChorusConnection == nil) {
        //            KTVLogWarn(@"updateRemotePlayBackVolumeIfNeed fail, connection = nil");
        //            return;
        //        }
        //        int uid = [VLLoginModel mediaPlayerUidWithUid:[NSString stringWithFormat:@"%ld", self.config.mainSingerUid]];
        //        [self.engine adjustUserPlaybackSignalVolumeEx:uid volume:volume connection:self.subChorusConnection];
    }
}

- (NSInteger)getNtpTimeInMs {
    NSInteger localNtpTime = [self.engine getNtpTimeInMs];
    if (localNtpTime != 0) {
        localNtpTime -= 2208988800 * 1000;
    } else {
        localNtpTime = round([[NSDate date] timeIntervalSince1970] * 1000.0);
    }
    return localNtpTime;
}

#pragma mark - rtc delgate proxies
- (void)mainRtcEngine:(AgoraRtcEngineKit *)engine didJoinedOfUid:(NSUInteger)uid elapsed:(NSInteger)elapsed
{
    KTVLogInfo(@"didJoinedOfUid: %ld", uid);
    //    if(self.config.type == KTVSongTypeChorus &&
    //       self.config.role == KTVSingRoleCoSinger &&
    //       uid == self.config.mainSingerUid) {
    //        [self.engine muteRemoteAudioStream:uid mute:YES];
    //    }
}

- (void)mainRtcEngine:(AgoraRtcEngineKit *)engine receiveStreamMessageFromUid:(NSUInteger)uid streamId:(NSInteger)streamId data:(NSData *)data
{
    NSNumber* loadHistory = [self.loadDict objectForKey:[self songCodeString:self.config.songCode]];
    KTVLoadSongState state = [loadHistory intValue];
    if (KTVLoadSongStateOK != state) {
        KTVLogWarn(@"recv data break, load song state: %ld", state);
        return;
    }
    
    NSDictionary *dict = [VLGlobalHelper dictionaryForJsonData:data];
    if (self.config.role == KTVSingRoleMainSinger) {
        KTVLogWarn(@"recv %@ cmd invalid", dict[@"cmd"]);
        return;
    }
    if ([dict[@"cmd"] isEqualToString:@"setLrcTime"]) {  //同步歌词
        NSInteger position = [dict[@"time"] integerValue];
        NSInteger duration = [dict[@"duration"] integerValue];
        NSInteger remoteNtp = [dict[@"ntp"] integerValue];
        double voicePitch = [dict[@"pitch"] doubleValue];
        AgoraMediaPlayerState state = [dict[@"playerState"] integerValue];
        if (self.playerState != state) {
            KTVLogInfo(@"recv state with setLrcTime : %ld", (long)state);
            self.playerState = state;
            [self updateCosingerPlayerStatusIfNeed];
            [self.delegate controller:self song:self.config.songCode didChangedToState:state local:NO];
        }

        self.remotePlayerPosition = uptime() - position;
        self.remotePlayerDuration = duration;
        //        KTVLogInfo(@"setLrcTime: %ld / %ld", self.remotePlayerPosition, self.remotePlayerDuration);
        if(self.config.type == KTVSongTypeChorus && self.config.role == KTVSingRoleCoSinger) {
            if([self.rtcMediaPlayer getPlayerState] == AgoraMediaPlayerStatePlaying) {
                NSInteger localNtpTime = [self getNtpTimeInMs];
                NSInteger localPosition = uptime() - self.localPlayerPosition;
                //                NSInteger localPosition2 = [self.rtcMediaPlayer getPosition];
                NSInteger expectPosition = position + localNtpTime - remoteNtp + self.audioPlayoutDelay;
                NSInteger threshold = expectPosition - localPosition;
                if(labs(threshold) > 40) {
                    KTVLogInfo(@"progress: setthreshold: %ld  expectPosition: %ld  position: %ld, localNtpTime: %ld, remoteNtp: %ld, audioPlayoutDelay: %ld, localPosition: %ld", threshold, expectPosition, position, localNtpTime, remoteNtp, self.audioPlayoutDelay, localPosition);
                    [self.rtcMediaPlayer seekToPosition:expectPosition];
                }
            }
        } else if (self.config.role == KTVSingRoleAudience) {
            self.voicePitch = voicePitch;
        }
        [self.delegate controller:self song:self.config.songCode config:self.config didChangedToPosition:position local:NO];
    } else if([dict[@"cmd"] isEqualToString:@"PlayerState"]) {
        AgoraMediaPlayerState state = [dict[@"state"] integerValue];
        KTVLogInfo(@"recv state with PlayerState: %ld, %@ %@", (long)state, dict[@"userId"], VLUserCenter.user.id);
        self.playerState = state;
        [self updateCosingerPlayerStatusIfNeed];
        
        [self.delegate controller:self song:self.config.songCode didChangedToState:state local:NO];
    } else if([dict[@"cmd"] isEqualToString:@"TrackMode"]) {
        
    } else if([dict[@"cmd"] isEqualToString:@"setVoicePitch"]) {
//        int pitch = [dict[@"pitch"] intValue];
        //伴唱显示自己分数
        if (self.config.type == KTVSongTypeChorus
            && self.config.role == KTVSingRoleCoSinger) {
            return;
        }
        int pitch = [dict[@"pitch"] intValue];
        NSInteger time = [dict[@"time"] integerValue];
        kWeakSelf(self)
        dispatch_async(dispatch_get_main_queue(), ^{
            [weakself.karaokeView setPitchWithPitch:pitch progress:time];
        });
        KTVLogInfo(@"receiveStreamMessageFromUid1 setVoicePitch: %ld", time);
    }
}

- (void)mainRtcEngine:(AgoraRtcEngineKit *)engine reportAudioVolumeIndicationOfSpeakers:(NSArray<AgoraRtcAudioVolumeInfo *> *)speakers totalVolume:(NSInteger)totalVolume
{
    if (self.playerState != AgoraMediaPlayerStatePlaying) {
        return;
    }
    
    if (self.config.role == KTVSingRoleAudience) {
        return;
    }
    
    double pitch = speakers.firstObject.voicePitch;
    self.voicePitch = pitch;
    NSDictionary *dict = @{
        @"cmd":@"setVoicePitch",
        @"pitch":@(pitch),
        @"time": @([self getPlayerCurrentTime])
    };
    [self sendStreamMessageWithDict:dict success:^(BOOL ifSuccess) {
    }];
    kWeakSelf(self)
    dispatch_async(dispatch_get_main_queue(), ^{
        [weakself.karaokeView setPitchWithPitch:pitch progress:[self getPlayerCurrentTime]];
    });
//   // [self.lrcView setVoicePitch:@[@(pitch)]];
//
//    if (self.config.role != KTVSingRoleMainSinger) {
//        return;
//    }
//
//    NSDictionary *dict = @{
//        @"cmd":@"setVoicePitch",
//        @"pitch":@(pitch),
//        @"time": @([self getPlayerCurrentTime])
//    };
//    [self sendStreamMessageWithDict:dict success:^(BOOL ifSuccess) {
//    }];
}


- (void)mainRtcEngine:(AgoraRtcEngineKit *)engine localAudioStats:(AgoraRtcLocalAudioStats *)stats {
    self.audioPlayoutDelay = stats.audioDeviceDelay;
}

#pragma mark - setter
- (void)setKaraokeView:(KaraokeView *)karaokeView{
    _karaokeView = karaokeView;
    _karaokeView.delegate = self;
}

- (void)onKaraokeViewWithView:(KaraokeView *)view didDragTo:(NSInteger)position{
    self.last = position;
    [self.rtcMediaPlayer seekToPosition:position];
    self.totalScore = [view.scoringView getCumulativeScore];
    if([self.delegate respondsToSelector:@selector(didlrcViewDidScrolledWithCumulativeScore:totalScore:)]){
        [self.delegate didlrcViewDidScrolledWithCumulativeScore:self.totalScore totalScore:self.totalCount*100];
    }
    
}

- (void)onKaraokeViewWithView:(KaraokeView *)view didFinishLineWith:(LyricLineModel *)model score:(NSInteger)score cumulativeScore:(NSInteger)cumulativeScore lineIndex:(NSInteger)lineIndex lineCount:(NSInteger)lineCount{
    self.totalLines = lineCount;
    self.totalScore = cumulativeScore;
    if([self.delegate respondsToSelector:@selector(didlrcViewDidScrollFinishedWithCumulativeScore:totalScore:lineScore:)]){
        [self.delegate didlrcViewDidScrollFinishedWithCumulativeScore:self.totalScore
                                                           totalScore:lineCount * 100
                                                            lineScore:score];
    }
}

- (void)setPlayerState:(AgoraMediaPlayerState)playerState {
    _playerState = playerState;
    [self updateRemotePlayBackVolumeIfNeed];

    [self updateTimeWithState:playerState];
}

-(void)updateTimeWithState:(AgoraMediaPlayerState)playerState {
    NSLog(@"playerState: %ld", playerState);
    VL(weakSelf);
    dispatch_async(dispatch_get_main_queue(), ^{
        switch (playerState) {
            case AgoraMediaPlayerStatePaused:
                [weakSelf pauseTimer];
                break;
            case AgoraMediaPlayerStateStopped:
                //                case AgoraMediaPlayerStatePlayBackAllLoopsCompleted:
                [weakSelf pauseTimer];
                break;
            case AgoraMediaPlayerStatePlaying:
                [weakSelf startTimer];
                break;
            default:
                break;
        }
    });
}

- (void)setAudioScenario:(AgoraAudioScenario)audioScenario {
    _audioScenario = audioScenario;
    [self.engine setAudioScenario:audioScenario];
    KTVLogInfo(@"setAudioScenario: %ld", audioScenario);
}

#pragma mark - AgoraAudioFrameDelegate
- (BOOL)onRecordAudioFrame:(AgoraAudioFrame *)frame channelId:(NSString *)channelId
{
    //    KTVLogInfo(@"onRecordAudioFrame: %@", frame);
    if(self.pushDirectAudioEnable) {
        [self.engine pushDirectAudioFrameRawData:frame.buffer samples:frame.channels*frame.samplesPerChannel sampleRate:frame.samplesPerSec channels:frame.channels];
    }
    return true;
}

- (AgoraAudioFramePosition)getObservedAudioFramePosition {
    return AgoraAudioFramePositionRecord;
}

- (AgoraAudioParams*)getRecordAudioParams {
    AgoraAudioParams* params = [AgoraAudioParams new];
    params.channel = 2;
    params.samplesPerCall = 960;
    params.sampleRate = 48000;
    params.mode = AgoraAudioRawFrameOperationModeReadOnly;
    return params;
}

#pragma mark - AgoraRtcMediaPlayerDelegate
-(void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol>)playerKit didChangedToState:(AgoraMediaPlayerState)state error:(AgoraMediaPlayerError)error
{
    if (state == AgoraMediaPlayerStateOpenCompleted) {
        KTVLogInfo(@"loadSong play completed %ld", self.config.songCode);
        self.localPlayerPosition = uptime();
        self.playerDuration = 0;
        if (self.config.role == KTVSingRoleMainSinger) {
            //主唱播放，通过同步消息“setLrcTime”通知伴唱play
            [playerKit play];
        }
    } else if (state == AgoraMediaPlayerStateStopped) {
        self.localPlayerPosition = uptime();
        self.playerDuration = 0;
        self.remotePlayerPosition = uptime() - 0;
    } else if (state == AgoraMediaPlayerStatePlaying) {
        self.localPlayerPosition = uptime() - [self.rtcMediaPlayer getPosition];
        self.playerDuration = 0;
    } else if (state == AgoraMediaPlayerStatePaused) {
        self.remotePlayerPosition = uptime();
    } else if (state == AgoraMediaPlayerStatePlaying) {
        self.localPlayerPosition = uptime() - [self.rtcMediaPlayer getPosition];
        self.playerDuration = 0;
        self.remotePlayerPosition = uptime();
    }
    if (self.config.role == KTVSingRoleMainSinger) {
        [self syncPlayState:state];
    }
    self.playerState = state;
    KTVLogInfo(@"recv state with player callback : %ld", (long)state);
    [self.delegate controller:self song:self.config.songCode didChangedToState:state local:YES];
}

-(void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol>)playerKit didChangedToPosition:(NSInteger)position
{
    self.localPlayerPosition = uptime() - position;
    if (self.config.role == KTVSingRoleMainSinger && position > self.audioPlayoutDelay) {
        if (self.config.role == KTVSingRoleMainSinger) {
            //if i am main singer
            NSDictionary *dict = @{
                @"cmd":@"setLrcTime",
                @"duration":@(self.playerDuration),
                @"time":@(position - self.audioPlayoutDelay),   //不同机型delay不同，需要发送同步的时候减去发送机型的delay，在接收同步加上接收机型的delay
                @"ntp":@([self getNtpTimeInMs]),
                @"pitch": @(self.voicePitch),
                @"playerState":@(self.playerState)
            };
            [self sendStreamMessageWithDict:dict success:nil];
        }
        [self.delegate controller:self song:self.config.songCode config:self.config didChangedToPosition:position local:YES];
    }
}

#pragma mark - AgoraLrcViewDelegate
-(NSTimeInterval)getTotalTime {
    if (self.config.role == KTVSingRoleMainSinger) {
        NSTimeInterval time = self.playerDuration;
        return time;
    }
    return self.remotePlayerDuration;
}

- (NSTimeInterval)getPlayerCurrentTime {
    if (self.config.role == KTVSingRoleMainSinger || self.config.role == KTVSingRoleCoSinger) {
        NSTimeInterval time = uptime() - self.localPlayerPosition;
        return time;
    }
    
    return uptime() - self.remotePlayerPosition;
}

- (NSInteger)playerDuration {
    if (_playerDuration == 0) {
        //只在特殊情况(播放、暂停等)调用getDuration(会耗时)
        _playerDuration = [_rtcMediaPlayer getDuration];
    }
    
    return _playerDuration;
}

#pragma mark - AgoraLrcDownloadDelegate
- (void)downloadLrcFinishedWithUrl:(NSString *)url {
    KTVLogInfo(@"download lrc finished %@",url);
    
    LyricCallback callback = [self.lyricCallbacks objectForKey:url];
    if(!callback) {
        return;
    }
    [self.lyricCallbacks removeObjectForKey:url];
    
    callback(url);
}

- (void)downloadLrcErrorWithUrl:(NSString *)url error:(NSError *)error {
    KTVLogInfo(@"download lrc fail %@: %@",url,error);
    
    LyricCallback callback = [self.lyricCallbacks objectForKey:url];
    if(!callback) {
        return;
    }
    [self.lyricCallbacks removeObjectForKey:url];
    
    callback(nil);
}

#pragma mark AgoraMusicContentCenterEventDelegate
- (void)onLyricResult:(nonnull NSString *)requestId
             lyricUrl:(nonnull NSString *)lyricUrl {
    LyricCallback callback = [self.lyricCallbacks objectForKey:requestId];
    if(!callback) {
        return;
    }
    [self.lyricCallbacks removeObjectForKey:requestId];
    
    if ([lyricUrl length] == 0) {
        callback(nil);
        return;
    }
    
    callback(lyricUrl);
}

- (void)onMusicChartsResult:(nonnull NSString *)requestId
                     status:(AgoraMusicContentCenterStatusCode)status
                     result:(nonnull NSArray<AgoraMusicChartInfo *> *)result {
}

- (void)onMusicCollectionResult:(nonnull NSString *)requestId
                         status:(AgoraMusicContentCenterStatusCode)status
                         result:(nonnull AgoraMusicCollection *)result {
}

- (void)onPreLoadEvent:(NSInteger)songCode
               percent:(NSInteger)percent
                status:(AgoraMusicContentCenterPreloadStatus)status
                   msg:(nonnull NSString *)msg
              lyricUrl:(nonnull NSString *)lyricUrl {
    if (status == AgoraMusicContentCenterPreloadStatusPreloading) {
        return;
    }
    NSString* sSongCode = [NSString stringWithFormat:@"%ld", songCode];
    LoadMusicCallback block = [self.musicCallbacks objectForKey:sSongCode];
    if(!block) {
        return;
    }
    [self.musicCallbacks removeObjectForKey:sSongCode];
    block(status);
}

#pragma RTC delegate for chorus channel2
//-(void)rtcEngine:(AgoraRtcEngineKit *)engine didJoinChannel:(NSString *)channel withUid:(NSUInteger)uid elapsed:(NSInteger)elapsed
//{
//    KTVLogInfo(@"KTVAPI didJoinChannel: %ld, %@", uid, channel);
//    [self.engine setAudioScenario:AgoraAudioScenarioChorus];
//}

//-(void)rtcEngine:(AgoraRtcEngineKit *)engine didLeaveChannelWithStats:(AgoraChannelStats *)stats
//{
//    self.audioScenario = AgoraAudioScenarioGameStreaming;
//}

#pragma private apis
//发送流消息
- (void)sendStreamMessageWithDict:(NSDictionary *)dict
                          success:(_Nullable sendStreamSuccess)success {
    //    VLLog(@"sendStremMessageWithDict:::%@",dict);
    NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
    
    int code = [self.engine sendStreamMessage:self.dataStreamId
                                         data:messageData];
    if (code == 0 && success) {
        success(YES);
    }
    if (code != 0) {
        KTVLogError(@"sendStreamMessage fail: %d\n",code);
    };
}

- (void)syncPlayState:(AgoraMediaPlayerState)state {
    NSDictionary *dict = @{
        @"cmd":@"PlayerState",
        @"userId": VLUserCenter.user.id,
        @"state": [NSString stringWithFormat:@"%ld", state]
    };
    [self sendStreamMessageWithDict:dict success:nil];
}

- (void)syncTrackMode:(KTVPlayerTrackMode)mode {
    NSDictionary *dict = @{
        @"cmd":@"TrackMode",
        @"value":[NSString stringWithFormat:@"%ld", mode]
    };
    [self sendStreamMessageWithDict:dict success:nil];
}


- (void)joinChorus2ndChannel
{
    if(self.subChorusConnection) {
        KTVLogWarn(@"joinChorus2ndChannel fail! rejoin!");
        return;
    }
    
    KTVSingRole role = self.config.role;
    AgoraRtcChannelMediaOptions* options = [AgoraRtcChannelMediaOptions new];
    // main singer do not subscribe 2nd channel
    // co singer auto sub
    options.autoSubscribeAudio = role == KTVSingRoleMainSinger ? NO : YES;
    options.autoSubscribeVideo = NO;
    options.publishMicrophoneTrack = NO;
    //co singer record & playout
    options.enableAudioRecordingOrPlayout = role == KTVSingRoleMainSinger ? NO : YES;
    options.clientRoleType = AgoraClientRoleBroadcaster;
    options.publishDirectCustomAudioTrack = role == KTVSingRoleMainSinger ? YES : NO;;
    
    AgoraRtcConnection* connection = [AgoraRtcConnection new];
    connection.channelId = [NSString stringWithFormat:@"%@_ex", self.channelName];
    connection.localUid = [VLLoginModel mediaPlayerUidWithUid:VLUserCenter.user.id];//VLUserCenter.user.agoraPlayerRTCUid;
    self.subChorusConnection = connection;
    
    KTVLogInfo(@"will joinChannelExByToken: channelId: %@, enableAudioRecordingOrPlayout: %d, role: %ld", connection.channelId, options.enableAudioRecordingOrPlayout, role);
    VL(weakSelf);
    [self.engine setDirectExternalAudioSource:YES];
    [self.engine setAudioFrameDelegate:self];
    self.audioScenario = AgoraAudioScenarioChorus;
    int ret =
    [self.engine joinChannelExByToken:VLUserCenter.user.agoraPlayerRTCToken connection:connection delegate:self mediaOptions:options joinSuccess:^(NSString * _Nonnull channel, NSUInteger uid, NSInteger elapsed) {
        KTVLogInfo(@"joinChannelExByToken success: channel: %@, uid: %ld", channel, uid);
        
        if(weakSelf.config.type == KTVSongTypeChorus &&
           weakSelf.config.role == KTVSingRoleMainSinger) {
            //fix pushDirectAudioFrameRawData frozen
            weakSelf.pushDirectAudioEnable = YES;
        }
        
        [weakSelf updateRemotePlayBackVolumeIfNeed];
    }];
    if(ret != 0) {
        KTVLogError(@"joinChannelExByToken status: %d channelId: %@ uid: %ld, token:%@ ", ret, connection.channelId, connection.localUid, VLUserCenter.user.agoraPlayerRTCToken);
    }
}

- (void)leaveChorus2ndChannel
{
    if(self.subChorusConnection == nil) {
        KTVLogWarn(@"leaveChorus2ndChannel fail connection = nil");
        return;
    }
    
    [self.engine setDirectExternalAudioSource:NO];
    [self.engine setAudioFrameDelegate:nil];
    KTVSingRole role = self.config.role;
    if(role == KTVSingRoleMainSinger) {
        AgoraRtcChannelMediaOptions* options = [AgoraRtcChannelMediaOptions new];
        options.publishDirectCustomAudioTrack = NO;
        [self.engine updateChannelExWithMediaOptions:options connection:self.subChorusConnection];
        [self.engine leaveChannelEx:self.subChorusConnection leaveChannelBlock:nil];
    } else if(role == KTVSingRoleCoSinger) {
        [self.engine leaveChannelEx:self.subChorusConnection leaveChannelBlock:nil];
        [self.engine muteRemoteAudioStream:self.config.mainSingerUid mute:NO];
    }
    
    [self adjustPlayoutVolume:self.playoutVolume];
    [self adjustPublishSignalVolume:self.publishSignalVolume];
    self.pushDirectAudioEnable = NO;
    self.subChorusConnection = nil;
}

- (void)cancelAsyncTasks
{
    [self.lyricCallbacks removeAllObjects];
    [self.musicCallbacks removeAllObjects];
}

- (void)setLrcLyric:(NSString*)url withCallback:(void (^ _Nullable)(NSString* lyricUrl))block
{
    BOOL taskExits = [self.lyricCallbacks objectForKey:url] != nil;
    if(!taskExits){
        //overwrite existing callback and use new
        [self.lyricCallbacks setObject:block forKey:url];
    }
    
    //重置每首歌的前奏尾奏发送状态
    self.hasSendPreludeEndPosition = false;
    self.hasSendEndPosition = false;
    self.totalLines = 0;
    self.totalScore = 0;
    self.currentLoadUrl = url;
    
    //判断歌词是zip文件还是本地地址
    bool isLocal = [url hasSuffix:@".zip"];
    [self.downLoadManager downloadLrcFileWithUrlString:url completion:^(NSString *lrcUrl) {
        
        //因为歌词下载是异步操作 需要特殊处理一下如果歌词还没下载下来。但是点击了下一首歌 会出现下载的是上一首的歌词。但是需要的是下一首的
        NSString *curStr = [self.currentLoadUrl componentsSeparatedByString:@"/"].lastObject;
        NSString *loadStr = [lrcUrl componentsSeparatedByString:@"/"].lastObject;
        NSString *curSongStr = [curStr componentsSeparatedByString:@"."].firstObject;
        NSString *loadSongStr = [loadStr componentsSeparatedByString:@"."].firstObject;
        if(![curSongStr isEqualToString:loadSongStr]){
            KTVLogInfo(@"load lrc is not equal to download lrc");
            return;
        }
        NSURL *musicUrl = isLocal ? [NSURL fileURLWithPath:lrcUrl] : [NSURL URLWithString:url];
        NSData *data = [NSData dataWithContentsOfURL:musicUrl];
        LyricModel *model = [KaraokeView parseLyricDataWithData:data];
        self.lyricModel = model;
        
        if([self.delegate respondsToSelector:@selector(didSongLoadedWith:)]){
            [self.delegate didSongLoadedWith:model];
        }
        
        if(model){
            [self.karaokeView setLyricDataWithData:model];
            self.totalCount = model.lines.count;
            block(url);
        } else {
            NSLog(@"歌词解析失败！");
        }
    } failure:^{
        
    }];
    
    
}

- (int)getAvgSongScore
{
    if(self.totalLines <= 0) {
        return 0;
    }
    else {
        return (int)(self.totalScore / self.totalLines);
    }
//    KTVLogInfo(@"setLrcLyric: %@", url);
//    [self.lrcView setLrcUrlWithUrl:url];
}

- (NSString*)cachedLyricUrl:(NSInteger)songCode
{
    return [self.lyricUrlDict objectForKey:[self songCodeString:songCode]];
}

- (NSString*)songCodeString:(NSInteger)songCode
{
    return [NSString stringWithFormat: @"%ld", songCode];
}

- (void)loadLyric:(NSInteger)songNo withCallback:(void (^ _Nullable)(NSString* lyricUrl))block {
    KTVLogInfo(@"loadLyric: %ld", songNo);
    NSString* requestId = [self.musicCenter getLyricWithSongCode:songNo lyricType:0];
    if ([requestId length] == 0) {
        if (block) {
            block(nil);
        }
        return;
    }
    [self.lyricCallbacks setObject:block forKey:requestId];
}

- (void)loadMusic:(NSInteger)songCode withCallback:(LoadMusicCallback)block {
    KTVLogInfo(@"loadMusic: %ld", songCode);
    NSInteger songCodeIntValue = songCode;
    NSInteger error = [self.musicCenter isPreloadedWithSongCode:songCodeIntValue];
    if(error == 0) {
        if(block) {
            [self.musicCallbacks removeObjectForKey:[self songCodeString:songCode]];
            block(AgoraMusicContentCenterPreloadStatusOK);
        }
        
        return;
    }
    
    error = [self.musicCenter preloadWithSongCode:songCodeIntValue jsonOption:nil];
    if (error != 0) {
        if(block) {
            [self.musicCallbacks removeObjectForKey:[self songCodeString:songCode]];
            block(AgoraMusicContentCenterPreloadStatusError);
        }
        return;
    }
    [self.musicCallbacks setObject:block forKey:[self songCodeString:songCode]];
}


@end



