//
//  VLChoosedSongTCell.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "VLHotSpotBtn.h"
#import "AgoraEntScenarios-Swift.h"
NS_ASSUME_NONNULL_BEGIN
@interface VLSRSongListCell : UITableViewCell

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

@property (nonatomic, strong) VLSRRoomSelSongModel *selSongModel;

@property (nonatomic, copy) void (^deleteBtnClickBlock)(VLSRRoomSelSongModel *model);

@property (nonatomic, copy) void (^sortBtnClickBlock)(VLSRRoomSelSongModel *model);

@end

NS_ASSUME_NONNULL_END
