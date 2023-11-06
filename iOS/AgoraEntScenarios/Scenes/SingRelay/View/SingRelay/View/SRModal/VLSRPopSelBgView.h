//
//  VLPopSelBgView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@class VLSRSelBgCell,VLSRSelBgModel,VLSRPopSelBgView;

@protocol VLSRPopSelBgViewDelegate <NSObject>

- (void)onVLPopSelBgView:(VLSRPopSelBgView*)view tappedWithAction:(VLSRSelBgModel *)selBgModel atIndex:(NSInteger)index;

@end

@interface VLSRPopSelBgView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRPopSelBgViewDelegate>)delegate;

@property (nonatomic, strong) VLSRSelBgModel *selBgModel;

@end


@interface VLSRSelBgCell : UICollectionViewCell

@property (nonatomic, strong) UIImageView *picImgView;
@property (nonatomic, strong) UIImageView *selIcon;

@property (nonatomic, strong) VLSRSelBgModel *selBgModel;

@end

