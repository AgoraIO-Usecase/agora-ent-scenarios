//
//  VLPopSelBgView.m
//  VoiceOnLine
//

#import "VLPopSelBgView.h"
#import "VLKTVSelBgModel.h"
#import "VLFontUtils.h"
#import "VLMacroDefine.h"
#import "KTVMacro.h"
@import QMUIKit;
@import YYCategories;

@interface VLPopSelBgView ()<UICollectionViewDataSource,UICollectionViewDelegate>

@property(nonatomic, weak) id <VLPopSelBgViewDelegate>delegate;
@property (nonatomic, strong) UICollectionView *collectionView;
@property (nonatomic, strong) NSArray *picsArray;
@property (nonatomic, strong) NSArray *picsModelArray;

@end

@implementation VLPopSelBgView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLPopSelBgViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    
    self.picsModelArray = [VLKTVSelBgModel vj_modelArrayWithJson:self.picsArray];

    UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-200)*0.5, 20, 200, 22)];
    titleLabel.text = KTVLocalizedString(@"MV");
    titleLabel.font = VLUIFontMake(16);
    titleLabel.textAlignment = NSTextAlignmentCenter;
    titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:titleLabel];
    
    UICollectionViewFlowLayout *flowLayOut = [[UICollectionViewFlowLayout alloc]init];
    flowLayOut.scrollDirection = UICollectionViewScrollDirectionVertical;
    
    CGFloat leftMargin = 20;
    CGFloat middleMargin = 10;
    CGFloat itemW = (SCREEN_WIDTH-2*leftMargin-2*middleMargin)/3.0;
    CGFloat itemH = itemW*0.75;
    flowLayOut.itemSize = CGSizeMake(itemW, itemH);
    flowLayOut.minimumInteritemSpacing = middleMargin;
    flowLayOut.minimumLineSpacing = 15;
    
    self.collectionView = [[UICollectionView alloc] initWithFrame:CGRectMake(0, titleLabel.bottom+15, SCREEN_WIDTH, self.height-20-22-15-kSafeAreaBottomHeight) collectionViewLayout:flowLayOut];
    self.collectionView.dataSource = self;
    self.collectionView.delegate = self;
    self.collectionView.showsHorizontalScrollIndicator = false;
    self.collectionView.showsVerticalScrollIndicator = false;
    self.collectionView.backgroundColor = UIColorClear;
    self.collectionView.contentInset = UIEdgeInsetsMake(0, 20, 0, 20);
    if (@available(iOS 11, *)) {
        self.collectionView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
    }
    [self.collectionView registerClass:[VLKTVSelBgCell class] forCellWithReuseIdentifier:[VLKTVSelBgCell className]];
    [self addSubview:self.collectionView];
}

#pragma mark - UITableViewDelegate,UITableViewDataSource
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.picsModelArray.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    VLKTVSelBgCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:[VLKTVSelBgCell className] forIndexPath:indexPath];
    cell.selBgModel = self.picsModelArray[indexPath.item];
    return cell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    for (VLKTVSelBgModel *model in self.picsModelArray) {
        model.ifSelect = NO;
    }
    VLKTVSelBgModel *selBgModel = self.picsModelArray[indexPath.row];
    selBgModel.ifSelect = YES;
    [self.collectionView reloadData];
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVLPopSelBgView:tappedWithAction:atIndex:)]) {
        [self.delegate onVLPopSelBgView:self tappedWithAction:selBgModel atIndex:indexPath.item];
    }
}

- (void)setSelBgModel:(VLKTVSelBgModel *)selBgModel {
    _selBgModel = selBgModel;
    for (VLKTVSelBgModel *model in self.picsModelArray) {
        if ([_selBgModel.imageName isEqualToString:model.imageName]) {
            model.ifSelect = YES;
        }
    }
    if (!selBgModel) {
        VLKTVSelBgModel *picModel = self.picsModelArray.firstObject;
        picModel.ifSelect = YES;
    }
    [self.collectionView reloadData];
}

- (NSArray *)picsArray {
    if (!_picsArray) {
        _picsArray = @[@{@"imageName":@"ktv_mvbg0",@"ifSelect":@(false)},@{@"imageName":@"ktv_mvbg1",@"ifSelect":@(false)},@{@"imageName":@"ktv_mvbg2",@"ifSelect":@(false)},@{@"imageName":@"ktv_mvbg3",@"ifSelect":@(false)},@{@"imageName":@"ktv_mvbg4",@"ifSelect":@(false)},@{@"imageName":@"ktv_mvbg5",@"ifSelect":@(false)},@{@"imageName":@"ktv_mvbg6",@"ifSelect":@(false)},@{@"imageName":@"ktv_mvbg7",@"ifSelect":@(false)},@{@"imageName":@"ktv_mvbg8",@"ifSelect":@(false)}];
    }
    return _picsArray;
}


@end


@implementation VLKTVSelBgCell

- (instancetype)initWithFrame:(CGRect)frame {
    
    if (self = [super initWithFrame:frame]) {
        [self setupView];
    }
    return self;
}
#pragma mark - Intial Methods
- (void)setupView {
    
    self.picImgView = [[UIImageView alloc]initWithFrame:CGRectMake(0, 0, self.width, self.height)];
    self.picImgView.userInteractionEnabled = YES;
    [self.contentView addSubview:self.picImgView];
    
    self.selIcon = [[UIImageView alloc]initWithFrame:CGRectMake(self.width-18, self.height-17, 18, 17)];
    self.selIcon.image = [UIImage sceneImageWithName:@"ktv_selbg_icon"];
    [self.contentView addSubview:self.selIcon];
}

- (void)setSelBgModel:(VLKTVSelBgModel *)selBgModel {
    _selBgModel = selBgModel;
    self.picImgView.image = [UIImage sceneImageWithName:selBgModel.imageName];
    self.selIcon.hidden = !selBgModel.ifSelect;
}

@end

