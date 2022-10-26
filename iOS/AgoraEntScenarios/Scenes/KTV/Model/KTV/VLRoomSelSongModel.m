//
//  VLRoomSelSongModel.m
//  VoiceOnLine
//

#import "VLRoomSelSongModel.h"
#import "VLUserCenter.h"

@implementation VLRoomSelSongModel

- (bool)isOwnSong {
    if ([self.userNo isEqualToString:VLUserCenter.user.userNo]) {
        return YES;
    }
    return NO;
}

@end
