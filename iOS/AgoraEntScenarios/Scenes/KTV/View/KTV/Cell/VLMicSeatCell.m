//
//  VLRoomPersonIteimCCell.m
//  VoiceOnLine
//

#import "VLMicSeatCell.h"
#import "VLMacroDefine.h"
#import "AgoraEntScenarios-Swift.h"
#import "AESMacro.h"
@import YYCategories;

@interface VLMicSeatCell()

//@property (nonatomic, strong) AgoraRtcChannelMediaOptions *mediaOption;
@property (nonatomic, strong) CALayer *waveLayer1;
@property (nonatomic, strong) CALayer *waveLayer2;
@property (nonatomic, assign) BOOL isAnimating;

@end

@implementation VLMicSeatCell

- (instancetype)initWithFrame:(CGRect)frame {
    
    if (self = [super initWithFrame:frame]) {
        [self setupView];
    }
    return self;
}

- (CALayer *)waveLayer1 {
    if(!_waveLayer1) {
        _waveLayer1 = [self createWaveLayer];
    }
    return _waveLayer1;
}

- (CALayer *)waveLayer2 {
    if(!_waveLayer2) {
        _waveLayer2 = [self createWaveLayer];
    }
    return _waveLayer2;
}

- (CALayer *)createWaveLayer {
    CALayer *layer = [CALayer layer];
    layer.frame = CGRectMake(0, 0, VLREALVALUE_WIDTH(54),  VLREALVALUE_WIDTH(54));
    layer.backgroundColor = [UIColor colorWithHexString:@"#75ADFF"].CGColor;
    layer.cornerRadius = VLREALVALUE_WIDTH(54) * 0.5;
    layer.hidden = YES;
    return layer;
}

#pragma mark - Intial Methods
- (void)setupView {
    
    [self.contentView.layer addSublayer:self.waveLayer2];
    [self.contentView.layer addSublayer:self.waveLayer1];
    
    self.avatarImgView = [[UIImageView alloc]initWithFrame:CGRectMake(0, 0, VLREALVALUE_WIDTH(54),  VLREALVALUE_WIDTH(54))];
    self.avatarImgView.layer.cornerRadius = VLREALVALUE_WIDTH(54)*0.5;
    self.avatarImgView.layer.masksToBounds = YES;
    self.avatarImgView.userInteractionEnabled = YES;
    self.avatarImgView.contentMode = UIViewContentModeScaleAspectFill;
    [self.contentView addSubview:self.avatarImgView];
    
    self.videoView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, VLREALVALUE_WIDTH(54), VLREALVALUE_WIDTH(54))];
    self.videoView.layer.cornerRadius = VLREALVALUE_WIDTH(54)*0.5;
    self.videoView.layer.masksToBounds = YES;
    [self.contentView addSubview:self.videoView];
    
    self.avatarCoverBgView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, VLREALVALUE_WIDTH(54), VLREALVALUE_WIDTH(54))];
    self.avatarCoverBgView.userInteractionEnabled = YES;
    self.avatarCoverBgView.backgroundColor = UIColorMakeWithRGBA(0, 0, 0, 0);
    self.avatarCoverBgView.layer.cornerRadius = VLREALVALUE_WIDTH(54)*0.5;
    self.avatarCoverBgView.layer.masksToBounds = YES;
    [self.contentView addSubview:self.avatarCoverBgView];
    

    
    self.roomerImgView = [[UIImageView alloc]initWithFrame:CGRectMake((VLREALVALUE_WIDTH(54)-34)*0.5, VLREALVALUE_WIDTH(54)-12, 34, 12)];
    self.roomerImgView.image = [UIImage ktv_sceneImageWithName:@"ktv_roomOwner_icon" ];
    [self.contentView addSubview:self.roomerImgView];
    
    self.roomerLabel = [[UILabel alloc]initWithFrame:CGRectMake(0, 0, 34, 11)];
    self.roomerLabel.font = UIFontMake(8);
    self.roomerLabel.textColor = UIColorMakeWithHex(@"#DBDAE9");
    self.roomerLabel.textAlignment = NSTextAlignmentCenter;
    [self.roomerImgView addSubview:self.roomerLabel];
    
    self.nickNameLabel = [[UILabel alloc]initWithFrame:CGRectMake(2, self.avatarImgView.bottom+2, VLREALVALUE_WIDTH(54)-4, 17)];
    self.nickNameLabel.font = UIFontMake(12);
    self.nickNameLabel.textColor = UIColorMakeWithHex(@"#DBDAE9");
    self.nickNameLabel.textAlignment = NSTextAlignmentCenter;
    [self.contentView addSubview:self.nickNameLabel];
    
    self.muteImgView = [[UIImageView alloc]initWithFrame:CGRectMake(VLREALVALUE_WIDTH(54)/2-12, VLREALVALUE_WIDTH(54)/2-12, 24, 24)];
    self.muteImgView.image = [UIImage ktv_sceneImageWithName:@"ktv_self_seatMute"];
    self.muteImgView.userInteractionEnabled = YES;
    [self.contentView addSubview:self.muteImgView];
    
    self.singingBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.singingBtn setImage:[UIImage ktv_sceneImageWithName:@"ktv_seatsinging_icon" ] forState:UIControlStateNormal];
    [self.singingBtn setTitle:KTVLocalizedString(@"ktv_zc") forState:UIControlStateNormal];
    self.singingBtn.frame = CGRectMake((self.width-36)*0.5, self.nickNameLabel.bottom+2, 36, 12);
    self.singingBtn.layer.cornerRadius = 6;
    self.singingBtn.layer.masksToBounds = YES;
