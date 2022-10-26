//
//  VLDropOnLineView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLRoomSeatModel;
@protocol VLDropOnLineViewDelegate <NSObject>

@optional
- (void)dropOnLineAction:(VLRoomSeatModel *)seatModel;

@end

@interface VLDropOnLineView : UIView

@property (nonatomic, strong) VLRoomSeatModel *seatModel;

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLDropOnLineViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END
