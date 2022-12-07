//
//  AEACategoryView.m
//  AgoraEditAvatar
//
//  Created by FanPengpeng on 2022/9/20.
//

#import "AEACategoryView.h"
#import "AEACategoryTitleCell.h"


static NSString * const kTitleCellID = @"AEACategoryTitleCell";

@implementation AEACategoryItem

+ (instancetype)itemWithNormalImage:(UIImage *)normalImage selectedImage:(UIImage *)selectedImage {
    AEACategoryItem *item = [AEACategoryItem new];
    item.selectedImage = selectedImage;
    item.normalImage = normalImage;
    return item;
}

@end

@implementation AEACategoryViewLayout

+ (instancetype)defaultLayout {
    AEACategoryViewLayout *layout = [AEACategoryViewLayout new];
    CGFloat itemWidth = 40;
    UIEdgeInsets insets =  UIEdgeInsetsMake(5, 25, 5, 25);
    CGFloat spacing = ([UIScreen mainScreen].bounds.size.width - insets.left - insets.right - itemWidth * 5) / 4;
    layout.itemSize = CGSizeMake(itemWidth, itemWidth);
    layout.minSpacing = spacing;
    layout.contentInsets = insets;
    return layout;
}

@end

@interface AEACategoryView ()<UICollectionViewDataSource, UICollectionViewDelegateFlowLayout>
{
    NSInteger _selectedIndex;
}
@property (nonatomic, strong) UICollectionView *collectionView;

@property (nonatomic, strong)  UIView *lineView;

@property (nonatomic, strong) NSArray *dataArray;

@property (nonatomic, strong) AEACategoryViewLayout *categoryLayout;

@end

@implementation AEACategoryView

+ (instancetype)defaultCategoryView {
    return [self categoryViewWithLayout:[AEACategoryViewLayout defaultLayout]];
}

+ (instancetype)categoryViewWithLayout:(AEACategoryViewLayout *)layout {
    AEACategoryView *view = [AEACategoryView new];
    [view createSubviewsWithLayout:layout];
    return view;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    [self scrollIndicatorToSelectedIndexAnimated:NO];
}

- (void)createSubviewsWithLayout:(AEACategoryViewLayout *)categoryLayout {
    
    self.categoryLayout = categoryLayout;
    self.backgroundColor = [UIColor whiteColor];
    
    UICollectionViewFlowLayout *layout = [UICollectionViewFlowLayout new];
    layout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
    layout.itemSize = categoryLayout.itemSize;
    layout.minimumLineSpacing = categoryLayout.minSpacing;
    layout.sectionInset = categoryLayout.contentInsets;
    
    _collectionView = [[UICollectionView alloc] initWithFrame:CGRectZero
                                         collectionViewLayout:layout];
    _collectionView.delegate = self;
    _collectionView.dataSource = self;
    _collectionView.backgroundColor = [UIColor whiteColor];
    _collectionView.showsHorizontalScrollIndicator = NO;
    _collectionView.clipsToBounds = YES;
    
    [_collectionView registerClass:AEACategoryTitleCell.class forCellWithReuseIdentifier:kTitleCellID];
    
    UIView *lineView = [UIView new];
    self.lineView = lineView;
    lineView.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.1];
    
    [self addSubview:_collectionView];
    [self addSubview:lineView];
    
    _collectionView.translatesAutoresizingMaskIntoConstraints = NO;
    lineView.translatesAutoresizingMaskIntoConstraints = NO;
    
    [[_collectionView.topAnchor constraintEqualToAnchor:self.topAnchor] setActive:YES];
    [[_collectionView.leftAnchor constraintEqualToAnchor:self.leftAnchor constant:0] setActive:YES];
    [[_collectionView.rightAnchor constraintEqualToAnchor:self.rightAnchor constant:0] setActive:YES];
    [[_collectionView.bottomAnchor constraintEqualToAnchor:self.bottomAnchor] setActive:YES];
    [[_collectionView.heightAnchor constraintEqualToConstant:categoryLayout.itemSize.height + 14] setActive:YES];
    
    [[lineView.bottomAnchor constraintEqualToAnchor:self.bottomAnchor] setActive:YES];
    [[lineView.leftAnchor constraintEqualToAnchor:self.collectionView.leftAnchor] setActive:YES];
    [[lineView.rightAnchor constraintEqualToAnchor:self.collectionView.rightAnchor] setActive:YES];
    [[lineView.heightAnchor constraintEqualToConstant:1] setActive:YES];
}

