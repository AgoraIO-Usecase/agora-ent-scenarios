//
//  VLChoosedSongTCell.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "VLHotSpotBtn.h"

NS_ASSUME_NONNULL_BEGIN
@interface VLSongListCell : UITableViewCell

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



- (void)setSelSongModel:(VLRoomSelSongModel *)selSongModel isOwner:(BOOL)isOwner;

@property (nonatomic, copy) void (^deleteBtnClickBlock)(VLRoomSelSongModel *model);

@property (nonatomic, copy) void (^sortBtnClickBlock)(VLRoomSelSongModel *model);

@end

NS_ASSUME_NONNULL_END
