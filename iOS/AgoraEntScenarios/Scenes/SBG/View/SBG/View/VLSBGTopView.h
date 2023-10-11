//
//  VLSBGTopView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLSBGRoomListModel, VLSBGTopView;
@protocol VLSBGTopViewDelegate <NSObject>

- (void)onVLSBGTopView:(VLSBGTopView*)view closeBtnTapped:(id)sender;

- (void)onVLSBGTopView:(VLSBGTopView*)view moreBtnTapped:(id)sender;

@end

@interface VLSBGTopView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGTopViewDelegate>)delegate;

- (void)setNetworkQuality:(int)quality;

@property (nonatomic, strong) VLSBGRoomListModel *listModel;

@end

NS_ASSUME_NONNULL_END
