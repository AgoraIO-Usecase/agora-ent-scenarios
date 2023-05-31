//
//  VLRoomSetModel.m
//  VoiceOnLine
//

#import "VLSBGRoomSeatModel.h"

@implementation VLSBGRoomSeatModel

- (instancetype)init {
    if (self = [super init]) {
        self.chorusSongCode = @"";
    }
    return self;
}

- (void)resetWithInfo:(VLSBGRoomSeatModel*)seatInfo {
    self.isMaster = seatInfo ? seatInfo.isMaster : false;
    self.headUrl = seatInfo ? seatInfo.headUrl : @"";
    self.name = seatInfo ? seatInfo.name : @"";
    self.userNo = seatInfo ? seatInfo.userNo : @"";
    self.rtcUid = seatInfo.rtcUid;
    self.isAudioMuted = seatInfo ? seatInfo.isAudioMuted : 0;
    self.isVideoMuted = seatInfo ? seatInfo.isVideoMuted : 0;
    self.chorusSongCode = seatInfo.chorusSongCode;
}

//- (BOOL)isJoinChours {
//    return [self.chorusSongCode length] > 0;
//}
@end
