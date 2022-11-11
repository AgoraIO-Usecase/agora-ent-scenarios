//
//  VLChooseBelcantoView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@class VLBelcantoModel,VLKTVBelcantoCell,VLChooseBelcantoView;
@protocol VLChooseBelcantoViewDelegate <NSObject>

- (void)onVLChooseBelcantoView:(VLChooseBelcantoView*)view backBtnTapped:(id)sender;
- (void)onVLChooseBelcantoView:(VLChooseBelcantoView*)view itemTapped:(VLBelcantoModel *)model withIndex:(NSInteger)index;

@end

@interface VLChooseBelcantoView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLChooseBelcantoViewDelegate>)delegate;
@property (nonatomic, strong) VLBelcantoModel *selBelcantoModel;

@end




@interface VLKTVBelcantoCell : UICollectionViewCell

@property (nonatomic, strong) UIView *bgView;

@property (nonatomic, strong) UIImageView *iconImgView;

@property (nonatomic, strong) UILabel *titleLabel;

@property (nonatomic, strong) VLBelcantoModel *selBelcantoModel;

@end

