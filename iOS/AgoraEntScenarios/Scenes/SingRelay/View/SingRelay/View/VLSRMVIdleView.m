//
//  VLNoBodyOnLineView.m
//  VoiceOnLine
//

#import "VLSRMVIdleView.h"
#import "AESMacro.h"

@interface VLSRMVIdleView ()

@property(nonatomic, weak) id <VLSRMVIdleViewDelegate>delegate;

@end

@implementation VLSRMVIdleView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRMVIdleViewDelegate>)delegate {
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
    titleLabel.text = KTVLocalizedString(@"ktv_room_no_sing");
    titleLabel.textColor = UIColorWhite;
    titleLabel.font = UIFontMake(14);
    titleLabel.numberOfLines = 0;
    titleLabel.textAlignment = NSTextAlignmentCenter;
    [titleLabel sizeToFit];
    [self addSubview:titleLabel];
    titleLabel.center = CGPointMake(self.width / 2, self.height / 2);
}

@end
