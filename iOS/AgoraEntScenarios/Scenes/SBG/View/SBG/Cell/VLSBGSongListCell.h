//
//  VLChoosedSongTCell.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "VLHotSpotBtn.h"
#import "VLSBGRoomSelSongModel.h"

NS_ASSUME_NONNULL_BEGIN
@interface VLSBGSongListCell : UITableViewCell

@property (nonatomic, strong) UIImageView *picImgView;

@property (nonatomic, strong) UILabel *numberLabel;
@property (nonatomic, strong) UILabel *nameLabel;
//合唱/独唱
//@property (nonatomic, strong) UILabel *typeLabel;
@property (nonatomic, strong) UILabel *chooserLabel;
@property (nonatomic, strong) VLHotSpotBtn *deleteBtn;
@property (nonatomic, strong) VLHotSpotBtn *sortBtn;
@property (nonatomic, strong) UIButton *singingBtn;
@property (nonatomic, strong) UIView *bottomLine;

@property (nonatomic, strong) VLSBGRoomSelSongModel *selSongModel;

@property (nonatomic, copy) void (^deleteBtnClickBlock)(VLSBGRoomSelSongModel *model);

@property (nonatomic, copy) void (^sortBtnClickBlock)(VLSBGRoomSelSongModel *model);

@end

NS_ASSUME_NONNULL_END
