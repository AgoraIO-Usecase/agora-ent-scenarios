//
//  VLLoginModel.m
//  VoiceOnLine
//

#import "VLLoginModel.h"

@implementation VLLoginModel
+ (UInt32)mediaPlayerUidWithUid:(NSString*)uid {
    return 200000000 + [uid intValue];
}

- (UInt32)agoraPlayerRTCUid {
    return [[self class] mediaPlayerUidWithUid:self.id];
}

@end
