//
//  VLKTVTopView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLRoomListModel, VLKTVTopView, SyncRoomInfo;
@protocol VLKTVTopViewDelegate <NSObject>

- (void)onVLKTVTopView:(VLKTVTopView*)view closeBtnTapped:(id)sender;

- (void)onVLKTVTopView:(VLKTVTopView*)view moreBtnTapped:(id)sender;

@end

@interface VLKTVTopView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLKTVTopViewDelegate>)delegate;

- (void)setNetworkQuality:(int)quality;

@property (nonatomic, strong) SyncRoomInfo *listModel;

@end

NS_ASSUME_NONNULL_END
