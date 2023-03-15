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
NSString* kAgoraMediaPlayerKey = @"AgoraMediaPlayerKey";
NSString* kAgoraMccWeakTableKey = @"AgoraMccWeakTableKey";
NSString* kAgoraMpkWeakTableKey = @"AgoraMpkWeakTableKey";
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

- (void)setAgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerDelegate>)agoraRtcMediaPlayer {
    [[AppContext shared].extDic setValue:agoraRtcMediaPlayer forKey:kAgoraMediaPlayerKey];
}

- (id<AgoraRtcMediaPlayerDelegate>)agoraRtcMediaPlayer {
    return [[AppContext shared].extDic valueForKey:kAgoraMediaPlayerKey];
}

- (NSMapTable*)mccDelegateTable {
    NSMapTable* weakTable = [[AppContext shared].extDic valueForKey:kAgoraMccWeakTableKey];
    if (weakTable == nil) {
        weakTable = [NSMapTable mapTableWithKeyOptions:NSMapTableCopyIn valueOptions:NSMapTableWeakMemory];
        [[AppContext shared].extDic setValue:weakTable forKey:kAgoraMccWeakTableKey];
    }
    
    return weakTable;
}

- (NSMapTable*)mpkDelegateTable {
    NSMapTable* weakTable = [[AppContext shared].extDic valueForKey:kAgoraMpkWeakTableKey];
    if (weakTable == nil) {
        weakTable = [NSMapTable mapTableWithKeyOptions:NSMapTableCopyIn valueOptions:NSMapTableWeakMemory];
        [[AppContext shared].extDic setValue:weakTable forKey:kAgoraMpkWeakTableKey];
    }
    
    return weakTable;
}

- (NSArray<id<AgoraMusicContentCenterEventDelegate>>*)mccDelegateArray {
    return [[[self mccDelegateTable] objectEnumerator] allObjects];
}

- (NSArray<id<AgoraRtcMediaPlayerDelegate>>*)mpkDelegateArray {
    return [[[self mpkDelegateTable] objectEnumerator] allObjects];
}

- (void)registerEventDelegate:(id<AgoraMusicContentCenterEventDelegate>)delegate {
    NSString* key = [NSString stringWithFormat:@"%p", delegate];
    [[self mccDelegateTable] setObject:delegate forKey:key];
}

- (void)unregisterEventDelegate:(id<AgoraMusicContentCenterEventDelegate>)delegate {
    NSString* key = [NSString stringWithFormat:@"%p", delegate];
    [self.mccDelegateTable removeObjectForKey:key];
}

- (void)registerPlayerEventDelegate:(id<AgoraRtcMediaPlayerDelegate>)delegate {
    NSString* key = [NSString stringWithFormat:@"%p", delegate];
    [[self mpkDelegateTable] setObject:delegate forKey:key];
}

- (void)unregisterPlayerEventDelegate:(id<AgoraRtcMediaPlayerDelegate>)delegate {
    NSString* key = [NSString stringWithFormat:@"%p", delegate];
    [self.mpkDelegateTable removeObjectForKey:key];
}

#pragma mark service
+ (id<KTVServiceProtocol>)ktvServiceImp {
    id<KTVServiceProtocol> ktvServiceImp = [[AppContext shared].extDic valueForKey:kServiceImpKey];
    if (ktvServiceImp == nil) {
//        ktvServiceImp = [KTVServiceImp new];
        ktvServiceImp = [KTVSyncManagerServiceImp new];
        [[AppContext shared].extDic setValue:ktvServiceImp forKey:kServiceImpKey];
    }
    
    return ktvServiceImp;
}

+ (void)unloadServiceImp {
    [[AppContext shared].extDic removeAllObjects];
}


#pragma mark AgoraMusicContentCenterEventDelegate

- (void)onMusicChartsResult:(NSString *)requestId
                     status:(AgoraMusicContentCenterStatusCode)status
                     result:(NSArray<AgoraMusicChartInfo*> *)result {
    [[self mccDelegateArray] enumerateObjectsUsingBlock:^(id<AgoraMusicContentCenterEventDelegate>  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        [obj onMusicChartsResult:requestId
                          status:status
                          result:result];
    }];
}


- (void)onMusicCollectionResult:(NSString *)requestId
                         status:(AgoraMusicContentCenterStatusCode)status
                         result:(AgoraMusicCollection *)result {
    [[self mccDelegateArray] enumerateObjectsUsingBlock:^(id<AgoraMusicContentCenterEventDelegate>  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        [obj onMusicCollectionResult:requestId
                              status:status
                              result:result];
    }];
}

- (void)onLyricResult:(NSString*)requestId
             lyricUrl:(NSString*)lyricUrl {
    [[self mccDelegateArray] enumerateObjectsUsingBlock:^(id<AgoraMusicContentCenterEventDelegate>  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        [obj onLyricResult:requestId
                  lyricUrl:lyricUrl];
    }];
}

- (void)onPreLoadEvent:(NSInteger)songCode
               percent:(NSInteger)percent
                status:(AgoraMusicContentCenterPreloadStatus)status
                   msg:(NSString *)msg
              lyricUrl:(NSString *)lyricUrl {
    [[self mccDelegateArray] enumerateObjectsUsingBlock:^(id<AgoraMusicContentCenterEventDelegate>  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        [obj onPreLoadEvent:songCode
                    percent:percent
                     status:status
                        msg:msg
                   lyricUrl:lyricUrl];
    }];
}

#pragma mark AgoraRtcMediaPlayerDelegate
- (void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol>)playerKit didChangedToState:(AgoraMediaPlayerState)state error:(AgoraMediaPlayerError)error
{
    [[self mpkDelegateArray] enumerateObjectsUsingBlock:^(id<AgoraRtcMediaPlayerDelegate>  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        [obj AgoraRtcMediaPlayer:playerKit didChangedToState:state error:error];
    }];
}

-(void)AgoraRtcMediaPlayer:(id<AgoraRtcMediaPlayerProtocol>)playerKit didChangedToPosition:(NSInteger)position
{
    [[self mpkDelegateArray] enumerateObjectsUsingBlock:^(id<AgoraRtcMediaPlayerDelegate>  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        [obj AgoraRtcMediaPlayer:playerKit didChangedToPosition:position];
    }];
}
@end
