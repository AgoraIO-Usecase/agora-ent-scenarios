//
//  VLSearchEmptyView.m
//  VoiceOnLine
//

#import "VLSBGSearchEmptyView.h"
#import "VLMacroDefine.h"
#import "SBGMacro.h"
@import YYCategories;

@implementation VLSBGSearchEmptyView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        [self setupView];
    }
    return self;
}

- (void)setupView {
    UILabel *emptyLabel = [[UILabel alloc]initWithFrame:CGRectMake((self.width-200)*0.5, VLREALVALUE_WIDTH(60), 200, 20)];
    emptyLabel.text = SBGLocalizedString(@"未找到相关结果");
    emptyLabel.textAlignment = NSTextAlignmentCenter;
    emptyLabel.font = UIFontMake(14);
    emptyLabel.textColor = UIColorMakeWithHex(@"#979CBB");
    [self addSubview:emptyLabel];
}

@end
