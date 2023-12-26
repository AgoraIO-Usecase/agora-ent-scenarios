//
//  VLSBGBottomView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "VLSBGRoomSeatModel.h"
#import "VLHotSpotBtn.h"
#import "SBGServiceProtocol.h"

typedef enum : NSUInteger {
    VLSBGBottomBtnClickTypeAudio = 0,       // 声音
    VLSBGBottomBtnClickTypeVideo,           // 视频
    VLSBGBottomBtnClickTypeMore,            // 更多
    VLSBGBottomBtnClickTypeJoinChorus,           // 合唱
    VLSBGBottomBtnClickTypeChoose,          // 点歌
    VLSBGBottomBtnClickTypeLeaveChorus,
    VLSBGBottomBtnClickTypeShowVoice,       //人声突出
} VLSBGBottomBtnClickType;

NS_ASSUME_NONNULL_BEGIN
@class VLSBGBottomToolbar;
@protocol VLSBGBottomToolbarDelegate <NSObject>

- (void)onVLSBGBottomView:(VLSBGBottomToolbar*)view btnTapped:(id)sender withValues:(VLSBGBottomBtnClickType)typeValue;

@end

@interface VLSBGBottomToolbar : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGBottomToolbarDelegate>)delegate withRoomNo:(NSString *)roomNo withData:(NSArray <VLSBGRoomSeatModel *> *)seatsArray;
-(void)setAudioBtnEnabled:(BOOL)enabled;
- (void)updateAudioBtn:(BOOL)audioMuted;
- (void)updateVideoBtn:(BOOL)videoMuted;
- (void)resetBtnStatus;

@end

NS_ASSUME_NONNULL_END
