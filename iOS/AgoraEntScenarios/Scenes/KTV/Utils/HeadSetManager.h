//
//  HeadSetManager.h
//  AgoraEntScenarios
//
//  Created by CP on 2023/6/5.
//

#import <AVFoundation/AVFoundation.h>

@interface HeadSetManager : NSObject

typedef void (^HeadsetStatusCallback)(BOOL inserted);

- (BOOL)hasHeadset;

+ (instancetype)initHeadsetObserverWithCallback:(HeadsetStatusCallback)callback;

@end

