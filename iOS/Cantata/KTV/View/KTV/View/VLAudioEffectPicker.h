//
//  VLChooseBelcantoView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@class VLBelcantoModel,VLKTVAudioEffectCell,VLAudioEffectPicker;
@protocol VLAudioEffectPickerDelegate <NSObject>
@optional
- (void)onVLChooseBelcantoView:(VLAudioEffectPicker*)view backBtnTapped:(id)sender;
- (void)onVLChooseBelcantoView:(VLAudioEffectPicker*)view itemTapped:(VLBelcantoModel *)model withIndex:(NSInteger)index;

@end

@interface VLAudioEffectPicker : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLAudioEffectPickerDelegate>)delegate;
@property (nonatomic, strong) VLBelcantoModel *selBelcantoModel;

@end


@interface VLKTVAudioEffectCell : UICollectionViewCell

@property (nonatomic, strong) UIView *bgView;

@property (nonatomic, strong) UIImageView *iconImgView;

@property (nonatomic, strong) UILabel *titleLabel;

@property (nonatomic, strong) VLBelcantoModel *selBelcantoModel;

@end

