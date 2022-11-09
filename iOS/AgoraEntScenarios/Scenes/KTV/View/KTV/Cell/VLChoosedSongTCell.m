//
//  VLChoosedSongTCell.m
//  VoiceOnLine
//

#import "VLChoosedSongTCell.h"
#import "VLRoomSelSongModel.h"
#import "VLUserCenter.h"
#import "AgoraEntScenarios-Swift.h"
#import "KTVMacro.h"
@import QMUIKit;
@import YYCategories;
@import SDWebImage;

@implementation VLChoosedSongTCell

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
    self.picImgView.image = [UIImage sceneImageWithName:@"online_temp_icon"];
    
    self.numberLabel = [[UILabel alloc]init];
    self.numberLabel.textColor = UIColorMakeWithHex(@"#979CBB");
    self.numberLabel.font = UIFontMake(12);
    self.numberLabel.textAlignment = NSTextAlignmentCenter;
    [self addSubview:self.numberLabel];
    
    self.nameLabel = [[UILabel alloc]init];
    self.nameLabel.textColor = UIColorMakeWithHex(@"#FFFFFF");
    self.nameLabel.font = UIFontBoldMake(15);
    [self addSubview:self.nameLabel];
//
//    self.typeLabel = [[UILabel alloc]init];
//    self.typeLabel.textColor = UIColorMakeWithHex(@"#6C7192");
//    self.typeLabel.font = UIFontMake(9);
//    self.typeLabel.layer.cornerRadius = 4;
//    self.typeLabel.layer.masksToBounds = YES;
//    self.typeLabel.layer.borderWidth = 1.0f;
//    self.typeLabel.textAlignment = NSTextAlignmentCenter;
//    self.typeLabel.layer.borderColor = UIColorMakeWithHex(@"#6C7192").CGColor;
//    [self addSubview:self.typeLabel];
    
    self.chooserLabel = [[UILabel alloc]init];
    self.chooserLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    self.chooserLabel.font = UIFontMake(12);
    [self addSubview:self.chooserLabel];
    
    
    self.deleteBtn = [[VLHotSpotBtn alloc]init];
    [self.deleteBtn setImage:[UIImage sceneImageWithName:@"ktv_delete_icon"] forState:UIControlStateNormal];
    [self.deleteBtn setBackgroundColor:UIColorMakeWithRGBA(4, 9, 37, 0.35)];
    self.deleteBtn.layer.cornerRadius = 18;
    self.deleteBtn.layer.masksToBounds = YES;
    [self.deleteBtn addTarget:self action:@selector(deleteBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.deleteBtn];
    
    self.sortBtn = [[VLHotSpotBtn alloc]init];
    [self.sortBtn setImage:[UIImage sceneImageWithName:@"ktv_sort_icon"] forState:UIControlStateNormal];
    [self.sortBtn setBackgroundColor:UIColorMakeWithRGBA(4, 9, 37, 0.35)];
    self.sortBtn.layer.cornerRadius = 18;
    self.sortBtn.layer.masksToBounds = YES;
    [self.sortBtn addTarget:self action:@selector(sortBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.sortBtn];
    
    self.singingBtn = [[QMUIButton alloc] qmui_initWithImage:[UIImage sceneImageWithName:@"ktv_singing_icon"]
                                                       title:KTVLocalizedString(@"演唱中")];
    self.singingBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    self.singingBtn.imagePosition = QMUIButtonImagePositionLeft;
    self.singingBtn.spacingBetweenImageAndTitle = 4;
    self.singingBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentRight;
    [self.singingBtn setTitleColor:UIColorMakeWithHex(@"#009FFF") forState:UIControlStateNormal];
    self.singingBtn.titleLabel.font = UIFontMake(14.0);
    [self addSubview:self.singingBtn];
    
    self.bottomLine = [[UIView alloc]init];
    self.bottomLine.backgroundColor = UIColorMakeWithRGBA(255, 255, 255, 0.03);
    [self addSubview:self.bottomLine];
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    self.numberLabel.frame = CGRectMake(20, (self.height-17)*0.5, 20, 17);
    self.picImgView.frame = CGRectMake(self.numberLabel.right+15, (self.height-48)*0.5, 48, 48);
    self.deleteBtn.frame = CGRectMake(self.width-20-36, (self.height-36)*0.5, 36, 36);
    self.sortBtn.frame = CGRectMake(self.deleteBtn.left-16-36, (self.height-36)*0.5, 36, 36);
    
    self.nameLabel.frame = CGRectMake(self.picImgView.right+12, self.picImgView.top+2, self.width-self.picImgView.right-12-20-80, 21);
//    self.typeLabel.frame = CGRectMake(self.nameLabel.left, self.nameLabel.bottom+8, 24, 14);
    self.chooserLabel.frame = CGRectMake(self.picImgView.right+12, self.nameLabel.bottom+8, self.width-self.picImgView.right-20-80, 17);
    
    self.singingBtn.frame = CGRectMake(self.width-70-20, (self.height-20)*0.5, 70, 20);
    self.bottomLine.frame = CGRectMake(20, self.height-1, self.width-40, 1);
}

- (void)setSelSongModel:(VLRoomSelSongModel *)selSongModel {
    _selSongModel = selSongModel;
//    if (selSongModel.isChorus) {
//        self.typeLabel.text = KTVLocalizedString(@"合唱");
//    }else{
//        self.typeLabel.text = KTVLocalizedString(@"独唱");
//    }
    self.nameLabel.text = selSongModel.songName;
    if(selSongModel.isChorus) {
        self.chooserLabel.text = [NSString stringWithFormat:KTVLocalizedString(@"合唱: %@"), selSongModel.name];
    }
    else {
        self.chooserLabel.text = [NSString stringWithFormat:KTVLocalizedString(@"点唱: %@"), selSongModel.name];
    }
    
    if (selSongModel.status == 0) {
        self.sortBtn.hidden = self.deleteBtn.hidden = NO;
        self.singingBtn.hidden = YES;
    }else if (selSongModel.status == 2){
        self.sortBtn.hidden = self.deleteBtn.hidden = YES;
        self.singingBtn.hidden = NO;
    }
    
    
    if(!VLUserCenter.user.ifMaster) {
        self.sortBtn.hidden = YES;
        self.deleteBtn.hidden = YES;
    }
    
    [self.picImgView sd_setImageWithURL:[NSURL URLWithString:selSongModel.imageUrl]
                       placeholderImage:[UIImage sceneImageWithName:@"default_avatar"]];
}


- (void)deleteBtnClickEvent {
    self.deleteBtnClickBlock(self.selSongModel);
}

- (void)sortBtnClickEvent {
    self.sortBtnClickBlock(self.selSongModel);
}

@end
