//
//  VLKTVKindsView.m
//  VoiceOnLine
//

#import "VLKTVKindsView.h"
#import "VLKTVKindsCell.h"
#import "KTVMacro.h"
@import Masonry;
@import QMUIKit;

@interface VLKTVKindsView() <UICollectionViewDataSource,UICollectionViewDelegate>

@property (nonatomic, strong) UICollectionView *collectionView;
@property (nonatomic, strong) VLKTVKindsModel *currentModel;
@property (nonatomic, assign) long selectedOne;

@end

@implementation VLKTVKindsView

- (instancetype)init {
    if (self = [super init]) {
        [self initSubViews];
        [self addSubViewConstraints];
        _selectedOne = 0;
    }
    return self;
}

- (void)initSubViews {
    [self addSubview:self.collectionView];
}

- (void)addSubViewConstraints {
    [self.collectionView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self);
        make.height.mas_equalTo(50);
    }];
}

- (void)setList:(NSArray<VLKTVKindsModel *> *)list {
    _list = list;
    [self.collectionView reloadData];
}

#pragma mark - UITableViewDelegate,UITableViewDataSource
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return _list.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    VLKTVKindsCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"VLKTVKindsCell" forIndexPath:indexPath];
    cell.model = _list[indexPath.row];
    if(indexPath.item == _selectedOne) {
        [cell setSelected:YES];
    }
    else {
        [cell setSelected:NO];
    }
    return  cell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    VLKTVKindsModel *model = _list[indexPath.row];
    if (model == self.currentModel) {
        return;
    }
    
    _selectedOne = indexPath.row;
    
    for(int i=0; i<[_list count]; i++) {
        VLKTVKindsModel *model = _list[i];
        model.selected = NO;
    }
    self.currentModel.selected = NO;
    
    model.selected = YES;
    self.currentModel = model;
    [collectionView reloadData];
    
    if ([self.delegate respondsToSelector:@selector(kindsViewDidClickIndex:)]) {
        [self.delegate kindsViewDidClickIndex:indexPath.row];
    }
}

- (UICollectionView *)collectionView {
    if (!_collectionView) {
        UICollectionViewFlowLayout *flowLayOut = [[UICollectionViewFlowLayout alloc]init];
        flowLayOut.scrollDirection = UICollectionViewScrollDirectionHorizontal;
        flowLayOut.itemSize = CGSizeMake(75, 50);
        flowLayOut.minimumInteritemSpacing = 12;
//        flowLayOut.minimumLineSpacing = 12;
        
        _collectionView = [[UICollectionView alloc] initWithFrame:CGRectZero collectionViewLayout:flowLayOut];
        _collectionView.dataSource = self;
        _collectionView.delegate = self;
        _collectionView.showsHorizontalScrollIndicator = false;
        _collectionView.showsVerticalScrollIndicator = false;
        _collectionView.backgroundColor = UIColorClear;
        _collectionView.contentInset = UIEdgeInsetsMake(0, 20, 0, 20);
        if (@available(iOS 11, *)) {
            _collectionView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
        }
        [_collectionView registerClass:[VLKTVKindsCell class] forCellWithReuseIdentifier:@"VLKTVKindsCell"];
    }
    return _collectionView;
}

@end

@implementation VLKTVKindsModel

+ (NSArray<VLKTVKindsModel *> *)kinds {
    NSArray *titlesArray = @[
        KTVLocalizedString(@"原唱"),
        KTVLocalizedString(@"KTV"),
        KTVLocalizedString(@"演唱会"),
        KTVLocalizedString(@"录音棚"),
        KTVLocalizedString(@"留声机"),
        KTVLocalizedString(@"空旷"),
        KTVLocalizedString(@"空灵"),
        KTVLocalizedString(@"流行"),
        KTVLocalizedString(@"R&B")];
    NSArray *imagesArray = @[@"ktv_ console_setting1",@"ktv_ console_setting2",@"ktv_ console_setting3",@"ktv_ console_setting4",@"ktv_ console_setting1",@"ktv_ console_setting2",@"ktv_ console_setting3",@"ktv_ console_setting4",@"ktv_ console_setting1"];
    NSMutableArray *array = [NSMutableArray array];
    for (int i = 0; i <titlesArray.count ; i++) {
        VLKTVKindsModel *model = [[VLKTVKindsModel alloc] init];
        model.title = titlesArray[i];
        model.imageName = imagesArray[i];
        if(i == 0) {
            model.selected = YES;
        }
        [array addObject:model];
    }
    return array;
}

@end
