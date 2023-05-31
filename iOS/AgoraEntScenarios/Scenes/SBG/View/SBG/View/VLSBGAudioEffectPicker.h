//
//  VLChooseBelcantoView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@class VLSBGBelcantoModel,VLSBGAudioEffectCell,VLSBGAudioEffectPicker;
@protocol VLSBGAudioEffectPickerDelegate <NSObject>
@optional
- (void)onVLChooseBelcantoView:(VLSBGAudioEffectPicker*)view backBtnTapped:(id)sender;
- (void)onVLChooseBelcantoView:(VLSBGAudioEffectPicker*)view itemTapped:(VLSBGBelcantoModel *)model withIndex:(NSInteger)index;

@end

@interface VLSBGAudioEffectPicker : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGAudioEffectPickerDelegate>)delegate;
@property (nonatomic, strong) VLSBGBelcantoModel *selBelcantoModel;

@end


@interface VLSBGAudioEffectCell : UICollectionViewCell

@property (nonatomic, strong) UIView *bgView;

@property (nonatomic, strong) UIImageView *iconImgView;

@property (nonatomic, strong) UILabel *titleLabel;

@property (nonatomic, strong) VLSBGBelcantoModel *selBelcantoModel;

@end

