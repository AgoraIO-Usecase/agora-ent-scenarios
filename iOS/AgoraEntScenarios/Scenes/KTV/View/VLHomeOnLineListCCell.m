//
//  VLHomeOnListCCell.m
//  VoiceOnLine
//

#import "VLHomeOnLineListCCell.h"
#import "VLHotSpotBtn.h"
#import "VLMacroDefine.h"
#import "VLFontUtils.h"
#import "AESMacro.h"
@import Masonry;

@interface VLHomeOnLineListCCell()

@property (nonatomic, strong) UIImageView *bgImgView;
@property (nonatomic, strong) UIImageView *iconImgView;
@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UIImageView *onListIconView;
@property (nonatomic, strong) UILabel *countLabel;
@property (nonatomic, strong) UIButton *joinBtn;
@property (nonatomic, strong) UIImageView *lockImgView;

@end

@implementation VLHomeOnLineListCCell

- (instancetype)initWithFrame:(CGRect)frame {
    
    if (self = [super initWithFrame:frame]) {
        [self setupView];
    }
    return self;
}

#pragma mark - Intial Methods
- (void)setupView {

    self.bgImgView = [[UIImageView alloc]initWithFrame:CGRectMake(0, 12, self.width, self.height-12)];
    [self.contentView addSubview:self.bgImgView];
    
    self.iconImgView = [[UIImageView alloc]initWithFrame:CGRectMake(12, -12, 84, 84)];
    self.iconImgView.layer.cornerRadius = 20;
    self.iconImgView.layer.masksToBounds = YES;
    self.iconImgView.userInteractionEnabled = YES;
    [self.bgImgView addSubview:self.iconImgView];
    
    self.lockImgView = [[UIImageView alloc]initWithFrame:CGRectMake(22, self.iconImgView.bottom-8-16, 16, 16)];
    self.lockImgView.image = [UIImage sceneImageWithName:@"online_list_lockIcon"];
    [self.bgImgView addSubview:self.lockImgView];
    
    self.titleLabel = [[UILabel alloc]initWithFrame:CGRectMake(12, self.iconImgView.bottom+VLREALVALUE_WIDTH(10), self.width-30, 21)];
    self.titleLabel.font = UIFontBoldMake(15);
    self.titleLabel.textColor = UIColorMakeWithHex(@"#040925");
    self.titleLabel.userInteractionEnabled = YES;
    [self.bgImgView addSubview:self.titleLabel];
    
    self.onListIconView = [[UIImageView alloc]initWithFrame:CGRectMake(12, self.height-16-11, 11, 11)];
    self.onListIconView.image = [UIImage sceneImageWithName:@"online_list_countIcon"];
    [self.contentView addSubview:self.onListIconView];
    
    self.countLabel = [[UILabel alloc]initWithFrame:CGRectMake(self.onListIconView.right+2, self.onListIconView.centerY-7, 80, 14)];
    self.countLabel.font = UIFontMake(12);
    self.countLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    [self.contentView addSubview:self.countLabel];
    
    self.joinBtn = [[UIButton alloc]initWithFrame:CGRectMake(self.width-10-44, self.bgImgView.bottom-12-24, 44, 24)];
    self.joinBtn.layer.cornerRadius = 12;
    [self.joinBtn setBackgroundColor:UIColorWhite];
    self.joinBtn.layer.masksToBounds = YES;
    [self.joinBtn setTitle:KTVLocalizedString(@"加入") forState:UIControlStateNormal];
    [self.joinBtn setTitleColor:UIColorMakeWithHex(@"#009FFF") forState:UIControlStateNormal];
    self.joinBtn.titleLabel.font = VLUIFontMake(12);
    self.joinBtn.userInteractionEnabled = false;
    [self.contentView addSubview:self.joinBtn];
}

- (void)setListModel:(VLRoomListModel *)listModel {
    _listModel = listModel;
    self.bgImgView.image = [UIImage sceneImageWithName:@"online_list_itemBgIcon"];
    NSString* iconName = [NSString stringWithFormat:@"icon_room_cover%d.jpg", MAX([listModel.icon intValue], 1)];
    self.iconImgView.image = [UIImage sceneImageWithName:iconName];
    if (listModel.isPrivate) {
        self.lockImgView.hidden = NO;
    } else {
        self.lockImgView.hidden = YES;
    }
    self.titleLabel.text = listModel.name;
    self.countLabel.text = [NSString stringWithFormat:KTVLocalizedString(@"%@人"),listModel.roomPeopleNum];
}

@end
