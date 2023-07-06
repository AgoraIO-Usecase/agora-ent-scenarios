//
//  VLKTVSettingView.m
//  VoiceOnLine
//

#import "VLKTVSettingView.h"
#import "VLKTVSwitcherView.h"
#import "VLKTVTonesView.h"
#import "VLKTVSliderView.h"
#import "VLKTVKindsView.h"
#import "VLKTVRemoteVolumeView.h"
#import "VLFontUtils.h"
#import "KTVMacro.h"
#import "EffectCollectionViewCell.h"
@import Masonry;

@interface VLKTVSettingView() <
VLKTVSwitcherViewDelegate,
VLKTVSliderViewDelegate,
VLKTVKindsViewDelegate,
VLKTVTonesViewDelegate,
VLKTVRemoteVolumeViewDelegate,
UICollectionViewDelegate,
UICollectionViewDataSource
>

@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) VLKTVSwitcherView *soundSwitcher;
@property (nonatomic, strong) VLKTVSwitcherView *soundCardSwitcher;
@property (nonatomic, strong) VLKTVTonesView *tonesView;
@property (nonatomic, strong) VLKTVSliderView *soundSlider;
@property (nonatomic, strong) VLKTVSliderView *accSlider;
@property (nonatomic, strong) VLKTVSliderView *remoteSlider;
@property (nonatomic, strong) VLKTVKindsView *kindsView;
@property (nonatomic, strong) VLKTVRemoteVolumeView* remoteVolumeView;
@property (nonatomic, strong, readonly) VLKTVSettingModel *setting;
@property (nonatomic, strong) UICollectionView *collectionView;
@property (nonatomic, assign) CGFloat cellWidth;
@property (nonatomic, strong) NSArray *effectImgs;
@property (nonatomic, strong) NSArray *titles;
@end

@implementation VLKTVSettingView

- (instancetype)initWithSetting:(VLKTVSettingModel *)setting {
    if (self = [super init]) {
        [self configData:setting];
        [self initSubViews];
        [self addSubViewConstraints];
        self.soundSlider.value = 1.0;
        self.accSlider.value = 0.5;
        self.remoteSlider.value = 0.4;
        self.setting.remoteVolume = 40;
        self.cellWidth = (CGRectGetWidth([UIScreen mainScreen].bounds) - 48) / 4.0;
        self.titles = @[@"原声", @"KTV",@"演唱会", @"录音棚", @"留声机", @"空旷", @"空灵", @"流行",@"R&B"];
        self.effectImgs = @[@"ktv_console_setting1",@"ktv_console_setting2",@"ktv_console_setting3",@"ktv_console_setting4"];
    }
    return self;
}

- (void)configData:(VLKTVSettingModel *)setting {
    if (!setting) {
        _setting = [[VLKTVSettingModel alloc] init];
        [self.setting setDefaultProperties];
    } else {
        _setting = setting;
    }
    self.soundCardSwitcher.on = self.setting.soundCardOn;
    self.soundSwitcher.on = self.setting.soundOn;
    self.soundSlider.value = self.setting.soundValue;
    self.accSlider.value = self.setting.accValue;
    self.remoteSlider.value = self.setting.remoteValue;
}

- (void)initSubViews {
    [self addSubview:self.titleLabel];
    [self addSubview:self.soundSwitcher];
    [self addSubview:self.soundCardSwitcher];
//    [self addSubview:self.tonesView];
    [self addSubview:self.soundSlider];
    [self addSubview:self.accSlider];
    [self addSubview:self.remoteSlider];
    [self addSubview:self.collectionView];
   // [self addSubview:self.remoteVolumeView];
//    [self addSubview:self.kindsView];
}

- (void)addSubViewConstraints {
    [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(20);
        make.centerX.mas_equalTo(self);
    }];
    
    [self.soundSwitcher mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.titleLabel.mas_bottom).offset(16);
    }];
    
    [self.soundCardSwitcher mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.soundSwitcher.mas_bottom).offset(16);
    }];
    
//    [self.tonesView mas_makeConstraints:^(MASConstraintMaker *make) {
//        make.left.right.mas_equalTo(self);
//        make.top.mas_equalTo(self.soundSwitcher.mas_bottom).offset(25);
//        make.height.mas_equalTo(26);
//    }];
    
    [self.soundSlider mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.soundCardSwitcher.mas_bottom).offset(25);
        make.height.mas_equalTo(22);
    }];
    
    [self.accSlider mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.soundSlider.mas_bottom).offset(25);
        make.height.mas_equalTo(22);
    }];
    
    [self.remoteSlider mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.accSlider.mas_bottom).offset(25);
        make.height.mas_equalTo(22);
    }];
    
    [self.collectionView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.remoteSlider.mas_bottom).offset(10);
        make.height.mas_equalTo(78);
    }];
    
