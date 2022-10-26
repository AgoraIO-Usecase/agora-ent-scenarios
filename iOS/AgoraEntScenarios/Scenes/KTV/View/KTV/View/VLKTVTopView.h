//
//  VLKTVTopView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLRoomListModel;
@protocol VLKTVTopViewDelegate <NSObject>

@optional
- (void)closeBtnAction;

@end

@interface VLKTVTopView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLKTVTopViewDelegate>)delegate;

- (void)setNetworkQuality:(int)quality;

@property (nonatomic, strong) VLRoomListModel *listModel;

@end

NS_ASSUME_NONNULL_END
