//
//  VLSelectSongTCell.m
//  VoiceOnLine
//

#import "VLSelectedSongListCell.h"
#import "VLSongItmModel.h"
#import "AESMacro.h"
#import "AgoraEntScenarios-Swift.h"
@import SDWebImage;

@implementation VLSelectedSongListCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        self.selectionStyle = UITableViewCellSelectionStyleNone;
        self.contentView.backgroundColor = UIColorMakeWithHex(@"#152164");
        [self setUpView];
    }
    return self;
}

- (void)setUpView {
    self.picImgView = [[UIImageView alloc]init];
    [self addSubview:self.picImgView];
    self.picImgView.layer.cornerRadius = 10;
    self.picImgView.layer.masksToBounds = YES;

    
    self.nameLabel = [[UILabel alloc]init];
    self.nameLabel.textColor = UIColorMakeWithHex(@"#FFFFFF");
    self.nameLabel.font = UIFontBoldMake(15);
    [self addSubview:self.nameLabel];
    
    self.singerLabel = [[UILabel alloc]init];
    self.singerLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    self.singerLabel.font = UIFontMake(12);
    [self addSubview:self.singerLabel];
    
    
    self.chooseBtn = [[UIButton alloc]init];
    [self.chooseBtn setTitleColor:UIColorMakeWithHex(@"#FFFFFF") forState:UIControlStateNormal];
    self.chooseBtn.titleLabel.font = UIFontMake(12.0);
    [self.chooseBtn setTitle:KTVLocalizedString(@"ktv_room_choose_song") forState:UIControlStateNormal];
    self.chooseBtn.accessibilityIdentifier = @"ktv_choose_song_button_id";
    [self.chooseBtn setBackgroundColor:UIColorMakeWithHex(@"#2753FF")];
    self.chooseBtn.layer.cornerRadius = 14;
    self.chooseBtn.layer.masksToBounds = YES;
    [self.chooseBtn addTarget:self action:@selector(chooseBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.chooseBtn];
    
    self.bottomLine = [[UIView alloc]init];
    self.bottomLine.backgroundColor = UIColorMakeWithRGBA(255, 255, 255, 0.03);
    [self addSubview:self.bottomLine];
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    self.picImgView.frame = CGRectMake(22, (self.height-48)*0.5, 48, 48);

    self.nameLabel.frame = CGRectMake(self.picImgView.right+12, self.picImgView.top+2, self.width-self.picImgView.right-12-80-15, 21);
//    self.typeLabel.frame = CGRectMake(self.nameLabel.left, self.nameLabel.bottom+8, 24, 14);
    self.singerLabel.frame = CGRectMake(self.nameLabel.left, self.nameLabel.bottom+8, self.width-20-80, 17);
    
    [self.chooseBtn sizeToFit];
    CGFloat chooseButtonWidth = MAX(self.chooseBtn.width + 10, 56);
    self.chooseBtn.frame = CGRectMake(self.width-chooseButtonWidth-25, (self.height-28)*0.5, chooseButtonWidth, 28);
    
    self.bottomLine.frame = CGRectMake(20, self.height-1, self.width-40, 1);
}

- (void)setSongItemModel:(VLSongItmModel *)songItemModel {
    _songItemModel = songItemModel;
    [self.picImgView sd_setImageWithURL:[NSURL URLWithString:songItemModel.imageUrl]
                       placeholderImage:[UIImage ktv_sceneImageWithName:@"default_avatar" ]];
    self.nameLabel.text = songItemModel.songName;
    self.singerLabel.text = songItemModel.singer;
    
    if (songItemModel.ifChoosed) {
        self.chooseBtn.enabled = NO;
        [self.chooseBtn setTitle:KTVLocalizedString(@"ktv_room_chosen_song_list") forState:UIControlStateNormal];
        [self.chooseBtn setBackgroundColor:UIColorMakeWithRGBA(0, 0, 0, 0.4)];
    }else{
        self.chooseBtn.enabled = YES;
        [self.chooseBtn setTitle:KTVLocalizedString(@"ktv_room_choose_song") forState:UIControlStateNormal];
        [self.chooseBtn setBackgroundColor:UIColorMakeWithHex(@"#2753FF")];
    }

}

- (void)chooseBtnClickEvent {
    self.chooseBtn.enabled = NO;
    self.dianGeBtnClickBlock(self.songItemModel);
}

@end
