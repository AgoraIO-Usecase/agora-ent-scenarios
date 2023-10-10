//
//  VLRoomPersonView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import <AgoraRtcKit/AgoraRtcKit.h>

typedef enum : NSUInteger {
    VLSBGRoomSeatDropTypeSelfly = 0,//自己下麦
    VLSBGRoomSeatDropTypeForceByRoomer //被房主强制下麦
    
} VLSBGRoomSeatDropType;
#define viewTag 99999
@class VLSBGRoomSeatModel, VLSBGMicSeatList;
@protocol VLSBGMicSeatListDelegate <NSObject>

- (void)onVLRoomPersonView:(VLSBGMicSeatList*)view seatItemTappedWithModel:(VLSBGRoomSeatModel *)model atIndex:(NSInteger)seatIndex;
- (void)onVLRoomPersonView:(VLSBGMicSeatList*)view onRenderVideo:(VLSBGRoomSeatModel*)model inView:(UIView*)videoView atIndex:(NSInteger)seatIndex;

@end

@interface VLSBGMicSeatList : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGMicSeatListDelegate>)delegate withRTCkit:(AgoraRtcEngineKit *)RTCkit;

@property (nonatomic, strong) NSArray *roomSeatsArray;


- (void)reloadSeatIndex:(NSUInteger)seatIndex;

- (void)updateSingBtnWithChoosedSongArray:(NSArray *)choosedSongArray;
- (void)updateIfNeeded;
@end

