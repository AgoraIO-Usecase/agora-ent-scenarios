//
//  VLHomeView.m
//  VoiceOnLine
//

#import "VLHomeView.h"
#import "VLHomeItemView.h"
#import "VLHomeItemModel.h"
#import "VLMacroDefine.h"
@import QMUIKit;
@import YYCategories;

@interface VLHomeView ()

@property (nonatomic, weak) id <VLHomeViewDelegate>delegate;
@property (nonatomic, strong) UIScrollView* contentView;
@property (nonatomic, strong) NSArray *itemsArray;

@end

@implementation VLHomeView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLHomeViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    
    _contentView = [[UIScrollView alloc] initWithFrame:self.bounds];
    [self addSubview:_contentView];
    
    CGFloat leftMargin = VLREALVALUE_WIDTH(20);
    CGFloat middleMargin = VLREALVALUE_WIDTH(11);
    CGFloat lineMargin = VLREALVALUE_WIDTH(14);
    CGFloat itemWidth = (SCREEN_WIDTH-2*leftMargin-middleMargin)/2.0;
    CGFloat itemHeight = itemWidth * 1.4;
    CGFloat topY = kTopNavHeight;
    
    NSArray *modelsArray = [VLHomeItemModel vj_modelArrayWithJson:self.itemsArray];
    CGFloat bottom = 0;
    for (int i=0; i<modelsArray.count; i++) {
        int row = i/2;
        int col = i%2;
        
        VLHomeItemView *itemView = [[VLHomeItemView alloc]initWithFrame:CGRectMake(leftMargin+(middleMargin+itemWidth)*col, topY+row*(itemHeight+lineMargin), itemWidth, itemHeight)];
        itemView.itemModel = modelsArray[i];
        UITapGestureRecognizer *tapGes = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(itemTapEvent:)];
        itemView.tag = i;
        itemView.userInteractionEnabled = YES;
        [itemView addGestureRecognizer:tapGes];
        bottom = MAX(bottom, itemView.bottom);
        [_contentView addSubview:itemView];
    }
    [_contentView setContentSize:CGSizeMake(_contentView.width, bottom)];
}

- (void)itemTapEvent:(UITapGestureRecognizer *)tapGes{
    VLHomeView *itemView = (VLHomeView *)tapGes.view;
    if (self.delegate && [self.delegate respondsToSelector:@selector(itemClickAction:)]) {
        [self.delegate itemClickAction:(int)itemView.tag];
    }
}

- (NSArray *)itemsArray {
    if (!_itemsArray) {
        _itemsArray = @[
            @{@"bgImgStr":@"home_KTV_bg",@"iconImgStr":@"home_KTV_icon",@"titleStr":@"在线K歌房",@"subTitleStr":@""},
            @{@"bgImgStr":@"home_live_bg",@"iconImgStr":@"home_live_icon",@"titleStr":@"元直播",@"subTitleStr":NSLocalizedString(@"敬请期待", nil)},
            @{@"bgImgStr":@"home_talk_bg",@"iconImgStr":@"home_talk_icon",@"titleStr":@"元语聊",@"subTitleStr":NSLocalizedString(@"敬请期待", nil)},
            @{@"bgImgStr":@"home_game_bg",@"iconImgStr":@"home_game_icon",@"titleStr":@"互动游戏",@"subTitleStr":NSLocalizedString(@"敬请期待", nil)}
        ];
    }
    return _itemsArray;
}

@end
