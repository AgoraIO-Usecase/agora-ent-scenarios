//
//  HeadSetManager.m
//  AgoraEntScenarios
//
//  Created by CP on 2023/6/5.
//

#import "HeadSetManager.h"

@implementation HeadSetManager

static HeadsetStatusCallback headsetStatusCallBack;

+ (void)addHeadsetObserverWithCallback:(HeadsetStatusCallback)callback {
    headsetStatusCallBack = callback;
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(headsetChangeListener:)
                                                 name:AVAudioSessionRouteChangeNotification
                                               object:[AVAudioSession sharedInstance]];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(audioSessionInterruptionListener:) name:AVAudioSessionInterruptionNotification object:nil];

}

+ (BOOL)hasHeadset {
    
    AVAudioSession *audioSession = [AVAudioSession sharedInstance];
    AVAudioSessionRouteDescription *currentRoute = audioSession.currentRoute;
    
    for (AVAudioSessionPortDescription *output in currentRoute.outputs) {
        if ([output.portType isEqualToString:AVAudioSessionPortHeadphones] || [output.portType isEqualToString:AVAudioSessionPortBluetoothA2DP] || [output.portType isEqualToString:AVAudioSessionPortBluetoothHFP]) {
            return YES;
        }
    }
    
    return NO;
}

- (void)audioSessionInterruptionListener:(NSNotification *)notification {
    AVAudioSessionInterruptionType interruptionType = [notification.userInfo[AVAudioSessionInterruptionTypeKey] unsignedIntegerValue];
    
    if (interruptionType == AVAudioSessionInterruptionTypeBegan) {
        // 耳机断开
        if (headsetStatusCallBack != nil) {
            headsetStatusCallBack(NO);
        }
    } else if (interruptionType == AVAudioSessionInterruptionTypeEnded) {
        // 耳机插入
        if (headsetStatusCallBack != nil) {
            headsetStatusCallBack(YES);
        }
    }
}

+ (void)headsetChangeListener:(NSNotification *)notification {
    NSDictionary *userInfo = notification.userInfo;
    UInt8 reasonValue = [[userInfo valueForKey:AVAudioSessionRouteChangeReasonKey] intValue];
    AVAudioSessionRouteChangeReason reason = reasonValue;

    switch (reason) {
        case AVAudioSessionRouteChangeReasonNewDeviceAvailable:
            if (headsetStatusCallBack != nil) {
                headsetStatusCallBack(YES);
            }
            break;
            
        case AVAudioSessionRouteChangeReasonOldDeviceUnavailable:
            if (headsetStatusCallBack != nil) {
                headsetStatusCallBack(NO);
            }
            break;
            
        default:
            break;
    }
}

@end