//    self.singingBtn.imagePosition = QMUIButtonImagePositionLeft;
    self.singingBtn.spacingBetweenImageAndTitle = 2;
    self.singingBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    [self.singingBtn setTitleColor:UIColorMakeWithHex(@"#FFFFFF") forState:UIControlStateNormal];
    self.singingBtn.titleLabel.font = UIFontMake(8);
    self.singingBtn.userInteractionEnabled = NO;
    self.singingBtn.backgroundColor = UIColorMakeWithRGBA(0, 0, 0, 0.5);
    self.singingBtn.alpha = 0.6;
    [self.contentView addSubview:self.singingBtn];

    
    self.joinChorusBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.joinChorusBtn setImage:[UIImage ktv_sceneImageWithName:@"ic_hc"] forState:UIControlStateNormal];
    [self.joinChorusBtn setTitle:KTVLocalizedString(@"ktv_hc") forState:UIControlStateNormal];
    self.joinChorusBtn.frame = CGRectMake((self.width-36)*0.5, self.nickNameLabel.bottom+2, 36, 12);
    self.joinChorusBtn.layer.cornerRadius = 6;
    self.joinChorusBtn.layer.masksToBounds = YES;
//    self.joinChorusBtn.imagePosition = QMUIButtonImagePositionLeft;
    self.joinChorusBtn.spacingBetweenImageAndTitle = 2;
    self.joinChorusBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    [self.joinChorusBtn setTitleColor:UIColorMakeWithHex(@"#FFFFFF") forState:UIControlStateNormal];
    self.joinChorusBtn.titleLabel.font = UIFontMake(8);
    self.joinChorusBtn.userInteractionEnabled = NO;
    self.joinChorusBtn.backgroundColor = UIColorMakeWithRGBA(0, 0, 0, 0.5);
    self.joinChorusBtn.alpha = 0.6;
    [self.contentView addSubview:self.joinChorusBtn];
}


- (void)startAnimation {
    if(_isAnimating) {
        return;
    }
    _isAnimating = YES;
    self.waveLayer1.hidden = NO;
    self.waveLayer2.hidden = NO;
    CAKeyframeAnimation *animation = [CAKeyframeAnimation animationWithKeyPath:@"transform.scale"];
    animation.values = @[@1,@1.1,@1];
    animation.keyTimes = @[@0.0,@0.5,@1];
    
    CAKeyframeAnimation *alphaAnimation = [CAKeyframeAnimation animationWithKeyPath:@"opacity"];
    alphaAnimation.values = @[@1,@0.5,@0.3];
    alphaAnimation.keyTimes = @[@0.0,@0.5,@1];
    
    CAAnimationGroup *groupAnimation = [CAAnimationGroup animation];
    groupAnimation.animations = @[animation,alphaAnimation];
//    groupAnimation.autoreverses = YES;
    groupAnimation.repeatCount = MAXFLOAT;
    groupAnimation.duration = 1.4;
    [self.waveLayer1 addAnimation:groupAnimation forKey:nil];
    
    
    CAKeyframeAnimation *animation2 = [CAKeyframeAnimation animationWithKeyPath:@"transform.scale"];
    animation2.values = @[@1,@1.4];
    animation2.keyTimes = @[@0.0,@1];
    
    CAKeyframeAnimation *alphaAnimation2 = [CAKeyframeAnimation animationWithKeyPath:@"opacity"];
    alphaAnimation2.values = @[@0.6,@0.3,@0];
    alphaAnimation2.keyTimes = @[@0.0,@0.5,@1];
    
    CAAnimationGroup *groupAnimation2 = [CAAnimationGroup animation];
    groupAnimation2.animations = @[animation2,alphaAnimation2];
//    groupAnimation.autoreverses = YES;
    groupAnimation2.repeatCount = MAXFLOAT;
    groupAnimation2.duration = 1.4;
    [self.waveLayer2 addAnimation:groupAnimation2 forKey:nil];
    
}

-(void)stopAnimation{
    _isAnimating = NO;
    [self.waveLayer1 removeAllAnimations];
    [self.waveLayer2 removeAllAnimations];
    self.waveLayer1.hidden = YES;
    self.waveLayer2.hidden = YES;
}

- (void)setVolume:(NSInteger)volume {
    _volume = volume;
    if(_volume > 0) {
        [self startAnimation];
    }else{
        [self stopAnimation];
    }
}

@end
