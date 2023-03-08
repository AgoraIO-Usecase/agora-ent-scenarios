//
//  VLPopOnLineTypeView.m
//  VoiceOnLine
//

#import "VLPopOnLineTypeView.h"
#import "VLHotSpotBtn.h"
#import "VLFontUtils.h"
#import "VLMacroDefine.h"
#import "KTVMacro.h"
@import QMUIKit;
@import YYCategories;

@interface VLPopOnLineTypeView ()

@property(nonatomic, weak) id <VLPopOnLineTypeViewDelegate>delegate;
@property (nonatomic, strong) QMUIButton *audioBtn;
@property (nonatomic, strong) QMUIButton *videoBtn;
@property (nonatomic, strong) UIImageView *audioSelImgView;
@property (nonatomic, strong) UIImageView *videoSelImgView;

@end

@implementation VLPopOnLineTypeView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLPopOnLineTypeViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    
    VLHotSpotBtn *backBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(20, 20, 20, 20)];
    [backBtn setImage:[UIImage sceneImageWithName:@"ktv_back_whiteIcon"] forState:UIControlStateNormal];
    [backBtn addTarget:self action:@selector(backBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:backBtn];
    
    UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-200)*0.5, 20, 200, 22)];
    titleLabel.text = @"上麦";
    titleLabel.font = VLUIFontMake(16);
    titleLabel.textAlignment = NSTextAlignmentCenter;
    titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:titleLabel];
    
    CGFloat margin = VLREALVALUE_WIDTH(20);
    CGFloat itemW = (SCREEN_WIDTH-3*margin)/2.0;
    
    QMUIButton *audioBtn = [[QMUIButton alloc] qmui_initWithImage:[UIImage sceneImageWithName:@"KTV_onLineType_audionIcon"]
                                                            title:KTVLocalizedString(@"音频上麦")];
    audioBtn.frame = CGRectMake(margin, titleLabel.bottom+26, itemW, VLREALVALUE_WIDTH(72));
    audioBtn.imagePosition = QMUIButtonImagePositionLeft;
    audioBtn.spacingBetweenImageAndTitle = 16;
    audioBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    [audioBtn setTitleColor:UIColorMakeWithHex(@"#C6C4DE") forState:UIControlStateNormal];
    audioBtn.titleLabel.font = UIFontMake(14.0);
    audioBtn.layer.cornerRadius = 10;
    audioBtn.layer.masksToBounds = YES;
    audioBtn.tag = 0;
    self.audioBtn = audioBtn;
    self.audioBtn.adjustsButtonWhenHighlighted = NO;
    self.audioBtn.layer.borderWidth = 1.5f;
    self.audioSelImgView.hidden = YES;
    [audioBtn addTarget:self action:@selector(typeBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [audioBtn setBackgroundColor:UIColorMakeWithHex(@"#000000")];
    [self addSubview:audioBtn];
    
    self.audioSelImgView = [[UIImageView alloc]initWithFrame:CGRectMake(self.audioBtn.right-18, self.audioBtn.bottom-17, 18, 17)];
    self.audioSelImgView.image = [UIImage sceneImageWithName:@"ktv_selbg_icon"];
    self.audioSelImgView.hidden = YES;
    [self addSubview:self.audioSelImgView];
    
    QMUIButton *videoBtn = [[QMUIButton alloc] qmui_initWithImage:[UIImage sceneImageWithName:@"KTV_onLineType_videoIcon"]
                                                            title:KTVLocalizedString(@"视频上麦")];
    videoBtn.frame = CGRectMake(audioBtn.right+margin, audioBtn.top, itemW, VLREALVALUE_WIDTH(72));
    videoBtn.imagePosition = QMUIButtonImagePositionLeft;
    videoBtn.spacingBetweenImageAndTitle = 16;
    videoBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    [videoBtn setTitleColor:UIColorMakeWithHex(@"#C6C4DE") forState:UIControlStateNormal];
    videoBtn.titleLabel.font = UIFontMake(14.0);
    videoBtn.layer.cornerRadius = 10;
    videoBtn.layer.masksToBounds = YES;
    videoBtn.tag = 1;
    self.videoBtn = videoBtn;
    self.videoBtn.layer.borderWidth = 1.5f;
    self.videoBtn.adjustsButtonWhenHighlighted = NO;
    [videoBtn addTarget:self action:@selector(typeBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [videoBtn setBackgroundColor:UIColorMakeWithHex(@"#000000")];
    [self addSubview:videoBtn];
    
    self.videoSelImgView = [[UIImageView alloc]initWithFrame:CGRectMake(self.videoBtn.right-18, self.videoBtn.bottom-17, 18, 17)];
    self.videoSelImgView.image = [UIImage sceneImageWithName:@"ktv_selbg_icon"];
    self.videoSelImgView.hidden = YES;
    [self addSubview:self.videoSelImgView];
}

- (void)typeBtnClickEvent:(QMUIButton *)sender {
    if (sender.tag == 0) {
        self.audioBtn.layer.borderColor = UIColorMakeWithHex(@"#009FFF").CGColor;
        self.videoBtn.layer.borderColor = UIColorClear.CGColor;
        self.videoSelImgView.hidden = YES;
        self.audioSelImgView.hidden = NO;
    }else{
        self.videoBtn.layer.borderColor = UIColorMakeWithHex(@"#009FFF").CGColor;
        self.audioBtn.layer.borderColor = UIColorClear.CGColor;
        self.audioSelImgView.hidden = YES;
        self.videoSelImgView.hidden = NO;
    }
}

- (void)backBtnClickEvent:(id)sender {
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVLPopOnLineTypeView:backBtnTapped:)]) {
        [self.delegate onVLPopOnLineTypeView:self backBtnTapped:sender];
    }
}

@end