//    [self.remoteVolumeView mas_makeConstraints:^(MASConstraintMaker *make) {
//        make.left.right.mas_equalTo(self);
//        make.top.mas_equalTo(self.remoteSlider.mas_bottom).offset(25);
//        make.height.mas_equalTo(22);
//    }];
    
//    [self.kindsView mas_makeConstraints:^(MASConstraintMaker *make) {
//        make.top.mas_equalTo(self.remoteVolumeView.mas_bottom).offset(35);
//        make.left.right.mas_equalTo(self);
//        make.bottom.mas_equalTo(self).offset(-64);
//    }];
}

#pragma mark - VLKTVSwitcherViewDelegate

- (void)switcherView:(VLKTVSwitcherView *)switcherView on:(BOOL)on {
    VLKTVValueDidChangedType type;
    if (switcherView == self.soundSwitcher) {
        self.setting.soundOn = on;
        type = VLKTVValueDidChangedTypeEar;
    } else if (switcherView == self.soundCardSwitcher) {
        self.setting.soundCardOn = on;
        type = VLKTVValueDidChangedTypeSoundCard;
    } else {
        type = VLKTVValueDidChangedTypeMV;
        self.setting.mvOn = on;
    }
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:type];
    }
}

#pragma mark - VLKTVSliderViewDelegate

- (void)sliderView:(VLKTVSliderView *)sliderView valueChanged:(float)value {
    VLKTVValueDidChangedType type;
    if (sliderView == self.soundSlider) {
        NSLog(@"value:%f", value);
        self.setting.soundValue = value;
        type = VLKTVValueDidChangedTypeSound;
    } else if (sliderView == self.accSlider){
        self.setting.accValue = value;
        type = VLKTVValueDidChangedTypeAcc;
    } else {
        self.setting.remoteVolume = value ;
        type = VLKTVValueDidChangedTypeRemoteValue;
    }
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:type];
    }
}

#pragma mark - collectionViewDelegate

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.titles.count; //设置cell数量
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    EffectCollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"EffectCollectionViewCell" forIndexPath:indexPath];
    cell.bgImageView.image = [UIImage sceneImageWithName:self.effectImgs[indexPath.item % 4]];
    cell.titleLabel.text = self.titles[indexPath.item];
    cell.layer.cornerRadius = 5;
    cell.layer.masksToBounds = true;
    if(self.setting.selectEffect == indexPath.item){
        cell.layer.borderColor = [UIColor blueColor].CGColor;
        cell.layer.borderWidth = 2;
    } else {
        cell.layer.borderWidth = 0;
    }
    return cell;
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout*)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    return CGSizeMake(self.cellWidth, 54); //设置cell大小
}

- (CGFloat)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout*)collectionViewLayout minimumLineSpacingForSectionAtIndex:(NSInteger)section {
    return 12; //设置上下间距
}

- (CGFloat)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout*)collectionViewLayout minimumInteritemSpacingForSectionAtIndex:(NSInteger)section {
    return 12; //设置左右间距
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    self.setting.selectEffect = indexPath.item;
    [self.collectionView reloadData];
    if([self.delegate respondsToSelector:@selector(settingViewEffectChoosed:)]){
        [self.delegate settingViewEffectChoosed:indexPath.item];
    }
}

#pragma mark - VLKTVKindsViewDelegate

- (void)kindsViewDidClickIndex:(NSInteger)index {
    self.setting.kindIndex = index;
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLKTVValueDidChangedTypeListItem];
    }
}

#pragma mark - VLKTVTonesViewDelegate

- (void)tonesViewValueChanged:(NSInteger)value {
    self.setting.toneValue = value;
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLKTVValueDidChangedRiseFall];
    }
}

#pragma mark VLKTVRemoteVolumeViewDelegate
- (void)view:(VLKTVRemoteVolumeView *)view remoteVolumeValueChanged:(int)value {
    self.setting.remoteVolume = value;
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLKTVValueDidChangedTypeRemoteValue];
    }
}

#pragma mark - Lazy

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] init];
        _titleLabel.text = KTVLocalizedString(@"控制台");
        _titleLabel.font = VLUIFontMake(16);
        _titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    }
    return _titleLabel;
}

- (VLKTVSwitcherView *)soundSwitcher {
    if (!_soundSwitcher) {
        _soundSwitcher = [[VLKTVSwitcherView alloc] init];
        _soundSwitcher.titleLabel.text = KTVLocalizedString(@"耳返设置");
        _soundSwitcher.subText = KTVLocalizedString(@"请插入耳机使用耳返功能");
        _soundSwitcher.delegate = self;
    }
    return _soundSwitcher;
}

