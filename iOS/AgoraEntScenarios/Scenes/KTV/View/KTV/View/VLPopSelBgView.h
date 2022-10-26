//
//  VLPopSelBgView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@class VLKTVSelBgCell,VLKTVSelBgModel;

@protocol VLPopSelBgViewDelegate <NSObject>

@optional
- (void)bgItemClickAction:(VLKTVSelBgModel *)selBgModel index:(NSInteger)index;

@end

@interface VLPopSelBgView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLPopSelBgViewDelegate>)delegate;

@property (nonatomic, strong) VLKTVSelBgModel *selBgModel;

@end






@interface VLKTVSelBgCell : UICollectionViewCell

@property (nonatomic, strong) UIImageView *picImgView;
@property (nonatomic, strong) UIImageView *selIcon;

@property (nonatomic, strong) VLKTVSelBgModel *selBgModel;

@end

