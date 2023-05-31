//
//  VLDropOnLineView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLSBGRoomSeatModel,VLSBGDropOnLineView;
@protocol VLSBGDropOnLineViewDelegate <NSObject>

- (void)onVLDropOnLineView:(VLSBGDropOnLineView*)view action:(VLSBGRoomSeatModel * __nullable)seatModel;

@end

@interface VLSBGDropOnLineView : UIView

@property (nonatomic, strong) VLSBGRoomSeatModel *seatModel;

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGDropOnLineViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
