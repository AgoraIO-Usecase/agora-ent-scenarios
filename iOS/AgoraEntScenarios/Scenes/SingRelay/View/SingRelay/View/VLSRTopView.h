//
//  VLSRTopView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLSRRoomListModel, VLSRTopView;
@protocol VLSRTopViewDelegate <NSObject>

- (void)onVLSRTopView:(VLSRTopView*)view closeBtnTapped:(id)sender;

- (void)onVLSRTopView:(VLSRTopView*)view moreBtnTapped:(id)sender;

@end

@interface VLSRTopView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRTopViewDelegate>)delegate;

- (void)setNetworkQuality:(int)quality;

@property (nonatomic, strong) VLSRRoomListModel *listModel;

@end

NS_ASSUME_NONNULL_END