- (void)setIndicator:(UIView *)indicator {
    _indicator = indicator;
    [self addSubview:_indicator];
}

- (void)setDefaultSelectedIndex:(NSInteger)defaultSelectedIndex {
    _defaultSelectedIndex = defaultSelectedIndex;
    _selectedIndex = defaultSelectedIndex;
    NSIndexPath *indexPath = [NSIndexPath indexPathForItem:defaultSelectedIndex inSection:0];
    if ([self.collectionView numberOfItemsInSection:0] <= defaultSelectedIndex) {
        return;
    }
    [self.collectionView selectItemAtIndexPath:indexPath animated:NO scrollPosition: UICollectionViewScrollPositionNone];
}

- (void)setTitles:(NSArray<NSString *> *)titles {
    _titles = titles;
    self.dataArray = titles;
    [self.collectionView reloadData];
}

- (void)setItems:(NSArray<AEACategoryItem *> *)items {
    _items = items;
    self.dataArray = items;
    [self.collectionView reloadData];
}

- (void)setShowBottomLine:(BOOL)showBottomLine {
    _showBottomLine = showBottomLine;
    self.lineView.hidden = !showBottomLine;
}

- (void)scrollIndicatorToSelectedIndexAnimated:(BOOL) animated {
    UICollectionViewCell *cell = [self.collectionView cellForItemAtIndexPath:[NSIndexPath indexPathForItem:_selectedIndex inSection:0]];
    if (cell) {
        void (^changeCenter)(void) = ^{
            self.indicator.center = CGPointMake(cell.center.x, self.bounds.size.height - self.indicator.bounds.size.height);
        };
        if (animated){
            [UIView animateWithDuration:0.2 animations:^{
                changeCenter();
            }];
        }else{
            changeCenter();
        }
    }else {
        CGFloat defaultSelectedCellCenterX = (self.categoryLayout.contentInsets.left + (_selectedIndex + 1) * self.categoryLayout.itemSize.width + self.categoryLayout.minSpacing * _selectedIndex ) * 0.5;
        self.indicator.center = CGPointMake(defaultSelectedCellCenterX, self.bounds.size.height - self.indicator.bounds.size.height);
    }
}

#pragma mark -- UICollectionViewDataSource & UICollectionViewDelegate

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.dataArray.count;
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    id data = self.dataArray[indexPath.item];
    AEACategoryTitleCell *titleCell = [collectionView dequeueReusableCellWithReuseIdentifier:kTitleCellID forIndexPath:indexPath];
    titleCell.titleFont = _titleFont;
    titleCell.titleSelectedFont = _titleSelectedFont;
    titleCell.titleColor = _titleColor;
    titleCell.titleSelectedColor = _titleSelectedColor;
    titleCell.title = (NSString *)data;
    return titleCell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    AEACategoryItem *item = self.dataArray[indexPath.item];
    if ([self.delegate respondsToSelector:@selector(categoryView:didSelectItem:index:)]) {
        [self.delegate categoryView:self didSelectItem:item index:indexPath.item];
    }
    [collectionView scrollToItemAtIndexPath:indexPath atScrollPosition:UICollectionViewScrollPositionCenteredHorizontally animated:YES];
    _selectedIndex = indexPath.item;
    [self scrollIndicatorToSelectedIndexAnimated:YES];
}


@end
