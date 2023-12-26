//
//  VLRoomPersonIteimCCell.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
NS_ASSUME_NONNULL_BEGIN
@interface VLMicSeatCell : UICollectionViewCell

@property (nonatomic, strong) UIImageView *avatarImgView;
@property (nonatomic, strong) UIImageView *roomerImgView;
@property (nonatomic, strong) UIView *avatarCoverBgView;
@property (nonatomic, strong) UIImageView *muteImgView;
@property (nonatomic, strong) UILabel *roomerLabel;
@property (nonatomic, strong) UILabel *nickNameLabel;
@property (nonatomic, strong) UIButton *singingBtn;
@property (nonatomic, strong) UIView *videoView; //显示视频
@property (nonatomic, strong) UIButton *joinChorusBtn;

@property (nonatomic, assign) NSInteger volume;

@end

NS_ASSUME_NONNULL_END
