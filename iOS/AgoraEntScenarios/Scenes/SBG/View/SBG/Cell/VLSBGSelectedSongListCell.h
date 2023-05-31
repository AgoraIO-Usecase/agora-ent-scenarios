//
//  VLSelectSongTCell.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLSBGSongItmModel;
@interface VLSBGSelectedSongListCell : UITableViewCell

@property (nonatomic, strong) UIImageView *picImgView;

@property (nonatomic, strong) UILabel *nameLabel;
//@property (nonatomic, strong) UILabel *typeLabel;
@property (nonatomic, strong) UILabel *singerLabel;
@property (nonatomic, strong) UIButton *chooseBtn;
@property (nonatomic, strong) UIView *bottomLine;

@property (nonatomic, strong) VLSBGSongItmModel *songItemModel;

@property (nonatomic, copy) void(^dianGeBtnClickBlock)(VLSBGSongItmModel *model);

@end

NS_ASSUME_NONNULL_END
