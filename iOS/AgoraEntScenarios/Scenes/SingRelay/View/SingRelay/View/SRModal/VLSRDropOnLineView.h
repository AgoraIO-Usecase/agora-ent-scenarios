//
//  VLDropOnLineView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLSRRoomSeatModel,VLSRDropOnLineView;
@protocol VLSRDropOnLineViewDelegate <NSObject>

- (void)onVLDropOnLineView:(VLSRDropOnLineView*)view action:(VLSRRoomSeatModel * __nullable)seatModel;

@end

@interface VLSRDropOnLineView : UIView

@property (nonatomic, strong) VLSRRoomSeatModel *seatModel;

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRDropOnLineViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
