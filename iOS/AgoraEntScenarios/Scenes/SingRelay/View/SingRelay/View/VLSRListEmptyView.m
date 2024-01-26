//
//  VLListEmptyView.m
//  VoiceOnLine
//

#import "VLSRListEmptyView.h"
#import <Masonry/Masonry.h>
#import "AgoraEntScenarios-Swift.h"
@import AgoraCommon;
@implementation VLSRListEmptyView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorClear;
        [self setupView];
    }
    return self;
}

- (void)setupView {

    UILabel *introduceLabel = [[UILabel alloc]init];
    NSString *introduceStr = SRLocalizedString(@"sr_create_tips1");
    introduceLabel.attributedText = [self attributedString:introduceStr fontSize:14 lineSpace:4 wordSpace:0];
    introduceLabel.textAlignment = NSTextAlignmentCenter;
    introduceLabel.numberOfLines = 0;
    introduceLabel.textColor = UIColorMakeWithHex(@"#3C4267");
    introduceLabel.font = [UIFont systemFontOfSize:14];
    [self addSubview:introduceLabel];
    
    UIImageView *emptyImgView = [[UIImageView alloc]init];
    emptyImgView.image = [UIImage sr_sceneImageWithName:@"online_empty_placeHolder" ];
    [self addSubview:emptyImgView];
    
    [emptyImgView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(self);
        make.centerY.equalTo(self);
    }];
    
    [introduceLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(emptyImgView.mas_bottom);
        make.left.equalTo(self).offset(20);
        make.right.equalTo(self).offset(-20);
    }];
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
