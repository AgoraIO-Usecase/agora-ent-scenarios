//
//  VLTouristOnLineView.m
//  VoiceOnLine
//

#import "VLSBGAudienceIndicator.h"
#import "SBGMacro.h"
@import YYCategories;

@interface VLSBGAudienceIndicator ()

@property(nonatomic, weak) id <VLSBGAudienceIndicatorDelegate>delegate;
@property (nonatomic, strong) UIImageView *requestImgView;
@property (nonatomic, strong) UILabel *tipsLabel;
@end

@implementation VLSBGAudienceIndicator

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGAudienceIndicatorDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorClear;
        self.delegate = delegate;
        UITapGestureRecognizer *tapges = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(tapgesEvent)];
        [self addGestureRecognizer:tapges];
        [self setupView];
    }
    return self;
}

- (void)setupView {
    
    UIImageView *requestImgView = [[UIImageView alloc]initWithFrame:CGRectMake((self.width-345)*0.5, 0, 345, 16)];
    requestImgView.image = [UIImage sceneImageWithName:@"ktv_request_onlineIcon"];
    requestImgView.userInteractionEnabled = YES;
    [self addSubview:requestImgView];
    self.requestImgView = requestImgView;
    
    UILabel *tipsLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-300)*0.5, requestImgView.bottom+20, 300, 20)];
    tipsLabel.text = SBGLocalizedString(@"点击空麦位上麦后加入游戏");
    tipsLabel.textAlignment = NSTextAlignmentCenter;
    tipsLabel.textColor = UIColorMakeWithHex(@"#C6C4DE");
    tipsLabel.font = UIFontMake(14);
    [self addSubview:tipsLabel];
    self.tipsLabel = tipsLabel;
}

-(void)setTipHidden:(BOOL)isHidden {
    self.tipsLabel.hidden = isHidden;
    self.requestImgView.hidden = isHidden;
}

- (void)tapgesEvent {
    if ([self.delegate respondsToSelector:@selector(requestOnlineAction)]) {
        [self.delegate requestOnlineAction];
    }
}

@end
