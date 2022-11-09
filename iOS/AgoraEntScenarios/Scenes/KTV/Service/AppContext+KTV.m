//
//  AppContext+KTV.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/21.
//

#import "AppContext+KTV.h"
#import "AgoraEntScenarios-Swift.h"

NSString* kServiceImpKey = @"ServiceImpKey";
NSString* kAgoraMusicContentCenterKey = @"AgoraMusicContentCenterKey";
NSString* kAgoraMccWeakTableKey = @"AgoraMccWeakTableKey";
@implementation AppContext (KTV)

#pragma mark mcc
+ (void)setupKtvConfig {
    [AppContext shared].sceneImageBundleName = @"KtvResource";
    [AppContext shared].sceneLocalizeBundleName = @"KtvResource";
}

- (void)setAgoraMcc:(AgoraMusicContentCenter *)agoraMcc {
    [[AppContext shared].extDic setValue:agoraMcc forKey:kAgoraMusicContentCenterKey];
}

- (AgoraMusicContentCenter*)agoraMcc {
    return [[AppContext shared].extDic valueForKey:kAgoraMusicContentCenterKey];
}

- (NSMapTable*)mccDelegateTable {
    NSMapTable* weakTable = [[AppContext shared].extDic valueForKey:kAgoraMccWeakTableKey];
    if (weakTable == nil) {
        weakTable = [NSMapTable mapTableWithKeyOptions:NSMapTableCopyIn valueOptions:NSMapTableWeakMemory];
        [[AppContext shared].extDic setValue:weakTable forKey:kAgoraMccWeakTableKey];
    }
    
    return weakTable;
}

- (NSArray<id<AgoraMusicContentCenterEventDelegate>>*)mccDelegateArray {
    return [[[self mccDelegateTable] objectEnumerator] allObjects];
}

- (void)registerEventDelegate:(id<AgoraMusicContentCenterEventDelegate>)delegate {
    NSString* key = [NSString stringWithFormat:@"%p", delegate];
    [[self mccDelegateTable] setObject:delegate forKey:key];
}

- (void)unregisterEventDelegate:(id<AgoraMusicContentCenterEventDelegate>)delegate {
    NSString* key = [NSString stringWithFormat:@"%p", delegate];
    [self.mccDelegateTable removeObjectForKey:key];
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
@end
