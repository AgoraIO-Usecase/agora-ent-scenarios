//
//  VLPopSelBgView.m
//  VoiceOnLine
//

#import "VLSRPopSelBgView.h"
#import "VLSRSelBgModel.h"
#import "VLFontUtils.h"
#import "VLMacroDefine.h"
#import "AESMacro.h"

@interface VLSRPopSelBgView ()<UICollectionViewDataSource,UICollectionViewDelegate>

@property(nonatomic, weak) id <VLSRPopSelBgViewDelegate>delegate;
@property (nonatomic, strong) UICollectionView *collectionView;
@property (nonatomic, strong) NSArray *picsArray;
@property (nonatomic, strong) NSArray *picsModelArray;

@end

@implementation VLSRPopSelBgView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRPopSelBgViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    self.picsModelArray = [VLSRSelBgModel vj_modelArrayWithJson:self.picsArray];

    UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-200)*0.5, 20, 200, 22)];
    titleLabel.text = SRLocalizedString(@"MV");
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
    [self.collectionView registerClass:[VLSRSelBgCell class] forCellWithReuseIdentifier:[VLSRSelBgCell className]];
    [self addSubview:self.collectionView];
}

#pragma mark - UITableViewDelegate,UITableViewDataSource
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.picsModelArray.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    VLSRSelBgCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:[VLSRSelBgCell className] forIndexPath:indexPath];
    cell.selBgModel = self.picsModelArray[indexPath.item];
    return cell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    for (VLSRSelBgModel *model in self.picsModelArray) {
        model.isSelect = NO;
    }
    VLSRSelBgModel *selBgModel = self.picsModelArray[indexPath.row];
    selBgModel.isSelect = YES;
    [self.collectionView reloadData];
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVLPopSelBgView:tappedWithAction:atIndex:)]) {
        [self.delegate onVLPopSelBgView:self tappedWithAction:selBgModel atIndex:indexPath.item];
    }
}

- (void)setSelBgModel:(VLSRSelBgModel *)selBgModel {
    _selBgModel = selBgModel;
    for (VLSRSelBgModel *model in self.picsModelArray) {
        if ([_selBgModel.imageName isEqualToString:model.imageName]) {
            model.isSelect = YES;
        }
    }
    if (!selBgModel) {
        VLSRSelBgModel *picModel = self.picsModelArray.firstObject;
        picModel.isSelect = YES;
    }
    [self.collectionView reloadData];
}

- (NSArray *)picsArray {
    if (!_picsArray) {
        _picsArray = @[
        @{@"imageName":@"SR_mvbg0",@"ifSelect":@(false)},
        @{@"imageName":@"SR_mvbg1",@"ifSelect":@(false)},
        @{@"imageName":@"SR_mvbg2",@"ifSelect":@(false)},
        @{@"imageName":@"SR_mvbg3",@"ifSelect":@(false)},
        @{@"imageName":@"SR_mvbg4",@"ifSelect":@(false)},
        @{@"imageName":@"SR_mvbg5",@"ifSelect":@(false)},
        @{@"imageName":@"SR_mvbg6",@"ifSelect":@(false)},
        @{@"imageName":@"SR_mvbg7",@"ifSelect":@(false)},
        @{@"imageName":@"SR_mvbg8",@"ifSelect":@(false)}
        ];
    }
    return _picsArray;
}

@end

@implementation VLSRSelBgCell

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
    self.selIcon.image = [UIImage sceneImageWithName:@"SR_selbg_icon"];
    [self.contentView addSubview:self.selIcon];
}

- (void)setSelBgModel:(VLSRSelBgModel *)selBgModel {
    _selBgModel = selBgModel;
    self.picImgView.image = [UIImage sceneImageWithName:selBgModel.imageName];
    self.selIcon.hidden = !selBgModel.isSelect;
}

@end

