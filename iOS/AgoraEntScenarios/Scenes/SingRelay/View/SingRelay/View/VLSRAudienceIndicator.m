//
//  VLTouristOnLineView.m
//  VoiceOnLine
//

#import "VLSRAudienceIndicator.h"
#import "AESMacro.h"
@import YYCategories;

@interface VLSRAudienceIndicator ()

@property(nonatomic, weak) id <VLSRAudienceIndicatorDelegate>delegate;
@property (nonatomic, strong) UIImageView *requestImgView;
@property (nonatomic, strong) UILabel *tipsLabel;
@end

@implementation VLSRAudienceIndicator

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRAudienceIndicatorDelegate>)delegate {
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
    requestImgView.image = [UIImage sr_sceneImageWithName:@"ktv_request_onlineIcon" ];
    requestImgView.userInteractionEnabled = YES;
    [self addSubview:requestImgView];
    self.requestImgView = requestImgView;
    
    UILabel *tipsLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-300)*0.5, requestImgView.bottom+20, 300, 20)];
    tipsLabel.text = SRLocalizedString(@"sr_room_before_speaker");
    tipsLabel.textAlignment = NSTextAlignmentCenter;
    tipsLabel.textColor = UIColorMakeWithHex(@"#C6C4DE");
    tipsLabel.font = UIFontMake(14);
    [self addSubview:tipsLabel];
    self.tipsLabel = tipsLabel;
}

- (void)tapgesEvent {
    if ([self.delegate respondsToSelector:@selector(requestOnlineAction)]) {
        [self.delegate requestOnlineAction];
    }
}

-(void)setTipHidden:(BOOL)isHidden {
    self.tipsLabel.hidden = isHidden;
    self.requestImgView.hidden = isHidden;
}

@end
