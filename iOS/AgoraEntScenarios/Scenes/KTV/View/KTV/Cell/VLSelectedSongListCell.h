//
//  VLSelectSongTCell.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLSongItmModel;
@interface VLSelectedSongListCell : UITableViewCell

@property (nonatomic, strong) UIImageView *picImgView;

@property (nonatomic, strong) UILabel *nameLabel;
//@property (nonatomic, strong) UILabel *typeLabel;
@property (nonatomic, strong) UILabel *singerLabel;
@property (nonatomic, strong) UIButton *chooseBtn;
@property (nonatomic, strong) UIView *bottomLine;

@property (nonatomic, strong) VLSongItmModel *songItemModel;

@property (nonatomic, copy) void(^dianGeBtnClickBlock)(VLSongItmModel *model);

@end

NS_ASSUME_NONNULL_END
