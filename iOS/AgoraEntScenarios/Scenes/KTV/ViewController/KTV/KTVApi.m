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

typedef void (^LyricCallback)(NSString* lyricUrl);
typedef void (^LoadMusicCallback)(AgoraMusicContentCenterPreloadStatus);



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
    AgoraLrcViewDelegate,
    AgoraLrcDownloadDelegate,
    AgoraAudioFrameDelegate
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
@property (nonatomic, assign) NSInteger localPlayerSystemTime;
@property (nonatomic, assign) NSInteger remotePlayerPosition;
@property (nonatomic, assign) NSInteger remotePlayerDuration;
@property (nonatomic, assign) NSInteger dataStreamId;
@property (nonatomic, strong) KTVSongConfiguration* config;

@property (nonatomic, assign) AgoraMediaPlayerState playerState;
@end

@implementation KTVApi

-(id)initWithRtcEngine:(AgoraRtcEngineKit *)engine channel:(NSString*)channelName musicCenter:(AgoraMusicContentCenter*)musicCenter player:(nonnull id<AgoraMusicPlayerProtocol>)rtcMediaPlayer dataStreamId:(NSInteger)streamId delegate:(nonnull id<KTVApiDelegate>)delegate
{
    if (self = [super init]) {
        self.delegate = delegate;
        self.lyricCallbacks = [NSMutableDictionary dictionary];
        self.musicCallbacks = [NSMutableDictionary dictionary];
        self.loadDict = [NSMutableDictionary dictionary];
        self.lyricUrlDict = [NSMutableDictionary dictionary];
        
        self.engine = engine;
        self.channelName = channelName;
        self.dataStreamId = streamId;
        self.musicCenter = musicCenter;
        self.rtcMediaPlayer = rtcMediaPlayer;
        
        [[AppContext shared] registerEventDelegate:self];
        [[AppContext shared] registerPlayerEventDelegate:self];
        
        [engine setDirectExternalAudioSource:true];
        [engine setRecordingAudioFrameParametersWithSampleRate:48000 channel:2 mode:0 samplesPerCall:960];
        [engine setAudioFrameDelegate:self];
    }
    return self;
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
            [self.rtcMediaPlayer openMediaWithSongCode:songCode startPos:0];
            AgoraRtcChannelMediaOptions* options = [AgoraRtcChannelMediaOptions new];
            options.autoSubscribeAudio = YES;
            options.autoSubscribeVideo = YES;
            options.publishMediaPlayerId = [self.rtcMediaPlayer getMediaPlayerId];
            options.publishMediaPlayerAudioTrack = YES;
            [self.engine updateChannelWithMediaOptions:options];
        } else {
            AgoraRtcChannelMediaOptions* options = [AgoraRtcChannelMediaOptions new];
            options.autoSubscribeAudio = YES;
            options.autoSubscribeVideo = YES;
            options.publishMediaPlayerAudioTrack = NO;
            [self.engine updateChannelWithMediaOptions:options];
        }
    } else {
        if(role == KTVSingRoleMainSinger) {
            [self.rtcMediaPlayer openMediaWithSongCode:songCode startPos:0];
            AgoraRtcChannelMediaOptions* options = [AgoraRtcChannelMediaOptions new];
            options.autoSubscribeAudio = YES;
            options.autoSubscribeVideo = YES;
            options.publishMediaPlayerId = [self.rtcMediaPlayer getMediaPlayerId];
            options.publishMediaPlayerAudioTrack = YES;
            options.publishMicrophoneTrack = YES;
            [self.engine updateChannelWithMediaOptions:options];
            [self joinChorus2ndChannel];
        } else if(role == KTVSingRoleCoSinger) {
            [self.rtcMediaPlayer openMediaWithSongCode:songCode startPos:0];
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
    [self.rtcMediaPlayer resume];
}

-(void)pausePlay
{
    [self.rtcMediaPlayer pause];
}

-(void)stopSong
{
    [self.rtcMediaPlayer stop];
    [self cancelAsyncTasks];
    if(self.config.type == KTVSongTypeChorus) {
        [self leaveChorus2ndChannel];
    }
    [self.lrcView stop];
    [self.lrcView reset];
    self.config = nil;
}

-(void)selectTrackMode:(KTVPlayerTrackMode)mode
{
    [self.rtcMediaPlayer selectAudioTrack:mode == KTVPlayerTrackOrigin ? 0 : 1];
//    [self syncTrackMode:mode];
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

#pragma mark - rtc delgate proxies
- (void)mainRtcEngine:(AgoraRtcEngineKit *)engine didJoinedOfUid:(NSUInteger)uid elapsed:(NSInteger)elapsed
{
//    if(self.config.type == KTVSongTypeChorus &&
//       self.config.role == KTVSingRoleCoSinger &&
//       uid == self.config.mainSingerUid) {
//        [self.engine muteRemoteAudioStream:uid mute:YES];
//    }
}

- (void)mainRtcEngine:(AgoraRtcEngineKit *)engine receiveStreamMessageFromUid:(NSUInteger)uid streamId:(NSInteger)streamId data:(NSData *)data
{
    NSDictionary *dict = [VLGlobalHelper dictionaryForJsonData:data];
    if (self.config.role == KTVSingRoleMainSinger) {
        KTVLogWarn(@"recv %@ cmd invalid", dict[@"cmd"]);
        return;
    }
    if ([dict[@"cmd"] isEqualToString:@"setLrcTime"]) {  //同步歌词
        NSInteger position = [dict[@"time"] integerValue];
        NSInteger duration = [dict[@"duration"] integerValue];
        NSInteger remoteNtp = [dict[@"ntp"] integerValue];
        AgoraMediaPlayerState state = [dict[@"playerState"] integerValue];
        if (self.playerState != state) {
            KTVLogInfo(@"recv state with setLrcTime : %ld", (long)state);
            self.playerState = state;
            [self updateCosingerPlayerStatusIfNeed];
            
            [self.delegate controller:self song:self.config.songCode didChangedToState:state local:NO];
        }
        
        self.remotePlayerPosition = position;
        self.remotePlayerDuration = duration;
        if(self.config.type == KTVSongTypeChorus && self.config.role == KTVSingRoleCoSinger) {
            if([self.rtcMediaPlayer getPlayerState] == AgoraMediaPlayerStatePlaying) {
                NSInteger localNtpTime = [self.engine getNtpTimeInMs];
                NSInteger currentSystemTime = ([[NSDate date] timeIntervalSince1970] * 1000.0);
                NSInteger localPosition = currentSystemTime - self.localPlayerSystemTime + self.localPlayerPosition;
                NSInteger expectPosition = localNtpTime - remoteNtp + position;
                NSInteger diff = expectPosition - localPosition;
                if(labs(diff) > 40) {
                    self.localPlayerPosition = expectPosition;
                    [self.rtcMediaPlayer seekToPosition:expectPosition];
                }
            }
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
        int pitch = [dict[@"pitch"] intValue];
        NSInteger time = [dict[@"time"] integerValue];
        [self.lrcView setVoicePitch:@[@(pitch)]];
        KTVLogInfo(@"receiveStreamMessageFromUid1 setVoicePitch: %ld", time);
    }
}

- (void)mainRtcEngine:(AgoraRtcEngineKit *)engine reportAudioVolumeIndicationOfSpeakers:(NSArray<AgoraRtcAudioVolumeInfo *> *)speakers totalVolume:(NSInteger)totalVolume
{
    if (self.config.role != KTVSingRoleMainSinger
        || self.playerState != AgoraMediaPlayerStatePlaying) {
        return;
    }
    
    double pitch = speakers.firstObject.voicePitch;
    NSDictionary *dict = @{
        @"cmd":@"setVoicePitch",
        @"pitch":@(pitch),
        @"time": @([self.rtcMediaPlayer getPosition])
    };
    [self sendStreamMessageWithDict:dict success:^(BOOL ifSuccess) {
    }];
    
    [self.lrcView setVoicePitch:@[@(pitch)]];
}

#pragma mark - setter
- (void)setLrcView:(AgoraLrcScoreView *)lrcView
{
    _lrcView = lrcView;
    lrcView.downloadDelegate = self;
    lrcView.delegate = self;
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

#pragma mark - AgoraRtcMediaPlayerDelegate
-(void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol>)playerKit didChangedToState:(AgoraMediaPlayerState)state error:(AgoraMediaPlayerError)error
{
    if (state == AgoraMediaPlayerStateOpenCompleted) {
        [playerKit play];
    } else if (state == AgoraMediaPlayerStateStopped) {
        self.localPlayerPosition = 0;
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
    self.localPlayerPosition = position;
    self.localPlayerSystemTime = ([[NSDate date] timeIntervalSince1970] * 1000.0);
    
    if (self.config.role == KTVSingRoleMainSinger) {
        //if i am main singer
        NSDictionary *dict = @{
            @"cmd":@"setLrcTime",
            @"duration":@([self.rtcMediaPlayer getDuration]),
            @"time":@(position),
            @"ntp":@([self.engine getNtpTimeInMs]),
            @"playerState":@(self.playerState)
        };
        [self sendStreamMessageWithDict:dict success:nil];
    }
    
    [self.delegate controller:self song:self.config.songCode config:self.config didChangedToPosition:position local:YES];
}

#pragma mark - AgoraLrcViewDelegate
-(NSTimeInterval)getTotalTime {
    if (self.config.role == KTVSingRoleMainSinger) {
        NSTimeInterval time = [_rtcMediaPlayer getDuration];
        return time;
    }
    return self.remotePlayerDuration;
}

- (NSTimeInterval)getPlayerCurrentTime {
    if (self.config.role == KTVSingRoleMainSinger) {
        NSTimeInterval time = [_rtcMediaPlayer getPosition];
        return time;
    }
    
    return self.remotePlayerPosition;
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
-(void)rtcEngine:(AgoraRtcEngineKit *)engine didJoinChannel:(NSString *)channel withUid:(NSUInteger)uid elapsed:(NSInteger)elapsed
{
    KTVLogInfo(@"KTVAPI didJoinChannel: %ld, %@", uid, channel);
    [self.engine setAudioScenario:AgoraAudioScenarioChorus];
}

-(void)rtcEngine:(AgoraRtcEngineKit *)engine didLeaveChannelWithStats:(AgoraChannelStats *)stats
{
    [self.engine setAudioScenario:AgoraAudioScenarioGameStreaming];
}

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
    options.publishDirectCustomAudioTrack = YES;
    
    AgoraRtcConnection* connection = [AgoraRtcConnection new];
    connection.channelId = [NSString stringWithFormat:@"%@_ex", self.channelName];
    connection.localUid = VLUserCenter.user.agoraPlayerRTCUid;
    self.subChorusConnection = connection;
    
    KTVLogInfo(@"will joinChannelExByToken: channelId: %@, enableAudioRecordingOrPlayout: %d, role: %ld", connection.channelId, options.enableAudioRecordingOrPlayout, role);
    VL(weakSelf);
    int ret =
    [self.engine joinChannelExByToken:VLUserCenter.user.agoraPlayerRTCToken connection:connection delegate:self mediaOptions:options joinSuccess:^(NSString * _Nonnull channel, NSUInteger uid, NSInteger elapsed) {
        KTVLogInfo(@"joinChannelExByToken success: channel: %@, uid: %ld", channel, uid);
        
        if(weakSelf.config.type == KTVSongTypeChorus &&
           weakSelf.config.role == KTVSingRoleMainSinger) {
            //fix pushDirectAudioFrameRawData frozen
            weakSelf.pushDirectAudioEnable = YES;
        }
    }];
    if(ret != 0) {
        KTVLogError(@"joinChannelExByToken status: %d channelId: %@ uid: %ld, token:%@ ", ret, connection.channelId, connection.localUid, VLUserCenter.user.agoraPlayerRTCToken);
    }
}

- (void)leaveChorus2ndChannel
{
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
    self.pushDirectAudioEnable = NO;
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
    [self.lrcView setLrcUrlWithUrl:url];
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



