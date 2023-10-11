//
//  VLSBGRoomSelSongModel.m
//  VoiceOnLine
//

#import "VLSBGRoomSelSongModel.h"
#import "VLUserCenter.h"

@implementation VLSBGRoomSelSongModel

- (BOOL)isSongOwner {
    if ([self.userNo isEqualToString:VLUserCenter.user.id]) {
        return YES;
    }
    return NO;
}

- (NSString*)chorusSongId {
    return [NSString stringWithFormat:@"%@%lld", self.songNo, self.createAt];
}
@end
