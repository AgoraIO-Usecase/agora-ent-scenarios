//
//  VLHomeOnListCCell.m
//  VoiceOnLine
//

#import "VLSRHomeOnLineListCCell.h"
#import "AgoraEntScenarios-Swift.h"
#import <SDWebImage/UIImageView+WebCache.h>
@import Masonry;

@interface VLSRHomeOnLineListCCell()

@property (nonatomic, strong) UIImageView *iconImgView;
@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UIImageView *onListIconView;
@property (nonatomic, strong) UILabel *countLabel;
@property (nonatomic, strong) UIButton *joinBtn;
@property (nonatomic, strong) UIImageView *lockImgView;
@property (nonatomic, strong) UILabel *roomOwnerLabel;
@end

@implementation VLSRHomeOnLineListCCell

- (instancetype)initWithFrame:(CGRect)frame {
    
    if (self = [super initWithFrame:frame]) {
        [self setupView];
    }
    return self;
}

#pragma mark - Intial Methods
- (void)setupView {

    self.bgImgView = [[UIImageView alloc]initWithFrame:CGRectMake(0, 0, self.width, self.height)];
    [self.contentView addSubview:self.bgImgView];
    
    self.iconImgView = [[UIImageView alloc]initWithFrame:CGRectMake((self.width - 64) * 0.5, 20, 64, 64)];
    self.iconImgView.layer.cornerRadius = 32;
    self.iconImgView.layer.masksToBounds = YES;
    self.iconImgView.userInteractionEnabled = YES;
    self.iconImgView.contentMode = UIViewContentModeScaleAspectFill;
    [self.bgImgView addSubview:self.iconImgView];
    
    self.lockImgView = [[UIImageView alloc]initWithFrame:CGRectMake(self.width - 26, 10, 16, 16)];
    self.lockImgView.image = [UIImage sr_sceneImageWithName:@"suo" ];
    [self.bgImgView addSubview:self.lockImgView];
    
    self.titleLabel = [[UILabel alloc]initWithFrame:CGRectMake(10, self.iconImgView.bottom+VLREALVALUE_WIDTH(10), self.width-20, 40)];
    self.titleLabel.font = UIFontBoldMake(14);
    self.titleLabel.textAlignment = NSTextAlignmentCenter;
    self.titleLabel.numberOfLines = 0;
    self.titleLabel.textColor = UIColorMakeWithHex(@"#040925");
    
    self.titleLabel.userInteractionEnabled = YES;
    [self.bgImgView addSubview:self.titleLabel];

    self.onListIconView = [[UIImageView alloc]initWithFrame:CGRectMake(self.width - 55, self.height-16-11, 11, 11)];
    self.onListIconView.image = [UIImage sr_sceneImageWithName:@"online_list_countIcon" ];
    [self.contentView addSubview:self.onListIconView];
    
    self.countLabel = [[UILabel alloc]initWithFrame:CGRectMake(self.onListIconView.right+2, self.onListIconView.centerY-7, 40, 14)];
    self.countLabel.font = UIFontMake(12);
    self.countLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    [self.contentView addSubview:self.countLabel];
    
    self.roomOwnerLabel = [[UILabel alloc]initWithFrame:CGRectMake(10, self.onListIconView.centerY-7, 60, 14)];
    self.roomOwnerLabel.font = UIFontBoldMake(12);
    self.roomOwnerLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    
    self.roomOwnerLabel.userInteractionEnabled = YES;
    [self.bgImgView addSubview:self.roomOwnerLabel];
}

- (void)setListModel:(VLSRRoomListModel *)listModel {
    _listModel = listModel;
    [self.iconImgView sd_setImageWithURL:[NSURL URLWithString:listModel.creatorAvatar]];
    if (listModel.isPrivate) {
        self.lockImgView.hidden = NO;
    } else {
        self.lockImgView.hidden = YES;
    }
    self.titleLabel.text = listModel.name;
    self.roomOwnerLabel.text = listModel.creatorName;
    self.countLabel.text = [NSString stringWithFormat:@"%@%@",listModel.roomPeopleNum, SRLocalizedString(@"sr_people")];
}

@end
