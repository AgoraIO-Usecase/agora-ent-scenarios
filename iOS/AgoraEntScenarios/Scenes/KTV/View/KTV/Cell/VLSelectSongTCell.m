//
//  VLSelectSongTCell.m
//  VoiceOnLine
//

#import "VLSelectSongTCell.h"
#import "VLSongItmModel.h"
#import "KTVMacro.h"
@import QMUIKit;
@import YYCategories;
@import SDWebImage;

@implementation VLSelectSongTCell

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
    

//    self.typeLabel = [[UILabel alloc]init];
//    self.typeLabel.textColor = UIColorMakeWithHex(@"#6C7192");
//    self.typeLabel.font = UIFontMake(9);
//    self.typeLabel.layer.cornerRadius = 4;
//    self.typeLabel.layer.masksToBounds = YES;
//    self.typeLabel.layer.borderWidth = 1.0f;
//    self.typeLabel.textAlignment = NSTextAlignmentCenter;
//    self.typeLabel.layer.borderColor = UIColorMakeWithHex(@"#6C7192").CGColor;
//    [self addSubview:self.typeLabel];
//    self.typeLabel.text = @"合唱";
    
    self.singerLabel = [[UILabel alloc]init];
    self.singerLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    self.singerLabel.font = UIFontMake(12);
    [self addSubview:self.singerLabel];
    
    
    self.chooseBtn = [[UIButton alloc]init];
    [self.chooseBtn setTitleColor:UIColorMakeWithHex(@"#FFFFFF") forState:UIControlStateNormal];
    self.chooseBtn.titleLabel.font = UIFontMake(12.0);
    [self.chooseBtn setTitle:KTVLocalizedString(@"点歌") forState:UIControlStateNormal];
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
    
    self.chooseBtn.frame = CGRectMake(self.width-56-25, (self.height-28)*0.5, 56, 28);
    self.bottomLine.frame = CGRectMake(20, self.height-1, self.width-40, 1);
}

- (void)setSongItemModel:(VLSongItmModel *)songItemModel {
    _songItemModel = songItemModel;
    [self.picImgView sd_setImageWithURL:[NSURL URLWithString:songItemModel.imageUrl]
                       placeholderImage:[UIImage sceneImageWithName:@"default_avatar"]];
    self.nameLabel.text = songItemModel.songName;
    self.singerLabel.text = songItemModel.singer;
    
    if (songItemModel.ifChoosed) {
        self.chooseBtn.enabled = NO;
        [self.chooseBtn setTitle:KTVLocalizedString(@"已点") forState:UIControlStateNormal];
        [self.chooseBtn setBackgroundColor:UIColorMakeWithRGBA(0, 0, 0, 0.4)];
    }else{
        self.chooseBtn.enabled = YES;
        [self.chooseBtn setTitle:KTVLocalizedString(@"点歌") forState:UIControlStateNormal];
        [self.chooseBtn setBackgroundColor:UIColorMakeWithHex(@"#2753FF")];
    }

}

- (void)chooseBtnClickEvent {
    self.chooseBtn.enabled = NO;
    self.dianGeBtnClickBlock(self.songItemModel);
}

@end
