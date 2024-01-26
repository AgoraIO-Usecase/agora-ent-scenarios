//
//  VLPopMoreSelView.m
//  VoiceOnLine
//

#import "VLPopMoreSelView.h"
#import "AgoraEntScenarios-Swift.h"
@import Masonry;

@interface VLPopMoreSelView ()

@property(nonatomic, weak) id <VLPopMoreSelViewDelegate>delegate;

@end

@implementation VLPopMoreSelView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLPopMoreSelViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    
    UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-200)*0.5, 20, 200, 22)];
    titleLabel.text = KTVLocalizedString(@"ktv_more_actions");
    titleLabel.font = [UIFont systemFontOfSize:16];
    titleLabel.textAlignment = NSTextAlignmentCenter;
    titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:titleLabel];
    
  //  CGFloat leftMargin = VLREALVALUE_WIDTH(52);
    CGFloat leftMargin = VLREALVALUE_WIDTH(80);
    CGFloat itemWH = 64;
    CGFloat btnItemWH = 40;
  //  CGFloat middleMargin = (SCREEN_WIDTH-itemWH*3-2*leftMargin)/2.0;
    CGFloat middleMargin = (SCREEN_WIDTH-itemWH*2-2*leftMargin);
    CGFloat itemY = titleLabel.bottom + 26;

    NSArray *itemsArray = @[@"ktv_more_soundIcon",@"ktv_more_mvIcon"];
    NSArray *titlesArray = @[
        KTVLocalizedString(@"ktv_per_setting"),
    ];
    
    UIView *itemBgView = [[UIView alloc]initWithFrame:CGRectMake((self.bounds.size.width - itemWH)/2.0, itemY, itemWH, itemWH)];
    itemBgView.layer.cornerRadius = itemWH*0.5;
    itemBgView.layer.masksToBounds = YES;
    itemBgView.backgroundColor = UIColorMakeWithRGBA(4, 9, 37, 0.35);
    [self addSubview:itemBgView];

    UIButton *itemBtn = [[UIButton alloc]initWithFrame:CGRectMake((self.bounds.size.width - itemWH)/2.0+11, itemY+12, btnItemWH, btnItemWH)];
    [itemBtn setImage:[UIImage ktv_sceneImageWithName:itemsArray[0] ] forState:UIControlStateNormal];
    itemBtn.tag = 0;
    [itemBtn addTarget:self action:@selector(itemBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:itemBtn];
    
    NSString *title = titlesArray[0];
    UILabel *textLabel = [[UILabel alloc]initWithFrame:CGRectMake((self.bounds.size.width - itemWH)/2.0, itemY+itemWH+8, itemWH, 17)];
    textLabel.text = title;
    textLabel.textAlignment = NSTextAlignmentCenter;
    textLabel.font = UIFontMake(12);
    textLabel.textColor = UIColorMakeWithHex(@"#C6C4DE");
    [self addSubview:textLabel];
    
    
  //  for (int i=0; i<itemsArray.count; i++) {
//        UIView *itemBgView = [[UIView alloc]initWithFrame:CGRectMake(leftMargin+(itemWH+middleMargin)*i, itemY, itemWH, itemWH)];
//        itemBgView.layer.cornerRadius = itemWH*0.5;
//        itemBgView.layer.masksToBounds = YES;
//        itemBgView.backgroundColor = UIColorMakeWithRGBA(4, 9, 37, 0.35);
//        [self addSubview:itemBgView];
//
//        UIButton *itemBtn = [[UIButton alloc]initWithFrame:CGRectMake(leftMargin+(itemWH+middleMargin)*i+11, itemY+12, btnItemWH, btnItemWH)];
//        [itemBtn setImage:[UIImage ktv_sceneImageWithName:itemsArray[i]] forState:UIControlStateNormal];
//        itemBtn.tag = i;
//        [itemBtn addTarget:self action:@selector(itemBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
//        [self addSubview:itemBtn];
//
//        NSString *title = titlesArray[i];
//        UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake(leftMargin+(itemWH+middleMargin)*i, itemY+itemWH+8, itemWH, 17)];
//        titleLabel.text = title;
//        titleLabel.textAlignment = NSTextAlignmentCenter;
//        titleLabel.font = UIFontMake(12);
//        titleLabel.textColor = UIColorMakeWithHex(@"#C6C4DE");
//        [self addSubview:titleLabel];
   // }
}

- (void)itemBtnClickEvent:(UIButton *)itemBtn {
    if ([self.delegate respondsToSelector:@selector(onVLKTVMoreSelView:btnTapped:withValue:)]) {
        [self.delegate onVLKTVMoreSelView:self btnTapped:itemBtn withValue:itemBtn.tag];
    }
}

@end
