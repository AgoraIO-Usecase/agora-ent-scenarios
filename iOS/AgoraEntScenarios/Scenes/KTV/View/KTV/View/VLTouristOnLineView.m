//
//  VLTouristOnLineView.m
//  VoiceOnLine
//

#import "VLTouristOnLineView.h"
#import "KTVMacro.h"
@import QMUIKit;
@import YYCategories;

@interface VLTouristOnLineView ()

@property(nonatomic, weak) id <VLTouristOnLineViewDelegate>delegate;

@end

@implementation VLTouristOnLineView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLTouristOnLineViewDelegate>)delegate {
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
    
    UILabel *tipsLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-300)*0.5, requestImgView.bottom+20, 300, 20)];
    tipsLabel.text = KTVLocalizedString(@"点击空麦位，上麦后可点歌和发言");
    tipsLabel.textAlignment = NSTextAlignmentCenter;
    tipsLabel.textColor = UIColorMakeWithHex(@"#C6C4DE");
    tipsLabel.font = UIFontMake(14);
    [self addSubview:tipsLabel];
}

- (void)tapgesEvent {
    if (self.delegate && [self.delegate respondsToSelector:@selector(requestOnlineAction)]) {
        [self.delegate requestOnlineAction];
    }
}

@end
