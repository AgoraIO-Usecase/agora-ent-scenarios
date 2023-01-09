//
//  VLBadNetWorkView.m
//  VoiceOnLine
//

#import "VLBadNetWorkView.h"
#import "KTVMacro.h"
@import QMUIKit;
@import YYCategories;

@interface VLBadNetWorkView ()

@property(nonatomic, weak) id <VLBadNetWorkViewDelegate>delegate;

@end

@implementation VLBadNetWorkView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLBadNetWorkViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#FFFFFF");
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    UIImageView *iconImgView = [[UIImageView alloc]initWithFrame:CGRectMake((self.width-227)*0.5, 20, 227, 124)];
    iconImgView.image = [UIImage sceneImageWithName:@"ktv_badNet_icon"];
    [self addSubview:iconImgView];
    
    UILabel *lab = [[UILabel alloc]initWithFrame:CGRectMake((self.width-180)*0.5, iconImgView.bottom+5, 180, 20)];
    lab.text = KTVLocalizedString(@"本机网络差，无法加入合唱");
    lab.font = UIFontMake(14);
    lab.textAlignment = NSTextAlignmentCenter;
    lab.textColor = UIColorMakeWithHex(@"#3C4267");
    [self addSubview:lab];
    
    UIButton *knowBtn = [[UIButton alloc]initWithFrame:CGRectMake((self.width-115)*0.5, lab.bottom+37, 115, 40)];
    knowBtn.layer.cornerRadius = 20;
    knowBtn.layer.masksToBounds = YES;
    [knowBtn addTarget:self action:@selector(knowBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    [knowBtn setBackgroundColor:UIColorMakeWithHex(@"#345DFF")];
    [knowBtn setTitle:KTVLocalizedString(@"我知道了") forState:UIControlStateNormal];
    [knowBtn setTitleColor:UIColorWhite forState:UIControlStateNormal];
    knowBtn.titleLabel.font = UIFontBoldMake(16);
    [self addSubview:knowBtn];
    
}

- (void)knowBtnClickEvent:(id)sender {
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVLBadNetworkView:dismiss:)]) {
        [self.delegate onVLBadNetworkView:self dismiss:sender];
    }
}

@end
