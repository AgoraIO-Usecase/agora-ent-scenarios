//
//  VLDropOnLineView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@import AgoraCommon;
NS_ASSUME_NONNULL_BEGIN
@class VLRoomSeatModel,VLDropOnLineView;
@protocol VLDropOnLineViewDelegate <NSObject>

- (void)onVLDropOnLineView:(VLDropOnLineView*)view action:(VLRoomSeatModel * __nullable)seatModel;

@end

@interface VLDropOnLineView : UIView

@property (nonatomic, strong) VLRoomSeatModel *seatModel;
#if DEBUG
- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id)delegate;
#else
- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLDropOnLineViewDelegate>)delegate;
#endif
@end

NS_ASSUME_NONNULL_END
