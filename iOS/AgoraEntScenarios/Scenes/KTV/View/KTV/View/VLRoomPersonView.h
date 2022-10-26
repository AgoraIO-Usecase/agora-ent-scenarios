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
@class VLRoomSeatModel;
@protocol VLRoomPersonViewDelegate <NSObject>

@optional

- (void)seatItemClickAction:(VLRoomSeatModel *)model withIndex:(NSInteger)seatIndex;
//下麦
- (void)roomMasterMakePersonDropOnLineWithIndex:(NSInteger)seatIndex withDropType:(VLRoomSeatDropType)type;

- (BOOL)ifMyCameraIsOpened;

@end

@interface VLRoomPersonView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLRoomPersonViewDelegate>)delegate withRTCkit:(AgoraRtcEngineKit *)RTCkit;

@property (nonatomic, strong) NSMutableArray *roomSeatsArray;

- (void)updateSingBtnWithChoosedSongArray:(NSArray *)choosedSongArray;
- (void)updateSeatsByModel:(VLRoomSeatModel *)model;
- (void)setSeatsArray:(NSArray *)roomSeatsArray;
@end

