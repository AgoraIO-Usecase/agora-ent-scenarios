//
//  VLRoomPersonView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import <AgoraRtcKit/AgoraRtcKit.h>

typedef enum : NSUInteger {
    VLRoomSeatDropTypeSelfly = 0,//自己下麦
    VLRoomSeatDropTypeForceByRoomer //被房主强制下麦
    
} VLRoomSeatDropType;
#define viewTag 99999
@class VLRoomSeatModel, VLRoomPersonView;
@protocol VLRoomPersonViewDelegate <NSObject>

- (void)onVLRoomPersonView:(VLRoomPersonView*)view seatItemTappedWithModel:(VLRoomSeatModel *)model atIndex:(NSInteger)seatIndex;
- (void)onVLRoomPersonView:(VLRoomPersonView*)view onRenderVideo:(VLRoomSeatModel*)model inView:(UIView*)videoView atIndex:(NSInteger)seatIndex;

@end

@interface VLRoomPersonView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLRoomPersonViewDelegate>)delegate withRTCkit:(AgoraRtcEngineKit *)RTCkit;

@property (nonatomic, strong) NSArray *roomSeatsArray;

- (void)updateSingBtnWithChoosedSongArray:(NSArray *)choosedSongArray;
- (void)updateIfNeeded;
@end

