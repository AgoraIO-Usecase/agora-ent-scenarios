//
//  VLRoomSelSongModel.m
//  VoiceOnLine
//

#import "VLRoomSelSongModel.h"
#import "VLUserCenter.h"

@implementation VLRoomSelSongModel

- (BOOL)isSongOwner {
    if ([self.userNo isEqualToString:VLUserCenter.user.userNo]) {
        return YES;
    }
    return NO;
}

- (BOOL)waittingForChorus {
    return self.isChorus && self.chorusNo.length == 0;
}
@end
