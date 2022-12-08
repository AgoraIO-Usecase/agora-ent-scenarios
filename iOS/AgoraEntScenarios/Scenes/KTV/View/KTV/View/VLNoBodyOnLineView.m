//
//  VLNoBodyOnLineView.m
//  VoiceOnLine
//

#import "VLNoBodyOnLineView.h"
#import "KTVMacro.h"
@import QMUIKit;
@import YYCategories;

@interface VLNoBodyOnLineView ()

@property(nonatomic, weak) id <VLNoBodyOnLineViewDelegate>delegate;

@end

@implementation VLNoBodyOnLineView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLNoBodyOnLineViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    UIImageView *bgImageView = [[UIImageView alloc]initWithFrame:CGRectMake(0, 0, self.width, self.height)];
    bgImageView.image = [UIImage sceneImageWithName:@"ktv_nobody_iconBg"];
    [self addSubview:bgImageView];
    
    UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectZero];
    titleLabel.text = KTVLocalizedString(@"当前无人演唱\n\n点击“点歌”一展歌喉");
    titleLabel.textColor = UIColorWhite;
    titleLabel.font = UIFontMake(14);
    titleLabel.numberOfLines = 0;
    titleLabel.textAlignment = NSTextAlignmentCenter;
    [titleLabel sizeToFit];
    [self addSubview:titleLabel];
    titleLabel.center = CGPointMake(self.width / 2, self.height / 2);
}

@end
