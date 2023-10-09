//
//  VLHomeItemView.m
//  VoiceOnLine
//

#import "VLHomeItemView.h"
#import "VLHomeItemModel.h"
#import "VLMacroDefine.h"
#import "AESMacro.h"

@interface VLHomeItemView ()

@property (nonatomic, strong) UIImageView *bgImgView;
@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UIView *subTitleBgView;
@property (nonatomic, strong) UILabel *subTitleLabel;
@property (nonatomic, strong) UIImageView *itemImgView;



@end

@implementation VLHomeItemView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self setupView];
    }
    return self;
}

- (void)setupView {
    [self addSubview:self.bgImgView];
    [self addSubview:self.titleLabel];
    [self addSubview:self.subTitleBgView];
    [self.subTitleBgView addSubview:self.subTitleLabel];
    [self addSubview:self.itemImgView];
}

- (UIImageView *)bgImgView {
    if (!_bgImgView) {
        _bgImgView = [[UIImageView alloc]initWithFrame:CGRectMake(0, 0, self.width, self.height)];
        _bgImgView.userInteractionEnabled = YES;
    }
    return _bgImgView;
}

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc]initWithFrame:CGRectMake(21, VLREALVALUE_WIDTH(25), self.width-42, 30)];
        _titleLabel.font = [UIFont fontWithName:@"PingFangSC" size:16];
        _titleLabel.adjustsFontSizeToFitWidth = YES;
        _titleLabel.textColor = UIColorMakeWithHex(@"#FFFFFF");
    }
    return _titleLabel;
}

- (UIView *)subTitleBgView {
    if (!_subTitleBgView) {
        _subTitleBgView = [[UIImageView alloc]initWithFrame:CGRectMake(_titleLabel.left, _titleLabel.bottom+3, 150, 20)];
        _subTitleBgView.layer.cornerRadius = 10;
        _subTitleBgView.layer.masksToBounds = YES;
//        _subTitleBgView.backgroundColor = UIColorMakeWithRGBA(0, 0, 0, 0.3);
    }
    return _subTitleBgView;
}

- (UILabel *)subTitleLabel {
    if (!_subTitleLabel) {
        _subTitleLabel = [[UILabel alloc]initWithFrame:CGRectMake(0, 0, 150, 20)];
        _subTitleLabel.textAlignment = NSTextAlignmentLeft;
        _subTitleLabel.font = [UIFont fontWithName:@"PingFangSC" size:10];
        _subTitleLabel.font = [UIFont systemFontOfSize:10 weight:UIFontWeightMedium];
        _subTitleLabel.textColor = UIColorMakeWithHex(@"#FFFFFF");
    }
    return _subTitleLabel;
}

- (UIImageView *)itemImgView {
    if (!_itemImgView) {
        _itemImgView = [[UIImageView alloc]initWithFrame:CGRectMake((self.width-162)*0.5, self.height-12-121, 162, 121)];
        _itemImgView.userInteractionEnabled = YES;
    }
    return _itemImgView;
}


- (void)setItemModel:(VLHomeItemModel *)itemModel {
    _itemModel = itemModel;
    self.bgImgView.image = UIImageMake(itemModel.bgImgStr);
    self.titleLabel.text = itemModel.titleStr;
    self.subTitleLabel.text = itemModel.subTitleStr;
    [self.subTitleLabel sizeToFit];
    self.itemImgView.image = UIImageMake(itemModel.iconImgStr);
    self.subTitleBgView.hidden = !itemModel.subTitleStr.length;
    
}

@end
