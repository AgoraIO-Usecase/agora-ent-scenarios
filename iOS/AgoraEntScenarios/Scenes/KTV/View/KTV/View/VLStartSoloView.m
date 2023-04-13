//
//  VLSoloSongView.m
//  VoiceOnLine
//

#import "VLStartSoloView.h"
#import "KTVMacro.h"

@interface VLStartSoloView ()

@property(nonatomic, weak) id <VLStartSoloViewDelegate>delegate;

@end

@implementation VLStartSoloView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLStartSoloViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorClear;
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    UIImageView *bgImageView = [[UIImageView alloc]initWithFrame:self.bounds];
    bgImageView.image = [UIImage sceneImageWithName:@"ktv_nobody_iconBg"];
    [self addSubview:bgImageView];
    
    CGFloat centerY = self.height / 2;
    UIButton *soloBtn = [[UIButton alloc]initWithFrame:CGRectZero];
    [soloBtn addTarget:self action:@selector(soloBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    [soloBtn setBackgroundColor:UIColorMakeWithHex(@"#345DFF")];
    [soloBtn setTitle:KTVLocalizedString(@"不等了，独唱") forState:UIControlStateNormal];
    [soloBtn setTitleColor:UIColorWhite forState:UIControlStateNormal];
    soloBtn.titleLabel.font = UIFontBoldMake(14);
    [self addSubview:soloBtn];
    [soloBtn sizeToFit];
    CGFloat soloWidth = soloBtn.width + 30;
    soloBtn.frame = CGRectMake((self.width - soloWidth) / 2, centerY, soloWidth, soloBtn.height);
    soloBtn.layer.cornerRadius = soloBtn.height / 2;
    soloBtn.layer.masksToBounds = YES;
    
    UILabel *tipsLabel = [[UILabel alloc]initWithFrame:CGRectZero];
    tipsLabel.text = KTVLocalizedString(@"抢麦倒计时");
    tipsLabel.font = UIFontMake(14);
    tipsLabel.textColor = UIColorMakeWithHex(@"#FFFFFF");
    [self addSubview:tipsLabel];
    [tipsLabel sizeToFit];
    tipsLabel.frame = CGRectMake((self.width - tipsLabel.width) / 2 - 20, soloBtn.top - tipsLabel.height - 20, tipsLabel.width, tipsLabel.height);
    
    self.countDownLabel = [[UILabel alloc]initWithFrame:CGRectMake(tipsLabel.right+6, tipsLabel.top, 75, tipsLabel.height)];
    self.countDownLabel.font = UIFontMake(14);
    self.countDownLabel.textColor = UIColorMakeWithHex(@"#FFE960");
    [self addSubview:self.countDownLabel];
    
    UIImageView *musicIcon = [[UIImageView alloc]initWithFrame:CGRectMake(self.width / 2 - 80, tipsLabel.top - 50, 45, 45)];
    musicIcon.image = [UIImage sceneImageWithName:@"ktv_bigMusic_icon"];
    [self addSubview:musicIcon];
    
    self.musicLabel = [[UILabel alloc]initWithFrame:CGRectMake(musicIcon.right, musicIcon.centerY-10, self.width*0.5-15, 20)];
    self.musicLabel.font = UIFontMake(14);
    self.musicLabel.textColor = UIColorMakeWithHex(@"#FFFFFF");
    [self addSubview:self.musicLabel];
}

- (void)soloBtnClickEvent {
    if (self.delegate && [self.delegate respondsToSelector:@selector(onStartSoloBtn)]) {
        [self.delegate onStartSoloBtn];
    }
}


@end
