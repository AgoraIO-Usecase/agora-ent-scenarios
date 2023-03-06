//
//  VLLoginModel.m
//  VoiceOnLine
//

#import "VLLoginModel.h"

@implementation VLLoginModel
@synthesize extraDic = _extraDic;

+ (UInt32)mediaPlayerUidWithUid:(NSString*)uid {
    return 200000000 + [uid intValue];
}

- (UInt32)agoraPlayerRTCUid {
    return [[self class] mediaPlayerUidWithUid:self.id];
}

- (NSMutableDictionary*)extraDic {
    if (_extraDic == nil) {
        _extraDic = [NSMutableDictionary dictionaryWithCapacity:2];
    }
    
    return _extraDic;
}
@end
