//
//  VLChooseBelcantoView.m
//  VoiceOnLine
//

#import "VLSBGAudioEffectPicker.h"
#import "VLSBGBelcantoModel.h"
#import "SBGMacro.h"
#import "LSTPopView+SBGModal.h"

@interface VLSBGAudioEffectPicker ()<UICollectionViewDataSource,UICollectionViewDelegate>

@property(nonatomic, weak) id <VLSBGAudioEffectPickerDelegate>delegate;
@property (nonatomic, strong) UICollectionView *collectionView;
@property (nonatomic, strong) NSArray *itemsArray;
@property (nonatomic, strong) NSArray *itemsModelArray;
@property (nonatomic, assign) NSInteger indexValue;

@end


@implementation VLSBGAudioEffectPicker

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGAudioEffectPickerDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        self.indexValue = -1;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    
    self.itemsModelArray = [VLSBGBelcantoModel vj_modelArrayWithJson:self.itemsArray];
    
    VLHotSpotBtn *backBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(20, 20, 20, 20)];
    [backBtn setImage:[UIImage sbg_sceneImageWithName:@"ktv_back_whiteIcon"] forState:UIControlStateNormal];
    [backBtn addTarget:self action:@selector(backBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:backBtn];
    
    UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-200)*0.5, 20, 200, 22)];
    titleLabel.text = SBGLocalizedString(@"sbg_beauty_voice");
    titleLabel.font = [UIFont systemFontOfSize:16];
    titleLabel.textAlignment = NSTextAlignmentCenter;
    titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:titleLabel];
    
    UICollectionViewFlowLayout *flowLayOut = [[UICollectionViewFlowLayout alloc]init];
    flowLayOut.scrollDirection = UICollectionViewScrollDirectionHorizontal;
    
    CGFloat middleMargin = 25;
    CGFloat itemW = 60;
    CGFloat itemH = 80;
    flowLayOut.itemSize = CGSizeMake(itemW, itemH);
    flowLayOut.minimumInteritemSpacing = middleMargin;

    self.collectionView = [[UICollectionView alloc] initWithFrame:CGRectMake(0, titleLabel.bottom+25, SCREEN_WIDTH,85) collectionViewLayout:flowLayOut];
    self.collectionView.dataSource = self;
    self.collectionView.delegate = self;
    self.collectionView.showsHorizontalScrollIndicator = false;
    self.collectionView.showsVerticalScrollIndicator = false;
    self.collectionView.backgroundColor = UIColorClear;
    self.collectionView.contentInset = UIEdgeInsetsMake(0, 25, 0, 10);
    if (@available(iOS 11, *)) {
        self.collectionView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
    }
    [self.collectionView registerClass:[VLSBGAudioEffectCell class] forCellWithReuseIdentifier:[VLSBGAudioEffectCell className]];
    [self addSubview:self.collectionView];
    
}

- (void)backBtnClickEvent:(id)sender {
    if ([self.delegate respondsToSelector:@selector(onVLChooseBelcantoView:backBtnTapped:)]) {
        [self.delegate onVLChooseBelcantoView:self backBtnTapped:sender];
        return;
    }
    
    [[LSTPopView getSBGPopViewWithCustomView:self] dismiss];
}

#pragma mark - UITableViewDelegate,UITableViewDataSource
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.itemsModelArray.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    VLSBGAudioEffectCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:[VLSBGAudioEffectCell className] forIndexPath:indexPath];
    cell.selBelcantoModel = self.itemsModelArray[indexPath.item];
    return cell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    for (VLSBGBelcantoModel *model in self.itemsModelArray) {
        model.ifSelect = NO;
    }
    VLSBGBelcantoModel *selBgModel = self.itemsModelArray[indexPath.row];
    selBgModel.ifSelect = YES;
    self.indexValue = indexPath.item;
    [self.collectionView reloadData];
    if ([self.delegate respondsToSelector:@selector(onVLChooseBelcantoView:itemTapped:withIndex:)]) {
        [self.delegate onVLChooseBelcantoView:self itemTapped:selBgModel withIndex:indexPath.row];
    }
}

- (void)setSelBelcantoModel:(VLSBGBelcantoModel *)selBelcantoModel {
    _selBelcantoModel = selBelcantoModel;
    for (VLSBGBelcantoModel *model in self.itemsModelArray) {
        if ([_selBelcantoModel.imageName isEqualToString:model.imageName]) {
            model.ifSelect = YES;
        }
    }
    if (!selBelcantoModel) {
        VLSBGBelcantoModel *model = self.itemsModelArray.firstObject;
        model.ifSelect = YES;
    }
    [self.collectionView reloadData];
}

- (NSArray *)itemsArray {
    if (!_itemsArray) {
        _itemsArray = @[
        @{@"imageName":@"ktv_belcanto_defaultNo",@"titleStr":SBGLocalizedString(@"sbg_def_nothing"),@"ifSelect":@(false)},
        @{@"imageName":@"ktv_belcanto_bigRoomMale",@"titleStr":SBGLocalizedString(@"sbg_room_big_man"), @"ifSelect":@(false)},
        @{@"imageName":@"ktv_belcanto_smallRoomMale",@"titleStr":SBGLocalizedString(@"sbg_room_small_man"), @"ifSelect":@(false)},
        @{@"imageName":@"ktv_belcanto_bigRoomFemale",@"titleStr":SBGLocalizedString(@"sbg_room_big_woman"), @"ifSelect":@(false)},
        @{@"imageName":@"ktv_belcanto_smallRoomFemale",@"titleStr":SBGLocalizedString(@"sbg_room_small_woman"), @"ifSelect":@(false)}];
    }
    return _itemsArray;
}


@end

@implementation VLSBGAudioEffectCell

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self setupView];
    }
    return self;
}

- (void)setupView {
    
    UIView *bgView = [[UIView alloc]initWithFrame:CGRectMake(3, 3, 54, 54)];
    bgView.backgroundColor = UIColorMakeWithRGBA(4, 9, 37, 0.4);
    bgView.layer.cornerRadius = 12;
    bgView.layer.masksToBounds = YES;
    self.bgView = bgView;
    [self.contentView addSubview:bgView];
    
    self.iconImgView = [[UIImageView alloc]initWithFrame:CGRectMake(12, 12, 30, 30)];
    self.iconImgView.userInteractionEnabled = YES;
    [bgView addSubview:self.iconImgView];
    
    self.titleLabel = [[UILabel alloc]initWithFrame:CGRectMake(0, bgView.bottom+8, self.width, 17)];
    self.titleLabel.textAlignment = NSTextAlignmentCenter;
    self.titleLabel.font = UIFontMake(12);
    self.titleLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    [self.contentView addSubview:self.titleLabel];
}


- (void)setSelBelcantoModel:(VLSBGBelcantoModel *)selBelcantoModel {
    _selBelcantoModel = selBelcantoModel;
    self.iconImgView.image = [UIImage sbg_sceneImageWithName:selBelcantoModel.imageName];
    self.titleLabel.text = selBelcantoModel.titleStr;
    if (selBelcantoModel.ifSelect){
        self.bgView.layer.borderWidth = 1.5f;
        self.bgView.layer.borderColor = UIColorMakeWithHex(@"#009FFF").CGColor;
    }else{
        self.bgView.layer.borderWidth = 1.5f;
        self.bgView.layer.borderColor = UIColorClear.CGColor;
    }
}

@end



