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


- (BOOL)readyToPlay {
    return !self.isChorus || (self.isChorus && [self.chorusNo length] > 0);
}

- (BOOL)waittingForChorusMatch {
    return self.isChorus && self.chorusNo.length == 0 && self.status == VLSongPlayStatusIdle;
}

- (BOOL)doneChorusMatch {
    return self.isChorus && self.chorusNo.length > 0 && self.status == VLSongPlayStatusPlaying;
}

@end
