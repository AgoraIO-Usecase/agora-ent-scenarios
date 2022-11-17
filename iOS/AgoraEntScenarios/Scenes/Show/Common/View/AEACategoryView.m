//
//  AEACategoryView.m
//  AgoraEditAvatar
//
//  Created by FanPengpeng on 2022/9/20.
//

#import "AEACategoryView.h"
#import "AEACategoryImageCell.h"
#import "AEACategoryTitleCell.h"


static NSString * const kTitleCellID = @"AEACategoryTitleCell";
static NSString * const kImageCellID = @"AEACategoryImageCell";

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

@property (nonatomic, strong) UICollectionView *collectionView;

@property (nonatomic, strong)  UIView *lineView;

@property (nonatomic, strong) NSArray *dataArray;

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

- (void)createSubviewsWithLayout:(AEACategoryViewLayout *)categoryLayout {
    
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
    [_collectionView registerClass:AEACategoryImageCell.class forCellWithReuseIdentifier:kImageCellID];
    
    UIView *lineView = [UIView new];
    self.lineView = lineView;
    lineView.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.1];
    
    [self addSubview:_collectionView];
    [self addSubview:lineView];
    
    _collectionView.translatesAutoresizingMaskIntoConstraints = NO;
    lineView.translatesAutoresizingMaskIntoConstraints = NO;
    
    [[_collectionView.topAnchor constraintEqualToAnchor:self.topAnchor] setActive:YES];
    [[_collectionView.leftAnchor constraintEqualToAnchor:self.leftAnchor constant:5] setActive:YES];
    [[_collectionView.rightAnchor constraintEqualToAnchor:self.rightAnchor constant:-5] setActive:YES];
    [[_collectionView.bottomAnchor constraintEqualToAnchor:self.bottomAnchor] setActive:YES];
    [[_collectionView.heightAnchor constraintEqualToConstant:categoryLayout.itemSize.height + 14] setActive:YES];
    
    [[lineView.bottomAnchor constraintEqualToAnchor:self.bottomAnchor] setActive:YES];
    [[lineView.leftAnchor constraintEqualToAnchor:self.collectionView.leftAnchor] setActive:YES];
    [[lineView.rightAnchor constraintEqualToAnchor:self.collectionView.rightAnchor] setActive:YES];
    [[lineView.heightAnchor constraintEqualToConstant:1] setActive:YES];
}

- (void)setDefaultSelectedIndex:(NSInteger)defaultSelectedIndex {
    _defaultSelectedIndex = defaultSelectedIndex;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        NSIndexPath *indexPath = [NSIndexPath indexPathForItem:defaultSelectedIndex inSection:0];
        if ([self.collectionView numberOfItemsInSection:0] <= defaultSelectedIndex) {
            return;
        }
        [self.collectionView selectItemAtIndexPath:indexPath animated:NO scrollPosition: UICollectionViewScrollPositionLeft];
    });
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

#pragma mark -- UICollectionViewDataSource & UICollectionViewDelegate

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.dataArray.count;
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    id data = self.dataArray[indexPath.item];
    if ([data isKindOfClass:[NSString class]]) {
        AEACategoryTitleCell *titleCell = [collectionView dequeueReusableCellWithReuseIdentifier:kTitleCellID forIndexPath:indexPath];
        titleCell.title = (NSString *)data;
        return titleCell;
    }
    
    AEACategoryItem *item = data;
    AEACategoryImageCell *imageCell = [collectionView dequeueReusableCellWithReuseIdentifier:kImageCellID forIndexPath:indexPath];
    imageCell.selectedImage = item.selectedImage;
    imageCell.normalImage = item.normalImage;
    return imageCell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    AEACategoryItem *item = self.dataArray[indexPath.item];
    if ([self.delegate respondsToSelector:@selector(categoryView:didSelectItem:index:)]) {
        [self.delegate categoryView:self didSelectItem:item index:indexPath.item];
    }
    [collectionView scrollToItemAtIndexPath:indexPath atScrollPosition:UICollectionViewScrollPositionCenteredHorizontally animated:YES];
}


@end
