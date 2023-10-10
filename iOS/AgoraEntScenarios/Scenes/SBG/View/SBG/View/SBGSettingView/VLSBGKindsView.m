//
//  VLSBGKindsView.m
//  VoiceOnLine
//

#import "VLSBGKindsView.h"
#import "VLSBGKindsCell.h"
#import "SBGMacro.h"
@import Masonry;

@interface VLSBGKindsView() <UICollectionViewDataSource,UICollectionViewDelegate>

@property (nonatomic, strong) UICollectionView *collectionView;
@property (nonatomic, strong) VLSBGKindsModel *currentModel;
@property (nonatomic, assign) long selectedOne;

@end

@implementation VLSBGKindsView

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

- (void)setList:(NSArray<VLSBGKindsModel *> *)list {
    _list = list;
    [self.collectionView reloadData];
}

#pragma mark - UITableViewDelegate,UITableViewDataSource
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return _list.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    VLSBGKindsCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"VLSBGKindsCell" forIndexPath:indexPath];
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
    VLSBGKindsModel *model = _list[indexPath.row];
    if (model == self.currentModel) {
        return;
    }
    
    _selectedOne = indexPath.row;
    
    for(int i=0; i<[_list count]; i++) {
        VLSBGKindsModel *model = _list[i];
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
        [_collectionView registerClass:[VLSBGKindsCell class] forCellWithReuseIdentifier:@"VLSBGKindsCell"];
    }
    return _collectionView;
}

@end

@implementation VLSBGKindsModel

+ (NSArray<VLSBGKindsModel *> *)kinds {
    NSArray *titlesArray = @[
        SBGLocalizedString(@"原唱"),
        SBGLocalizedString(@"RS"),
        SBGLocalizedString(@"演唱会"),
        SBGLocalizedString(@"录音棚"),
        SBGLocalizedString(@"留声机"),
        SBGLocalizedString(@"空旷"),
        SBGLocalizedString(@"空灵"),
        SBGLocalizedString(@"流行"),
        SBGLocalizedString(@"R&B")];
    NSArray *imagesArray = @[@"ktv_console_setting1",
                             @"ktv_console_setting2",
                             @"ktv_console_setting3",
                             @"ktv_console_setting4",
                             @"ktv_console_setting1",
                             @"ktv_console_setting2",
                             @"ktv_console_setting3",
                             @"ktv_console_setting4",
                             @"ktv_console_setting1"];
    NSMutableArray *array = [NSMutableArray array];
    for (int i = 0; i <titlesArray.count ; i++) {
        VLSBGKindsModel *model = [[VLSBGKindsModel alloc] init];
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
