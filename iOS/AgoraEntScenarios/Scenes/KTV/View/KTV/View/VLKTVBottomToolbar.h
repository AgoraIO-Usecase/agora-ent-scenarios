//
//  VLKTVBottomView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "VLHotSpotBtn.h"
#import "AgoraEntScenarios-swift.h"
typedef enum : NSUInteger {
    VLKTVBottomBtnClickTypeAudio = 0,       // 声音
    VLKTVBottomBtnClickTypeVideo,           // 视频
    VLKTVBottomBtnClickTypeMore,            // 更多
    VLKTVBottomBtnClickTypeJoinChorus,           // 合唱
    VLKTVBottomBtnClickTypeChoose,          // 点歌
    VLKTVBottomBtnClickTypeLeaveChorus,
} VLKTVBottomBtnClickType;

NS_ASSUME_NONNULL_BEGIN
@class VLKTVBottomToolbar;
@protocol VLKTVBottomToolbarDelegate <NSObject>

- (void)onVLKTVBottomView:(VLKTVBottomToolbar*)view btnTapped:(id)sender withValues:(VLKTVBottomBtnClickType)typeValue;

@end

@interface VLKTVBottomToolbar : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLKTVBottomToolbarDelegate>)delegate withRoomNo:(NSString *)roomNo withData:(NSArray <VLRoomSeatModel *> *)seatsArray;

- (void)updateAudioBtn:(BOOL)audioMuted;
- (void)updateVideoBtn:(BOOL)videoMuted;
- (void)resetBtnStatus;

@end

NS_ASSUME_NONNULL_END
