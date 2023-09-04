//
//  VLRoomPersonView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import <AgoraRtcKit/AgoraRtcKit.h>

typedef enum : NSUInteger {
    VLRoomSeatDropTypeSelfly = 0,//自己下麦
    VLRoomSeatDropTypeForceByRoomer //被房主强制下麦
    
} VLSRRoomSeatDropType;
#define viewTag 99999
@class VLSRRoomSeatModel, VLSRMicSeatList;
@protocol VLSRMicSeatListDelegate <NSObject>

- (void)onVLRoomPersonView:(VLSRMicSeatList*)view seatItemTappedWithModel:(VLSRRoomSeatModel *)model atIndex:(NSInteger)seatIndex;
- (void)onVLRoomPersonView:(VLSRMicSeatList*)view onRenderVideo:(VLSRRoomSeatModel*)model inView:(UIView*)videoView atIndex:(NSInteger)seatIndex;

@end

@interface VLSRMicSeatList : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRMicSeatListDelegate>)delegate withRTCkit:(AgoraRtcEngineKit *)RTCkit;
@property (nonatomic, strong) UICollectionView *personCollectionView;
@property (nonatomic, strong) NSArray *roomSeatsArray;


- (void)reloadSeatIndex:(NSUInteger)seatIndex;

- (void)updateSingBtnWithChoosedSongArray:(NSArray *)choosedSongArray;
- (void)updateIfNeeded;
-(void)updateMicOwnerWith: (NSInteger)old new:(NSInteger)new;
@end

