//
//  VLDropOnLineView.m
//  VoiceOnLine
//

#import "VLDropOnLineView.h"
#import "VLRoomSeatModel.h"
#import "KTVMacro.h"
@import QMUIKit;
@import YYCategories;
@import SDWebImage;

@interface VLDropOnLineView ()

@property(nonatomic, weak) id <VLDropOnLineViewDelegate>delegate;
@property (nonatomic, strong) UIImageView *avatarImgView;
@property (nonatomic, strong) UILabel *nickNameLabel;

@end

@implementation VLDropOnLineView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLDropOnLineViewDelegate>)delegate{
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorClear;
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    UIView *topView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, self.width, 32)];
    topView.backgroundColor = UIColorClear;
    [self addSubview:topView];
    
    
    UIView *bottomView = [[UIView alloc]initWithFrame:CGRectMake(0, 32, SCREEN_WIDTH, self.height-32)];
    bottomView.backgroundColor = UIColorMakeWithHex(@"#152164");
    [self setPartRoundWithView:bottomView corners:UIRectCornerTopLeft | UIRectCornerTopRight cornerRadius:20];
    [self addSubview:bottomView];
    
    UIImageView *avatarImgView = [[UIImageView alloc]initWithFrame:CGRectMake((self.width-78)*0.5, 0, 78, 78)];
    self.avatarImgView = avatarImgView;
    self.avatarImgView.layer.cornerRadius = 78*0.5;
    self.avatarImgView.layer.masksToBounds = YES;
    [self addSubview:avatarImgView];
    
    UILabel *nickNameLabel = [[UILabel alloc]initWithFrame:CGRectMake((self.width-150)*0.5, avatarImgView.bottom+12, 150, 20)];
    nickNameLabel.textAlignment = NSTextAlignmentCenter;
    nickNameLabel.font = UIFontMake(14);
    nickNameLabel.textColor = UIColorWhite;
    self.nickNameLabel = nickNameLabel;
    [self addSubview:nickNameLabel];
    
    UIButton *dropLineBtn = [[UIButton alloc]initWithFrame:CGRectMake((self.width-102)*0.5, nickNameLabel.bottom+48, 102, 32)];
    dropLineBtn.layer.cornerRadius = 16;
    dropLineBtn.layer.masksToBounds = YES;
    [dropLineBtn setTitle:KTVLocalizedString(@"下麦") forState:UIControlStateNormal];
    [dropLineBtn setTitleColor:UIColorWhite forState:UIControlStateNormal];
    dropLineBtn.titleLabel.font = UIFontMake(12);
    [dropLineBtn setBackgroundColor:UIColorMakeWithHex(@"#2753FF")];
    [dropLineBtn addTarget:self action:@selector(dropLineBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:dropLineBtn];
}

- (void)setPartRoundWithView:(UIView *)view corners:(UIRectCorner)corners cornerRadius:(float)cornerRadius {
    CAShapeLayer *shapeLayer = [CAShapeLayer layer];
    shapeLayer.path = [UIBezierPath bezierPathWithRoundedRect:view.bounds byRoundingCorners:corners cornerRadii:CGSizeMake(cornerRadius, cornerRadius)].CGPath;
    view.layer.mask = shapeLayer;
}

- (void)dropLineBtnClickEvent {
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVLDropOnLineView:action:)]) {
        [self.delegate onVLDropOnLineView:self action:self.seatModel];
    }
}

- (void)setSeatModel:(VLRoomSeatModel *)seatModel {
    _seatModel = seatModel;
    [self.avatarImgView sd_setImageWithURL:[NSURL URLWithString:seatModel.headUrl]];
    self.nickNameLabel.text = seatModel.name;
}

@end
