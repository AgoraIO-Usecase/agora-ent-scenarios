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
@protocol VLKTVBottomViewDelegate <NSObject>

@optional
- (void)bottomAudionBtnAction:(NSInteger)ifMute;
- (void)bottomVideoBtnAction:(NSInteger)ifOpen;
- (void)bottomSetAudioMute:(NSInteger)ifMute;
- (void)bottomSetVideoMute:(NSInteger)ifOpen;

- (void)bottomBtnsClickAction:(VLKTVBottomBtnClickType)typeValue withSender:(VLHotSpotBtn *)sender;

@end

@interface VLKTVBottomView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLKTVBottomViewDelegate>)delegate withRoomNo:(NSString *)roomNo withData:(NSArray <VLRoomSeatModel *> *)seatsArray;

- (bool)isAudioMute;
- (void)resetBtnStatus;

@end

NS_ASSUME_NONNULL_END
