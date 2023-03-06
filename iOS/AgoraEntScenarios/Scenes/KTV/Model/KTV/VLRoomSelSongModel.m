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

- (BOOL)isSongCoSinger {
    if (self.isChorus && [self.chorusNo isEqualToString:VLUserCenter.user.id]) {
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

- (BOOL)isEqual:(VLRoomSelSongModel*)object {
    if (![self.songNo isEqualToString:object.songNo]) {
        return NO;
    }
    
    if (self.isChorus != object.isChorus) {
        return NO;
    }
    
    if (self.isSongOwner) {
        return YES;
    }
    
    NSString* origChorusNO = self.chorusNo ? self.chorusNo : @"";
    NSString* curChorusNO = object.chorusNo ? object.chorusNo : @"";
    if(![origChorusNO isEqualToString:curChorusNO]) {
        return NO;
    }
    
    return YES;
}

@end
