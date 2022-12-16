//
//  VLRobMicrophoneView.m
//  VoiceOnLine
//

#import "VLRobMicrophoneView.h"
#import "KTVMacro.h"
@import QMUIKit;
@import YYCategories;

@interface VLRobMicrophoneView ()

@property(nonatomic, weak) id <VLRobMicrophoneViewDelegate>delegate;

@end

@implementation VLRobMicrophoneView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLRobMicrophoneViewDelegate>)delegate {
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
        
    UIButton *joinBtn = [[UIButton alloc]initWithFrame:CGRectMake((self.width-115)*0.5, tipsLabel.bottom+30, 115, 36)];
    joinBtn.layer.cornerRadius = 18;
    joinBtn.layer.masksToBounds = YES;
    [joinBtn addTarget:self action:@selector(joinBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [joinBtn setBackgroundColor:UIColorMakeWithHex(@"#345DFF")];
    [joinBtn setTitle:KTVLocalizedString(@"加入合唱") forState:UIControlStateNormal];
    [joinBtn setTitleColor:UIColorWhite forState:UIControlStateNormal];
    joinBtn.titleLabel.font = UIFontBoldMake(14);
    [self addSubview:joinBtn];
}

- (void)joinBtnClickEvent:(UIButton*)sender {
    if ([self.delegate respondsToSelector:@selector(robViewChorusAction)]) {
        [self.delegate robViewChorusAction];
    }
}

@end
