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

@interface KTVApi ()<
    AgoraRtcMediaPlayerDelegate,
    AgoraMusicContentCenterEventDelegate
>

@property(nonatomic, weak)AgoraRtcEngineKit* engine;
@property(nonatomic, weak)AgoraMusicContentCenter* musicCenter;
@property(nonatomic, weak)id<AgoraMusicPlayerProtocol> rtcMediaPlayer;
@property(nonatomic, assign)NSInteger openedSongCode;
@property (nonatomic, strong) NSMutableDictionary<NSString*, LyricCallback>* lyricCallbacks;
@property (nonatomic, strong) NSMutableDictionary<NSString*, LoadMusicCallback>* musicCallbacks;
@property (nonatomic, strong) NSMutableDictionary<NSString*, NSNumber*>* loadDict;
@property (nonatomic, strong) NSMutableDictionary<NSString*, NSString*>* lyricUrlDict;
@property(nonatomic, assign)NSInteger dataStreamId;

@end

@implementation KTVApi

-(id)initWithRtcEngine:(AgoraRtcEngineKit *)engine musicCenter:(AgoraMusicContentCenter*)musicCenter player:(nonnull id<AgoraMusicPlayerProtocol>)rtcMediaPlayer dataStreamId:(NSInteger)streamId delegate:(nonnull id<KTVApiDelegate>)delegate
{
    if (self = [super init]) {
        self.delegate = delegate;
        self.lyricCallbacks = [NSMutableDictionary dictionary];
        self.musicCallbacks = [NSMutableDictionary dictionary];
        self.loadDict = [NSMutableDictionary dictionary];
        self.lyricUrlDict = [NSMutableDictionary dictionary];
        
        self.engine = engine;
        self.dataStreamId = streamId;
        self.musicCenter = musicCenter;
        self.rtcMediaPlayer = rtcMediaPlayer;
        
        [[AppContext shared] registerEventDelegate:self];
        [[AppContext shared] registerPlayerEventDelegate:self];
    }
    return self;
}

-(void)dealloc
{
    [self cancelAsyncTasks];
    [[AppContext shared] unregisterEventDelegate:self];
    [[AppContext shared] unregisterPlayerEventDelegate:self];
}

-(void)loadSong:(NSInteger)songCode withSongType:(KTVSongType)type asRole:(KTVSingRole)role withCallback:(void (^ _Nullable)(NSInteger songCode, NSString* lyricUrl, KTVSingRole role, KTVLoadSongState state))block
{
    NSNumber* loadHistory = [self.loadDict objectForKey:[self songCodeString:songCode]];
    if(loadHistory) {
        KTVLoadSongState state = [loadHistory intValue];
        KTVLogInfo(@"song %ld load state exits %ld", songCode, state);
        if(state == KTVLoadSongStateOK) {
            return block(songCode, [self cachedLyricUrl:songCode], role, state);
        } else if(state == KTVLoadSongStateInProgress) {
            //overwrite callback
            //TODO
            return;
        }
    }

    [self.loadDict setObject:[NSNumber numberWithInt:KTVLoadSongStateInProgress] forKey:[self songCodeString:songCode]];

    VL(weakSelf);
    if(role == KTVSingRoleMainSinger) {
        [self loadLyric:songCode withCallback:^(NSString *lyricUrl) {
            if (lyricUrl == nil) {
                [weakSelf.loadDict removeObjectForKey:[weakSelf songCodeString:songCode]];
                return block(songCode, nil, role, KTVLoadSongStateNoLyricUrl);
            }
            [weakSelf.lyricUrlDict setObject:lyricUrl forKey:[self songCodeString:songCode]];
            [weakSelf loadMusic:songCode withCallback:^(AgoraMusicContentCenterPreloadStatus status){
                if (status != AgoraMusicContentCenterPreloadStatusOK) {
                    [weakSelf.loadDict removeObjectForKey:[weakSelf songCodeString:songCode]];
                    return block(songCode, lyricUrl, role, KTVLoadSongStatePreloadFail);
                }
                [weakSelf.loadDict setObject:[NSNumber numberWithInt:KTVLoadSongStateOK] forKey:[weakSelf songCodeString:songCode]];
                return block(songCode, lyricUrl, role, KTVLoadSongStateOK);
            }];
        }];
    } else if(role == KTVSingRoleAudience) {
        [self loadLyric:songCode withCallback:^(NSString *lyricUrl) {
            if (lyricUrl == nil) {
                [weakSelf.loadDict removeObjectForKey:[weakSelf songCodeString:songCode]];
                return block(songCode, nil, role, KTVLoadSongStateNoLyricUrl);
            }
            [weakSelf.lyricUrlDict setObject:lyricUrl forKey:[self songCodeString:songCode]];
            [weakSelf.loadDict setObject:[NSNumber numberWithInt:KTVLoadSongStateOK] forKey:[weakSelf songCodeString:songCode]];
            return block(songCode, lyricUrl, role, KTVLoadSongStateOK);
        }];
    }
}

-(void)playSong:(NSInteger)songCode withSongType:(KTVSongType)type asRole:(KTVSingRole)role
{
    if(role == KTVSingRoleMainSinger) {
        self.openedSongCode = songCode;
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
    self.openedSongCode = -1;
    [self cancelAsyncTasks];
}

-(void)selectTrackMode:(KTVPlayerTrackMode)mode
{
    [self.rtcMediaPlayer selectAudioTrack:mode == KTVPlayerTrackOrigin ? 0 : 1];
}

//发送流消息
- (void)sendStreamMessageWithDict:(NSDictionary *)dict
                         success:(_Nullable sendStreamSuccess)success {
//    VLLog(@"sendStremMessageWithDict:::%@",dict);
    NSData *messageData = [VLGlobalHelper compactDictionaryToData:dict];
    
    int code = [self.engine sendStreamMessage:self.dataStreamId
                                         data:messageData];
    if (code == 0 && success) {
        success(YES);
    } else{
//        VLLog(@"发送失败-streamId:%ld\n",streamId);
    };
}

#pragma mark - AgoraRtcMediaPlayerDelegate
-(void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol>)playerKit didChangedToState:(AgoraMediaPlayerState)state error:(AgoraMediaPlayerError)error
{
    if (state == AgoraMediaPlayerStateOpenCompleted) {
        [playerKit play];
    } else if (state == AgoraMediaPlayerStatePlaying
               || state == AgoraMediaPlayerStatePaused
               || state == AgoraMediaPlayerStateStopped
               || state == AgoraMediaPlayerStatePlayBackAllLoopsCompleted) {
        [self.delegate controller:self song:self.openedSongCode didChangedToState:state];
    }
}

-(void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol>)playerKit didChangedToPosition:(NSInteger)position
{
    [self.delegate controller:self song:self.openedSongCode didChangedToPosition:position];
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

#pragma private apis
- (void)cancelAsyncTasks
{
    [self.lyricCallbacks removeAllObjects];
    [self.musicCallbacks removeAllObjects];
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
