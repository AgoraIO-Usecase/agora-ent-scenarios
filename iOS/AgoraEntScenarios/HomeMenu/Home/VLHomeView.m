//
//  VLHomeView.m
//  VoiceOnLine
//

#import "VLHomeView.h"
#import "VLHomeItemView.h"
#import "VLHomeItemModel.h"
#import "VLMacroDefine.h"
#import "AgoraEntScenarios-Swift.h"
@import QMUIKit;
@import YYCategories;

@interface VLHomeView ()<UICollectionViewDelegate,UICollectionViewDataSource>

@property (nonatomic, weak) id <VLHomeViewDelegate>delegate;
@property (nonatomic) UICollectionView *menuList;
@property (nonatomic, strong) NSArray *itemsArray;
@property (nonatomic) NSArray *models;

@end

@implementation VLHomeView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLHomeViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        [self addSubview:self.menuList];
    }
    return self;
}

- (UICollectionViewFlowLayout *)flowLayout {
    UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
    CGFloat leftMargin = VLREALVALUE_WIDTH(20);
    CGFloat middleMargin = VLREALVALUE_WIDTH(11);
    CGFloat lineMargin = VLREALVALUE_WIDTH(14);
    CGFloat itemWidth = (SCREEN_WIDTH-2*leftMargin-middleMargin)/2.0;
    layout.itemSize = CGSizeMake(itemWidth, itemWidth*1.4);
    layout.minimumLineSpacing = lineMargin;
    layout.minimumInteritemSpacing = middleMargin;
    layout.sectionInset = UIEdgeInsetsMake(20, 20, 20, 20);
    return layout;
}

- (NSArray *)models {
    if (!_models) {
        _models = [VLHomeItemModel vj_modelArrayWithJson:self.itemsArray];
    }
    return _models;
}

- (UICollectionView *)menuList {
    if (!_menuList) {
        _menuList = [[UICollectionView alloc] initWithFrame:self.bounds collectionViewLayout:[self flowLayout]];
        _menuList.dataSource = self;
        _menuList.delegate = self;
        [_menuList registerClass:[HomeMenuCell class] forCellWithReuseIdentifier:@"HomeMenuCell"];
    }
    return _menuList;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.itemsArray.count;
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    HomeMenuCell *cell = (HomeMenuCell *)[collectionView dequeueReusableCellWithReuseIdentifier:@"HomeMenuCell" forIndexPath:indexPath];
    [cell refreshWithItem:self.models[indexPath.row]];
    return cell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    [collectionView deselectItemAtIndexPath:indexPath animated:YES];
    if (self.delegate && [self.delegate respondsToSelector:@selector(itemClickAction:)]) {
        [self.delegate itemClickAction:(int)indexPath.row];
    }
}

- (NSArray *)itemsArray {
    if (!_itemsArray) {
        _itemsArray = @[
            @{@"bgImgStr":@"home_KTV_bg",@"iconImgStr":@"home_KTV_icon",@"titleStr":@"语聊房",@"subTitleStr":@""},
            @{@"bgImgStr":@"home_KTV_bg",@"iconImgStr":@"home_KTV_icon",@"titleStr":@"在线K歌房",@"subTitleStr":@""},
            @{@"bgImgStr":@"home_live_bg",@"iconImgStr":@"home_live_icon",@"titleStr":@"元直播",@"subTitleStr":NSLocalizedString(@"敬请期待", nil)},
            @{@"bgImgStr":@"home_talk_bg",@"iconImgStr":@"home_talk_icon",@"titleStr":@"元语聊",@"subTitleStr":NSLocalizedString(@"敬请期待", nil)},
            @{@"bgImgStr":@"home_game_bg",@"iconImgStr":@"home_game_icon",@"titleStr":@"互动游戏",@"subTitleStr":NSLocalizedString(@"敬请期待", nil)}
        ];
    }
    return _itemsArray;
}

@end
