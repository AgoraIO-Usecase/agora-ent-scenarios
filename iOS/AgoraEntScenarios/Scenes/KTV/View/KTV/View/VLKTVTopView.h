//
//  VLKTVTopView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLRoomListModel, VLKTVTopView;
@protocol VLKTVTopViewDelegate <NSObject>

- (void)onVLKTVTopView:(VLKTVTopView*)view closeBtnTapped:(id)sender;

@end

@interface VLKTVTopView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLKTVTopViewDelegate>)delegate;

- (void)setNetworkQuality:(int)quality;

@property (nonatomic, strong) VLRoomListModel *listModel;

@end

NS_ASSUME_NONNULL_END
