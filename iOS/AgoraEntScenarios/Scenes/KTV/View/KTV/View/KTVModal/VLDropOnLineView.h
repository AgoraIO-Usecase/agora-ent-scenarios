//
//  VLDropOnLineView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLRoomSeatModel,VLDropOnLineView;
@protocol VLDropOnLineViewDelegate <NSObject>

- (void)onVLDropOnLineView:(VLDropOnLineView*)view action:(VLRoomSeatModel * __nullable)seatModel;

@end

@interface VLDropOnLineView : UIView

@property (nonatomic, strong) VLRoomSeatModel *seatModel;

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLDropOnLineViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
