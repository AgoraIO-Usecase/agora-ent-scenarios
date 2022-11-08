//
//  VLPopScoreView.m
//  VoiceOnLine
//

#import "VLPopScoreView.h"
#import "UIView+VL.h"
#import "VLFontUtils.h"
#import "VLMacroDefine.h"
#import "KTVMacro.h"
@import QMUIKit;
@import Masonry;

@interface VLPopScoreView ()

@property(nonatomic, weak) id <VLPopScoreViewDelegate>delegate;
@property (nonatomic, strong) UIImageView *scoreImgView;
@property (nonatomic, strong) UIImageView *starImageView;
@property (nonatomic, strong) UILabel *scoreLabel;
@property (nonatomic, strong) UILabel *scoreTitleLabel;;
/// 圆盘光
@property (nonatomic, strong) UIImageView *circleLightImageView;
/// 圆盘
@property (nonatomic, strong) UIImageView *circleImageView;
@property (nonatomic, strong) UIButton *confirmButton;

@end


@implementation VLPopScoreView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLPopScoreViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithRGBA(0, 0, 0, 0.7);
        self.delegate = delegate;
        [self setupView];
//        @weakify(self)
//        [self vl_whenTapped:^{
//            @strongify(self)
//            [self dismiss];
//        }];
    }
    return self;
}

- (void)setupView {
    [self addSubview:self.circleImageView];
    [self.circleImageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.centerY.mas_equalTo(self);
    }];
    
    [self addSubview:self.circleLightImageView];
    [self.circleLightImageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.mas_equalTo(self);
        make.bottom.mas_equalTo(self.circleImageView.mas_top).offset(5);
    }];
    
    [self addSubview:self.starImageView];
    [self addSubview:self.scoreLabel];
    [self.scoreLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.mas_equalTo(self);
        make.bottom.mas_equalTo(self.circleImageView.mas_top).offset(-10);
    }];
    
    [self addSubview:self.scoreTitleLabel];
    [self.scoreTitleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.mas_equalTo(self);
        make.bottom.mas_equalTo(self.scoreLabel.mas_top).offset(-15);
    }];
    
    [self addSubview:self.scoreImgView];
    [self.scoreImgView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.mas_equalTo(self);
        make.bottom.mas_equalTo(self.scoreTitleLabel.mas_top).offset(-25);
    }];
    [self.starImageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.mas_equalTo(self.scoreImgView);
        make.centerY.mas_equalTo(self.scoreImgView).offset(100);
    }];

    [self addSubview:self.confirmButton];
    [self.confirmButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.mas_equalTo(self);
        make.bottom.mas_equalTo(self.circleImageView.mas_bottom).offset(60);
        make.height.mas_equalTo(40);
        make.width.mas_equalTo(115);
    }];
}

- (void)configScore:(int)score {
    self.scoreLabel.text = [NSString stringWithFormat:@"%d",score];
    NSString *imageNamed;
    // 评分逻辑
    // ktv_score_A 、ktv_score_B 、ktv_score_C 、 ktv_score_S
    if (score >= 90) {
        imageNamed = @"ktv_score_S";
    } else if (score >= 80 && score < 90) {
        imageNamed = @"ktv_score_A";
    } else if (score >= 60 && score < 80) {
        imageNamed = @"ktv_score_B";
    } else {
        imageNamed = @"ktv_score_C";
    }
    VLLog(@"Using as score view %@", imageNamed);
    _scoreImgView.image = [UIImage imageNamed:imageNamed];
}

- (void)dismiss {
    [UIView animateWithDuration:0.3 animations:^{
        self.alpha = 0;
    } completion:^(BOOL finished) {
        [self removeFromSuperview];
    }];
}

- (void)click {
    [self dismiss];
    if ([self.delegate respondsToSelector:@selector(popScoreViewDidClickConfirm)]) {
        [self.delegate popScoreViewDidClickConfirm];
    }
}

- (UILabel *)scoreLabel {
    if (!_scoreLabel) {
        _scoreLabel = [[UILabel alloc] init];
        _scoreLabel.text = KTVLocalizedString(@"99");
        _scoreLabel.font = VLUIFontMake(30);
        _scoreLabel.textColor = [UIColor whiteColor];
    }
    return _scoreLabel;
}

- (UILabel *)scoreTitleLabel {
    if (!_scoreTitleLabel) {
        _scoreTitleLabel = [[UILabel alloc] init];
        _scoreTitleLabel.text = KTVLocalizedString(@"你的总分");
        _scoreTitleLabel.font = VLUIFontMake(14);
        _scoreTitleLabel.textColor = [UIColor whiteColor];
    }
    return _scoreTitleLabel;
}

- (UIImageView *)starImageView {
    if (!_starImageView) {
        _starImageView = [[UIImageView alloc] init];
        _starImageView.image = [UIImage imageNamed:@"ktv_score_star"];
    }
    return _starImageView;
}

- (UIImageView *)scoreImgView {
    if (!_scoreImgView) {
        _scoreImgView = [[UIImageView alloc] init];
        _scoreImgView.image = [UIImage imageNamed:@"ktv_score_S"];
    }
    return _scoreImgView;
}

- (UIImageView *)circleLightImageView {
    if (!_circleLightImageView) {
        _circleLightImageView = [[UIImageView alloc] init];
        _circleLightImageView.image = [UIImage imageNamed:@"ktv_score_topPart"];
    }
    return _circleLightImageView;
}

- (UIImageView *)circleImageView {
    if (!_circleImageView) {
        _circleImageView = [[UIImageView alloc] init];
        _circleImageView.image = [UIImage imageNamed:@"ktv_score_bottomPart"];
    }
    return _circleImageView;
}

- (UIButton *)confirmButton {
    if (!_confirmButton) {
        _confirmButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _confirmButton.layer.cornerRadius = 20;
        _confirmButton.layer.masksToBounds = YES;
        [_confirmButton setBackgroundColor:UIColorMakeWithHex(@"#345DFF")];
        [_confirmButton setTitle:KTVLocalizedString(@"好的") forState:UIControlStateNormal];
        _confirmButton.titleLabel.font = UIFontBoldMake(16);
        [_confirmButton setTitleColor:UIColorWhite forState:UIControlStateNormal];
        [_confirmButton addTarget:self action:@selector(click) forControlEvents:UIControlEventTouchUpInside];
    }
    return _confirmButton;
}

@end