- (VLKTVSwitcherView *)soundCardSwitcher {
    if (!_soundCardSwitcher) {
        _soundCardSwitcher = [[VLKTVSwitcherView alloc] init];
        _soundCardSwitcher.titleLabel.text = KTVLocalizedString(@"虚拟声卡");
        _soundCardSwitcher.subText = KTVLocalizedString(@"请插入耳机使用耳返功能");
        _soundCardSwitcher.delegate = self;
    }
    return _soundCardSwitcher;
}

- (VLKTVTonesView *)tonesView {
    if (!_tonesView) {
        _tonesView = [[VLKTVTonesView alloc] initWithMaxLevel:12 currentLevel:6];
        _tonesView.titleLabel.text = KTVLocalizedString(@"升降调");
        _tonesView.delegate = self;
    }
    return _tonesView;
}

- (VLKTVSliderView *)soundSlider {
    if (!_soundSlider) {
        _soundSlider = [[VLKTVSliderView alloc] initWithMax:1 min:0];
        _soundSlider.titleLabel.text = KTVLocalizedString(@"人声音量");
        _soundSlider.delegate = self;
    }
    return _soundSlider;
}

- (VLKTVSliderView *)accSlider {
    if (!_accSlider) {
        _accSlider = [[VLKTVSliderView alloc] initWithMax:1 min:0];
        _accSlider.titleLabel.text = KTVLocalizedString(@"伴奏音量");
        _accSlider.delegate = self;
    }
    return _accSlider;
}

- (VLKTVSliderView *)remoteSlider {
    if (!_remoteSlider) {
        _remoteSlider = [[VLKTVSliderView alloc] initWithMax:1 min:0];
        _remoteSlider.titleLabel.text = KTVLocalizedString(@"远端音量");
        _remoteSlider.delegate = self;
    }
    return _remoteSlider;
}

-(UICollectionView *)collectionView {
    if(!_collectionView){
            UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc]init];
            layout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
            self.collectionView = [[UICollectionView alloc] initWithFrame:CGRectZero collectionViewLayout:layout];
            self.collectionView.dataSource = self;
            self.collectionView.delegate = self;
            self.collectionView.backgroundColor = [UIColor clearColor];
            self.collectionView.showsHorizontalScrollIndicator = NO;
            self.collectionView.contentInset = UIEdgeInsetsMake(12, 12, 12, 12);
            [self.collectionView registerClass:[EffectCollectionViewCell class] forCellWithReuseIdentifier:@"EffectCollectionViewCell"];
    }
    return _collectionView;
}

//- (VLKTVRemoteVolumeView*)remoteVolumeView {
//    if (!_remoteVolumeView) {
//        _remoteVolumeView = [[VLKTVRemoteVolumeView alloc] initWithMin:0 withMax:100 withCurrent:40];
//        _remoteVolumeView.titleLabel.text = KTVLocalizedString(@"RemoteVolume");
//        _remoteVolumeView.delegate = self;
//        _setting.remoteVolume = 40;
//    }
//    return _remoteVolumeView;
//}

- (VLKTVKindsView *)kindsView {
    if (!_kindsView) {
        _kindsView = [[VLKTVKindsView alloc] init];
        _kindsView.delegate = self;
        _kindsView.list = [VLKTVKindsModel kinds];
    }
    return _kindsView;
}

- (void)setIsEarOn:(BOOL)isEarOn
{
    self.setting.soundOn = isEarOn;
    self.soundSwitcher.on = isEarOn;
}

-(void)setUseSoundCard:(BOOL)useSoundCard
{
    self.setting.soundCardOn = useSoundCard;
    self.soundCardSwitcher.on = useSoundCard;
}

- (void)setAccValue:(float)accValue {
    self.setting.accValue = accValue;
    self.accSlider.value = accValue;
}

-(void)setIspause:(BOOL)isPause{
    _remoteSlider.userInteractionEnabled = !isPause;
    _remoteSlider.value = isPause ? 1 : self.setting.remoteVolume / 100.0;
}

- (void)setSelectEffect:(NSInteger)index{
    self.setting.selectEffect = index;
    
}

@end

@implementation VLKTVSettingModel

- (void)setDefaultProperties {
    self.soundOn = NO;
    self.mvOn = NO;
    self.toneValue = 0;
    self.soundValue = 0.0;
    self.accValue = 0.0;
    self.remoteValue = 0.0;
    self.kindIndex = kKindUnSelectedIdentifier;
}

@end
