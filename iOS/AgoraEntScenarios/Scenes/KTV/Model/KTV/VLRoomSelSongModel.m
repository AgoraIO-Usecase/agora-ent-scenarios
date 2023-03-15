//
//  VLRoomSelSongModel.m
//  VoiceOnLine
//

#import "VLRoomSelSongModel.h"
#import "VLUserCenter.h"

@implementation VLRoomSelSongModel

- (BOOL)isSongOwner {
    if ([self.userNo isEqualToString:VLUserCenter.user.id]) {
        return YES;
    }
    return NO;
}

- (BOOL)isChorus {
    return self.chorusNum > 0;
}

@end
