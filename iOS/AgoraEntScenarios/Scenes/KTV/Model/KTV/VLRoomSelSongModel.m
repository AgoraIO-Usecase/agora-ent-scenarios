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
    
    if(self.chorusNum != object.chorusNum) {
        return NO;
    }
    
    return YES;
}

@end
