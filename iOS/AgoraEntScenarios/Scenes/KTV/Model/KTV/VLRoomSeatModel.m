//
//  VLRoomSetModel.m
//  VoiceOnLine
//

#import "VLRoomSeatModel.h"

@implementation VLRoomSeatModel

- (instancetype)init {
    if (self = [super init]) {
    }
    return self;
}

- (void)resetWithInfo:(VLRoomSeatModel*)seatInfo {
    self.isMaster = seatInfo ? seatInfo.isMaster : false;
    self.headUrl = seatInfo ? seatInfo.headUrl : @"";
    self.name = seatInfo ? seatInfo.name : @"";
    self.userNo = seatInfo ? seatInfo.userNo : @"";
    self.rtcUid = seatInfo ? seatInfo.rtcUid : nil;
    self.isAudioMuted = seatInfo ? seatInfo.isAudioMuted : 0;
    self.isVideoMuted = seatInfo ? seatInfo.isVideoMuted : 0;
    self.isJoinedChorus = seatInfo ? seatInfo.isJoinedChorus : NO;
}

@end
