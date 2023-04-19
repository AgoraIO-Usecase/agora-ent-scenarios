//
//  VLRobMicrophoneView.m
//  VoiceOnLine
//

#import "VLJoinChorusView.h"
#import "KTVMacro.h"

@interface VLJoinChorusView ()
@property (nonatomic, strong) UILabel *musicLabel;
@property (nonatomic, strong) UIImageView *musicIcon;
@property(nonatomic, weak) id <VLJoinChorusViewDelegate>delegate;

@end

@implementation VLJoinChorusView

- (void)setMusicLabelText:(NSString *)musicLabelText {
    _musicLabelText = musicLabelText;
    _musicLabel.text = musicLabelText;
    [_musicLabel sizeToFit];
    _musicLabel.frame = CGRectMake((self.width - _musicLabel.width + 45) / 2, _countDownLabel.top - _musicLabel.height - 10, _musicLabel.width, _musicLabel.height);
    _musicIcon.frame = CGRectMake(_musicLabel.left - 45, _musicLabel.top - 15, 45, 45);
}

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLJoinChorusViewDelegate>)delegate {
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
    
    CGFloat centerY = self.height / 2;
    
    UIButton *joinBtn = [[UIButton alloc]initWithFrame:CGRectMake((self.width-115)*0.5, centerY, 115, 36)];
    joinBtn.layer.cornerRadius = 18;
    joinBtn.layer.masksToBounds = YES;
    [joinBtn addTarget:self action:@selector(joinBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [joinBtn setBackgroundColor:UIColorMakeWithHex(@"#345DFF")];
    [joinBtn setTitle:KTVLocalizedString(@"加入合唱") forState:UIControlStateNormal];
    [joinBtn setTitleColor:UIColorWhite forState:UIControlStateNormal];
    joinBtn.titleLabel.font = UIFontBoldMake(14);
    [self addSubview:joinBtn];
    
    UILabel *tipsLabel = [[UILabel alloc]initWithFrame:CGRectZero];
    tipsLabel.text = KTVLocalizedString(@"抢麦倒计时");
    tipsLabel.font = UIFontMake(14);
    tipsLabel.textColor = UIColorMakeWithHex(@"#FFFFFF");
    [self addSubview:tipsLabel];
    [tipsLabel sizeToFit];
    
    self.countDownLabel = [[UILabel alloc]initWithFrame:CGRectZero];
    self.countDownLabel.font = UIFontMake(14);
    self.countDownLabel.textColor = UIColorMakeWithHex(@"#FFE960");
    [self addSubview:self.countDownLabel];
    
    tipsLabel.frame = CGRectMake((self.width - tipsLabel.width - 45) / 2, joinBtn.top - 30, tipsLabel.width, 20);
    self.countDownLabel.frame = CGRectMake(tipsLabel.right+6, tipsLabel.top, 75, 20);
    
    _musicIcon = [[UIImageView alloc]initWithFrame:CGRectZero];
    _musicIcon.image = [UIImage sceneImageWithName:@"ktv_bigMusic_icon"];
    [self addSubview:_musicIcon];
    
    self.musicLabel = [[UILabel alloc]initWithFrame:CGRectZero];
    self.musicLabel.font = UIFontMake(14);
    self.musicLabel.textColor = UIColorMakeWithHex(@"#FFFFFF");
    [self addSubview:self.musicLabel];
}

- (void)joinBtnClickEvent:(UIButton*)sender {
    if ([self.delegate respondsToSelector:@selector(onJoinChorusBtn)]) {
        [self.delegate onJoinChorusBtn];
    }
}

@end
