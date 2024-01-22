//
//  VLKTVTopView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
NS_ASSUME_NONNULL_BEGIN
@class VLRoomListModel, DHCVLKTVTopView;
@protocol DHCVLKTVTopViewDelegate <NSObject>

- (void)onVLKTVTopView:(DHCVLKTVTopView*)view closeBtnTapped:(id)sender;

- (void)onVLKTVTopView:(DHCVLKTVTopView*)view moreBtnTapped:(id)sender;

@end

@interface DHCVLKTVTopView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<DHCVLKTVTopViewDelegate>)delegate;

- (void)setNetworkQuality:(int)quality;

@property (nonatomic, strong) VLRoomListModel *listModel;

@end

NS_ASSUME_NONNULL_END
