//
//  VLTouristOnLineView.m
//  VoiceOnLine
//

#import "VLAudienceIndicator.h"
#import "AgoraEntScenarios-Swift.h"
@import YYCategories;

@interface VLAudienceIndicator ()

@property(nonatomic, weak) id <VLAudienceIndicatorDelegate>delegate;

@end

@implementation VLAudienceIndicator

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLAudienceIndicatorDelegate>)delegate {
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
    requestImgView.image = [UIImage ktv_sceneImageWithName:@"ktv_request_onlineIcon" ];
    requestImgView.userInteractionEnabled = YES;
    [self addSubview:requestImgView];
    
    UILabel *tipsLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-300)*0.5, requestImgView.bottom+20, 300, 20)];
    tipsLabel.text = KTVLocalizedString(@"ktv_room_before_speaker");
    tipsLabel.textAlignment = NSTextAlignmentCenter;
    tipsLabel.textColor = UIColorMakeWithHex(@"#C6C4DE");
    tipsLabel.font = UIFontMake(14);
    [self addSubview:tipsLabel];
}

- (void)tapgesEvent {
    if ([self.delegate respondsToSelector:@selector(requestOnlineAction)]) {
        [self.delegate requestOnlineAction];
    }
}

@end
