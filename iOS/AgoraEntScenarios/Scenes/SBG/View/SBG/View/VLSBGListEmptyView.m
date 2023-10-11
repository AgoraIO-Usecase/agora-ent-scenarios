//
//  VLSBGListEmptyView.m
//  VoiceOnLine
//

#import "VLSBGListEmptyView.h"
#import "VLMacroDefine.h"
#import "SBGMacro.h"

@implementation VLSBGListEmptyView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorClear;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    UILabel *introduceLabel = [[UILabel alloc]initWithFrame:CGRectMake(30, 0, SCREEN_WIDTH-60, 90)];
    NSString *introduceStr = SBGLocalizedString(@"欢迎来到声网嗨歌抢唱游戏，\n 热歌劲曲、歌词同步、动态打分等功能一站体验，\n一起来抢吧！");
    introduceLabel.attributedText = [self attributedString:introduceStr fontSize:14 lineSpace:4 wordSpace:0];
    introduceLabel.textAlignment = NSTextAlignmentCenter;
    introduceLabel.numberOfLines = 0;
    introduceLabel.textColor = UIColorMakeWithHex(@"#3C4267");
    introduceLabel.font = UIFontMake(14);
    [self addSubview:introduceLabel];
    
    UIImageView *emptyImgView = [[UIImageView alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-255)*0.5, introduceLabel.bottom+VLREALVALUE_WIDTH(30), 255, 202)];
    emptyImgView.image = [UIImage sceneImageWithName:@"online_empty_placeHolder"];
    [self addSubview:emptyImgView];
}


- (NSAttributedString *)attributedString:(NSString *)string fontSize:(CGFloat)fontSize lineSpace:(CGFloat)lineSpace wordSpace:(CGFloat)wordSpace {
    
    NSMutableParagraphStyle *paragraphStyle = [[NSMutableParagraphStyle alloc] init];
    paragraphStyle.lineBreakMode = NSLineBreakByCharWrapping;
    paragraphStyle.lineSpacing = lineSpace;
    NSDictionary *attributes = @{
                                 NSKernAttributeName:@(wordSpace),
                                 NSFontAttributeName:[UIFont systemFontOfSize:fontSize],
                                 NSParagraphStyleAttributeName:paragraphStyle
                                 };
    
    NSAttributedString *attributeStr = [[NSAttributedString alloc] initWithString:string attributes:attributes];
    return attributeStr;
}

@end
