//
//  VLKTVBottomView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "VLRoomSeatModel.h"
#import "VLHotSpotBtn.h"
#import "KTVServiceProtocol.h"

typedef enum : NSUInteger {
    VLKTVBottomBtnClickTypeAudio = 0,       // 声音
    VLKTVBottomBtnClickTypeVideo,           // 视频
    VLKTVBottomBtnClickTypeMore,            // 更多
    VLKTVBottomBtnClickTypeChorus,           // 合唱
    VLKTVBottomBtnClickTypeChoose,          // 点歌
} VLKTVBottomBtnClickType;

NS_ASSUME_NONNULL_BEGIN
@class VLKTVBottomView;
@protocol VLKTVBottomViewDelegate <NSObject>

- (void)onVLKTVBottomView:(VLKTVBottomView*)view btnTapped:(id)sender withValues:(VLKTVBottomBtnClickType)typeValue;

@end

@interface VLKTVBottomView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLKTVBottomViewDelegate>)delegate withRoomNo:(NSString *)roomNo withData:(NSArray <VLRoomSeatModel *> *)seatsArray;

- (void)updateAudioBtn:(BOOL)audioMuted;
- (void)updateVideoBtn:(BOOL)videoMuted;
- (void)resetBtnStatus;

@end

NS_ASSUME_NONNULL_END
