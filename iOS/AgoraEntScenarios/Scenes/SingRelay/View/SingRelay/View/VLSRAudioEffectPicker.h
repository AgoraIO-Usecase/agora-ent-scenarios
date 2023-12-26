//
//  VLChooseBelcantoView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@class VLSRBelcantoModel,VLSRAudioEffectCell,VLSRAudioEffectPicker;
@protocol VLSRAudioEffectPickerDelegate <NSObject>
@optional
- (void)onVLChooseBelcantoView:(VLSRAudioEffectPicker*)view backBtnTapped:(id)sender;
- (void)onVLChooseBelcantoView:(VLSRAudioEffectPicker*)view itemTapped:(VLSRBelcantoModel *)model withIndex:(NSInteger)index;

@end

@interface VLSRAudioEffectPicker : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRAudioEffectPickerDelegate>)delegate;
@property (nonatomic, strong) VLSRBelcantoModel *selBelcantoModel;

@end


@interface VLSRAudioEffectCell : UICollectionViewCell

@property (nonatomic, strong) UIView *bgView;

@property (nonatomic, strong) UIImageView *iconImgView;

@property (nonatomic, strong) UILabel *titleLabel;

@property (nonatomic, strong) VLSRBelcantoModel *selBelcantoModel;

@end

