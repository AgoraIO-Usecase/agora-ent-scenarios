//
//  VLSRBottomView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "VLHotSpotBtn.h"
#import "AgoraEntScenarios-Swift.h"
typedef enum : NSUInteger {
    VLSRBottomBtnClickTypeAudio = 0,       // 声音
    VLSRBottomBtnClickTypeVideo,           // 视频
    VLSRBottomBtnClickTypeMore,            // 更多
    VLSRBottomBtnClickTypeJoinChorus,           // 合唱
    VLSRBottomBtnClickTypeChoose,          // 点歌
    VLSRBottomBtnClickTypeLeaveChorus
} VLSRBottomBtnClickType;

NS_ASSUME_NONNULL_BEGIN
@class VLSRBottomToolbar;
@protocol VLSRBottomToolbarDelegate <NSObject>

- (void)onVLSRBottomView:(VLSRBottomToolbar*)view btnTapped:(id)sender withValues:(VLSRBottomBtnClickType)typeValue;

@end

@interface VLSRBottomToolbar : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRBottomToolbarDelegate>)delegate withRoomNo:(NSString *)roomNo withData:(NSArray <VLSRRoomSeatModel *> *)seatsArray;
-(void)setAudioBtnEnabled:(BOOL)enabled;
- (void)updateAudioBtn:(BOOL)audioMuted;
- (void)updateVideoBtn:(BOOL)videoMuted;
- (void)resetBtnStatus;

@end

NS_ASSUME_NONNULL_END
