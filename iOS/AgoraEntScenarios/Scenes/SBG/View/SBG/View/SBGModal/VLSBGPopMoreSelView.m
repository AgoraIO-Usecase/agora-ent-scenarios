//
//  VLPopMoreSelView.m
//  VoiceOnLine
//

#import "VLSBGPopMoreSelView.h"
#import "VLFontUtils.h"
#import "VLMacroDefine.h"
#import "AgoraEntScenarios-Swift.h"
#import "SBGMacro.h"
@import Masonry;

@interface VLSBGPopMoreSelView ()

@property(nonatomic, weak) id <VLSBGPopMoreSelViewDelegate>delegate;

@end

@implementation VLSBGPopMoreSelView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGPopMoreSelViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    
    UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-200)*0.5, 20, 200, 22)];
    titleLabel.text = SBGLocalizedString(@"sbg_more_actions");
    titleLabel.font = VLUIFontMake(16);
    titleLabel.textAlignment = NSTextAlignmentCenter;
    titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:titleLabel];
    
    CGFloat leftMargin = VLREALVALUE_WIDTH(52);
   // CGFloat leftMargin = VLREALVALUE_WIDTH(80);
    CGFloat itemWH = 64;
    CGFloat btnItemWH = 40;
    CGFloat middleMargin = (SCREEN_WIDTH-itemWH*3-2*leftMargin)/2.0;
   // CGFloat middleMargin = (SCREEN_WIDTH-itemWH*2-2*leftMargin);
    CGFloat itemY = titleLabel.bottom + 26;
    
    NSArray *itemsArray = @[@"ktv_more_belcantoIcon",@"ktv_more_soundIcon",@"ktv_more_mvIcon"];
    NSArray *titlesArray = @[
        SBGLocalizedString(@"sbg_voice_effect"),
        SBGLocalizedString(@"sbg_per_setting"),
        SBGLocalizedString(@"MV")];
    for (int i=0; i<itemsArray.count; i++) {
        UIView *itemBgView = [[UIView alloc]initWithFrame:CGRectMake(leftMargin+(itemWH+middleMargin)*i, itemY, itemWH, itemWH)];
        itemBgView.layer.cornerRadius = itemWH*0.5;
        itemBgView.layer.masksToBounds = YES;
        itemBgView.backgroundColor = UIColorMakeWithRGBA(4, 9, 37, 0.35);
        [self addSubview:itemBgView];
    
        UIButton *itemBtn = [[UIButton alloc]initWithFrame:CGRectMake(leftMargin+(itemWH+middleMargin)*i+11, itemY+12, btnItemWH, btnItemWH)];
        [itemBtn setImage:[UIImage sbg_sceneImageWithName:itemsArray[i]] forState:UIControlStateNormal];
        itemBtn.tag = i;
        [itemBtn addTarget:self action:@selector(itemBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:itemBtn];
        
        NSString *title = titlesArray[i];
        UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake(leftMargin+(itemWH+middleMargin)*i, itemY+itemWH+8, itemWH, 17)];
        titleLabel.text = title;
        titleLabel.textAlignment = NSTextAlignmentCenter;
        titleLabel.font = UIFontMake(12);
        titleLabel.textColor = UIColorMakeWithHex(@"#C6C4DE");
        [self addSubview:titleLabel];
        
    }
}

- (void)itemBtnClickEvent:(UIButton *)itemBtn {
    if ([self.delegate respondsToSelector:@selector(onVLSBGMoreSelView:btnTapped:withValue:)]) {
        [self.delegate onVLSBGMoreSelView:self btnTapped:itemBtn withValue:itemBtn.tag];
    }
}

@end
