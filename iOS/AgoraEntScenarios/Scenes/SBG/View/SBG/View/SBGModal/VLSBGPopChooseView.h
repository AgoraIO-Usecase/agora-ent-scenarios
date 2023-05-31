//
//  VLSBGPopChooseView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@class VLSBGSelBgCell,VLSBGSelBgModel,VLSBGPopSelBgView;

@protocol VLSBGPopSelBgViewDelegate <NSObject>

- (void)onVLPopSelBgView:(VLSBGPopSelBgView*)view tappedWithAction:(VLSBGSelBgModel *)selBgModel atIndex:(NSInteger)index;

@end

@interface VLSBGPopSelBgView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGPopSelBgViewDelegate>)delegate;

@property (nonatomic, strong) VLSBGSelBgModel *selBgModel;

@end


@interface VLSBGSelBgCell : UICollectionViewCell

@property (nonatomic, strong) UIImageView *picImgView;
@property (nonatomic, strong) UIImageView *selIcon;

@property (nonatomic, strong) VLSBGSelBgModel *selBgModel;

@end

