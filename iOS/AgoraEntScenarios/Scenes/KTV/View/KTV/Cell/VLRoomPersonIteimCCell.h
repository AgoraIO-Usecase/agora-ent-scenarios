//
//  VLRoomPersonIteimCCell.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@import QMUIKit;
NS_ASSUME_NONNULL_BEGIN
@interface VLRoomPersonIteimCCell : UICollectionViewCell

@property (nonatomic, strong) UIImageView *avatarImgView;
@property (nonatomic, strong) UIImageView *roomerImgView;
@property (nonatomic, strong) UIView *avatarCoverBgView;
@property (nonatomic, strong) UIImageView *muteImgView;
@property (nonatomic, strong) UILabel *roomerLabel;
@property (nonatomic, strong) UILabel *nickNameLabel;
@property (nonatomic, strong) QMUIButton *singingBtn;
@property (nonatomic, strong) UIView *videoView; //显示视频
@property (nonatomic, strong) QMUIButton *joinChorusBtn;

@end

NS_ASSUME_NONNULL_END
