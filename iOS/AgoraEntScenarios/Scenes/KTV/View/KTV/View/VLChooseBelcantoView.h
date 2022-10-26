//
//  VLChooseBelcantoView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@class VLBelcantoModel,VLKTVBelcantoCell;
@protocol VLChooseBelcantoViewDelegate <NSObject>

@optional
- (void)belcantoBackBtnAction;
- (void)belcantoItemClickAction:(VLBelcantoModel *)model withIndx:(NSInteger)index;

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

