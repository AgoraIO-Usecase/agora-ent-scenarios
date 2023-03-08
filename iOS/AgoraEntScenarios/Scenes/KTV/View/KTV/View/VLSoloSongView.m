//
//  VLSoloSongView.m
//  VoiceOnLine
//

#import "VLSoloSongView.h"
#import "KTVMacro.h"
@import QMUIKit;
@import YYCategories;

@interface VLSoloSongView ()

@property(nonatomic, weak) id <VLSoloSongViewDelegate>delegate;

@end

@implementation VLSoloSongView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSoloSongViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorClear;
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    UIImageView *bgImageView = [[UIImageView alloc]initWithFrame:CGRectMake(0, 0, self.width, self.height)];
    bgImageView.image = [UIImage sceneImageWithName:@"ktv_nobody_iconBg"];
    [self addSubview:bgImageView];
    
    UIImageView *musicIcon = [[UIImageView alloc]initWithFrame:CGRectMake(self.width*0.5-55, 55, 45, 45)];
    musicIcon.image = [UIImage sceneImageWithName:@"ktv_bigMusic_icon"];
    [self addSubview:musicIcon];
    
    self.musicLabel = [[UILabel alloc]initWithFrame:CGRectMake(musicIcon.right, musicIcon.centerY-10, self.width*0.5-15, 20)];
    self.musicLabel.font = UIFontMake(14);
    self.musicLabel.textColor = UIColorMakeWithHex(@"#FFFFFF");
    [self addSubview:self.musicLabel];
    
    UILabel *tipsLabel = [[UILabel alloc]initWithFrame:CGRectMake(self.width*0.5-45, musicIcon.bottom+5, 75, 20)];
    tipsLabel.text = KTVLocalizedString(@"抢麦倒计时");
    tipsLabel.font = UIFontMake(14);
    tipsLabel.textColor = UIColorMakeWithHex(@"#FFFFFF");
    [self addSubview:tipsLabel];
    
    self.countDownLabel = [[UILabel alloc]initWithFrame:CGRectMake(tipsLabel.right+6, tipsLabel.top, 75, 20)];
    self.countDownLabel.font = UIFontMake(14);
    self.countDownLabel.textColor = UIColorMakeWithHex(@"#FFE960");
    [self addSubview:self.countDownLabel];
    
    UIButton *soloBtn = [[UIButton alloc]initWithFrame:CGRectMake((self.width-115)*0.5, tipsLabel.bottom+30, 115, 36)];
    soloBtn.layer.cornerRadius = 18;
    soloBtn.layer.masksToBounds = YES;
    [soloBtn addTarget:self action:@selector(soloBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    [soloBtn setBackgroundColor:UIColorMakeWithHex(@"#345DFF")];
    [soloBtn setTitle:KTVLocalizedString(@"不等了，独唱") forState:UIControlStateNormal];
    [soloBtn setTitleColor:UIColorWhite forState:UIControlStateNormal];
    soloBtn.titleLabel.font = UIFontBoldMake(14);
    [self addSubview:soloBtn];
}

- (void)soloBtnClickEvent {
    if (self.delegate && [self.delegate respondsToSelector:@selector(soloBtnClickAction)]) {
        [self.delegate soloBtnClickAction];
    }
}


@end
