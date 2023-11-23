//
//  HeadSetManager.m
//  AgoraEntScenarios
//
//  Created by CP on 2023/6/5.
//

#import "HeadSetManager.h"

@interface HeadSetManager ()

@property(nonatomic, copy) HeadsetStatusCallback headsetStatusCallBack;

@end
@implementation HeadSetManager

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self name:AVAudioSessionRouteChangeNotification object:nil];
}

+ (instancetype)initHeadsetObserverWithCallback:(HeadsetStatusCallback)callback {
    HeadSetManager *instance = [[HeadSetManager alloc] init];
    instance.headsetStatusCallBack = callback;
    return instance;
}

- (instancetype)init {
    if (self = [super init]) {
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(headsetChangeListener:)
                                                     name:AVAudioSessionRouteChangeNotification
                                                   object:[AVAudioSession sharedInstance]];
    }
    return self;
}

- (BOOL)hasHeadset {
    AVAudioSession *audioSession = [AVAudioSession sharedInstance];
    AVAudioSessionRouteDescription *currentRoute = audioSession.currentRoute;
    
    for (AVAudioSessionPortDescription *output in currentRoute.outputs) {
        if ([output.portType isEqualToString:AVAudioSessionPortHeadphones] || [output.portType isEqualToString:AVAudioSessionPortBluetoothA2DP] || [output.portType isEqualToString:AVAudioSessionPortBluetoothHFP]) {
            return YES;
        }
    }
    
    return NO;
}

- (void)headsetChangeListener:(NSNotification *)notification {
    NSDictionary *userInfo = notification.userInfo;
    UInt8 reasonValue = [[userInfo valueForKey:AVAudioSessionRouteChangeReasonKey] intValue];
    AVAudioSessionRouteChangeReason reason = reasonValue;
    
    switch (reason) {
        case AVAudioSessionRouteChangeReasonNewDeviceAvailable:
            if (self.headsetStatusCallBack != nil) {
                self.headsetStatusCallBack(YES);
            }
            break;
            
        case AVAudioSessionRouteChangeReasonOldDeviceUnavailable:
            if (self.headsetStatusCallBack != nil) {
                self.headsetStatusCallBack(NO);
            }
            break;
            
        default:
            break;
    }
}

@end
