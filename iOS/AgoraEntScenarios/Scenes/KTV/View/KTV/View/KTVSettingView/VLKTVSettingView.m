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
#import "EffectCollectionViewCell.h"
#import "AgoraEntScenarios-Swift.h"
#import "VLKTVSegmentView.h"
#import "VLSettingNetSwitcherView.h"
#import "VLSettingAIAECSwitcherView.h"
#import "VLSettingSwitcherView.h"
@import Masonry;
@import AgoraCommon;
@interface VLKTVSettingView() <
VLKTVSwitcherViewDelegate,
VLKTVSliderViewDelegate,
VLKTVKindsViewDelegate,
VLKTVTonesViewDelegate,
VLKTVRemoteVolumeViewDelegate,
VLKTVSegmentViewDelegate,
VLSettingSwitcherViewDelegate,
VLSettingNetSwitcherViewDelegate,
VLSettingAIAECSwitcherViewDelegate,
UICollectionViewDelegate,
UICollectionViewDataSource
>

@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UIScrollView *scrollView;
@property (nonatomic, strong) VLKTVSwitcherView *soundSwitcher;
@property (nonatomic, strong) VLKTVTonesView *tonesView;
@property (nonatomic, strong) VLKTVSliderView *soundSlider;
@property (nonatomic, strong) VLKTVSwitcherView *soundCardSwitcher;
@property (nonatomic, strong) VLKTVSliderView *accSlider;
@property (nonatomic, strong) VLKTVSliderView *remoteSlider;
@property (nonatomic, strong) VLKTVKindsView *kindsView;
@property (nonatomic, strong) VLKTVRemoteVolumeView* remoteVolumeView;
@property (nonatomic, strong) VLKTVSegmentView *lrcSegmentView;
@property (nonatomic, strong) VLKTVSegmentView *vqsSegmentView;
@property (nonatomic, strong) VLKTVSegmentView *ansSegmentView;
@property (nonatomic, strong) VLSettingNetSwitcherView *netSwitcherView;
@property (nonatomic, strong) VLSettingAIAECSwitcherView *aiAecSwitcherView;
@property (nonatomic, strong) VLSettingSwitcherView *perBroSwitcherView;
@property (nonatomic, strong) VLSettingSwitcherView *delaySwitcherView;
@property (nonatomic, strong, readonly) VLKTVSettingModel *setting;
@property (nonatomic, strong) UICollectionView *collectionView;
@property (nonatomic, assign) CGFloat cellWidth;
@property (nonatomic, strong) NSArray *effectImgs;
@property (nonatomic, strong) NSArray *titles;
@property (nonatomic, assign) NSInteger lrcLevel;
@property (nonatomic, assign) NSInteger vqs;
@property (nonatomic, assign) NSInteger ans;
@end

@implementation VLKTVSettingView

- (instancetype)initWithSetting:(VLKTVSettingModel *)setting {
    if (self = [super init]) {
        [self configData:setting];
        [self initSubViews];
        [self addSubViewConstraints];
        self.soundSlider.value = 1.0;
        self.accSlider.value = 0.5;
        self.remoteSlider.value = 0.3;
        self.setting.remoteVolume = 30;
        self.lrcLevel = 1;
        self.vqs = 0;
        self.ans = 0;
        self.cellWidth = (CGRectGetWidth([UIScreen mainScreen].bounds) - 48) / 4.0;
        self.titles = @[ @"KTV", KTVLocalizedString(@"ktv_effect_off"),KTVLocalizedString(@"ktv_effect_concert"), KTVLocalizedString(@"ktv_effect_studio"), KTVLocalizedString(@"ktv_effect_phonograph"), KTVLocalizedString(@"ktv_effect_spatial"), KTVLocalizedString(@"ktv_effect_ethereal"), KTVLocalizedString(@"ktv_effect_pop"),@"R&B"];
        self.effectImgs = @[@"ktv_console_setting3",@"ktv_console_setting4",@"ktv_console_setting2",@"ktv_console_setting1"];
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
    self.lrcSegmentView.selectIndex = self.setting.lrcLevel;
    self.vqsSegmentView.selectIndex = self.setting.vqs;
    self.ansSegmentView.selectIndex = self.setting.ans;
    self.delaySwitcherView.on = self.setting.isDelay;
    self.perBroSwitcherView.on = self.setting.isPerBro;
    self.aiAecSwitcherView.on = self.setting.enableAec;
    self.aiAecSwitcherView.aecValue = self.setting.aecLevel;
    self.netSwitcherView.on = self.setting.enableMultipath;
}

-(void)setChorusStatus:(BOOL)status{
    self.accSlider.userInteractionEnabled = status;
    self.accSlider.alpha = status ? 1 : 0.6;
    self.remoteSlider.userInteractionEnabled = status;
    self.remoteSlider.alpha = status ? 1 : 0.6;
    self.lrcSegmentView.userInteractionEnabled = !status;
    self.lrcSegmentView.alpha = status ? 0.6 : 1;
}

- (void)initSubViews {
    self.scrollView = [[UIScrollView alloc]init];
    self.scrollView.scrollEnabled = true;
    self.scrollView.contentSize = CGSizeMake(0, 720);
    [self addSubview:self.scrollView];
    [self.scrollView addSubview:self.titleLabel];
    [self.scrollView addSubview:self.soundSwitcher];
    [self.scrollView addSubview:self.soundCardSwitcher];
    [self.scrollView addSubview:self.soundSlider];
    [self.scrollView addSubview:self.accSlider];
    [self.scrollView addSubview:self.remoteSlider];
    [self.scrollView addSubview:self.collectionView];
    [self.scrollView addSubview:self.lrcSegmentView];
    [self.scrollView addSubview:self.perBroSwitcherView];
    [self.scrollView addSubview:self.vqsSegmentView];
    [self.scrollView addSubview:self.ansSegmentView];
    [self.scrollView addSubview:self.netSwitcherView];
    [self.scrollView addSubview:self.aiAecSwitcherView];
}

- (void)addSubViewConstraints {
    [self.scrollView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.bottom.left.right.mas_equalTo(self);
    }];
    
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
    
    [self.lrcSegmentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.collectionView.mas_bottom).offset(0);
        make.height.mas_equalTo(50);
    }];
    
    [self.perBroSwitcherView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.lrcSegmentView.mas_bottom).offset(0);
        make.height.mas_equalTo(50);
    }];
    
    [self.netSwitcherView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.perBroSwitcherView.mas_bottom).offset(0);
        make.height.mas_equalTo(60);
    }];
    
    [self.vqsSegmentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.netSwitcherView.mas_bottom).offset(0);
        make.height.mas_equalTo(50);
    }];
    
    [self.ansSegmentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.vqsSegmentView.mas_bottom).offset(0);
        make.height.mas_equalTo(50);
    }];

    [self.aiAecSwitcherView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.ansSegmentView.mas_bottom).offset(0);
        make.height.mas_equalTo(90);
    }];
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
    cell.bgImageView.image = [UIImage ktv_sceneImageWithName:self.effectImgs[indexPath.item % 4] ];
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

#pragma mark
- (void)switcherNetView:(VLSettingNetSwitcherView *)switcherView on:(BOOL)on{
    self.setting.enableMultipath = on;
    if ([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]) {
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLKTVValueDidChangedTypeenableMultipath];
    }
}

#pragma mark - Lazy

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] init];
        _titleLabel.text = KTVLocalizedString(@"ktv_music_menu_dialog_title");
        _titleLabel.font = [UIFont systemFontOfSize:16];
        _titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    }
    return _titleLabel;
}

- (VLKTVSwitcherView *)soundSwitcher {
    if (!_soundSwitcher) {
        _soundSwitcher = [[VLKTVSwitcherView alloc] init];
        _soundSwitcher.titleLabel.text = KTVLocalizedString(@"ktv_music_menu_dialog_ear");
        _soundSwitcher.subText = KTVLocalizedString(@"ktv_please_use_headset");
        _soundSwitcher.delegate = self;
    }
    return _soundSwitcher;
}

- (VLKTVSwitcherView *)soundCardSwitcher {
    if (!_soundCardSwitcher) {
        _soundCardSwitcher = [[VLKTVSwitcherView alloc] init];
        _soundCardSwitcher.titleLabel.text = KTVLocalizedString(@"ktv_soundcard");
        _soundCardSwitcher.subText = KTVLocalizedString(@"ktv_please_use_headset");
        _soundCardSwitcher.delegate = self;
    }
    return _soundCardSwitcher;
}

- (VLSettingSwitcherView *)perBroSwitcherView {
    if (!_perBroSwitcherView) {
        _perBroSwitcherView = [[VLSettingSwitcherView alloc] init];
        _perBroSwitcherView.subText = KTVLocalizedString(@"ktv_per_bro");
        _perBroSwitcherView.delegate = self;
    }
    return _perBroSwitcherView;
}

- (VLSettingSwitcherView *)delaySwitcherView {
    if (!_delaySwitcherView) {
        _delaySwitcherView = [[VLSettingSwitcherView alloc] init];
        _delaySwitcherView.delegate = self;
    }
    return _delaySwitcherView;
}

- (VLSettingNetSwitcherView *)netSwitcherView {
    if (!_netSwitcherView) {
        _netSwitcherView = [[VLSettingNetSwitcherView alloc] init];
        _netSwitcherView.delegate = self;
    }
    return _netSwitcherView;
}

- (VLKTVTonesView *)tonesView {
    if (!_tonesView) {
        _tonesView = [[VLKTVTonesView alloc] initWithMaxLevel:12 currentLevel:6];
        _tonesView.titleLabel.text = KTVLocalizedString(@"ktv_music_menu_dialog_tone");
        _tonesView.delegate = self;
    }
    return _tonesView;
}

- (VLKTVSliderView *)soundSlider {
    if (!_soundSlider) {
        _soundSlider = [[VLKTVSliderView alloc] initWithMax:1 min:0];
        _soundSlider.titleLabel.text = KTVLocalizedString(@"ktv_music_menu_dialog_vol1");
        _soundSlider.delegate = self;
    }
    return _soundSlider;
}

- (VLKTVSliderView *)accSlider {
    if (!_accSlider) {
        _accSlider = [[VLKTVSliderView alloc] initWithMax:1 min:0];
        _accSlider.accessibilityIdentifier = @"ktv_room_setting_acc_slider_id";
        _accSlider.titleLabel.text = KTVLocalizedString(@"ktv_music_menu_dialog_vol2");
        _accSlider.delegate = self;
    }
    return _accSlider;
}


- (VLKTVSliderView *)remoteSlider {
    if (!_remoteSlider) {
        _remoteSlider = [[VLKTVSliderView alloc] initWithMax:1 min:0];
        _remoteSlider.titleLabel.text = KTVLocalizedString(@"ktv_music_menu_dialog_remote_volume");
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

- (VLKTVSegmentView *)lrcSegmentView {
    if (!_lrcSegmentView) {
        _lrcSegmentView = [[VLKTVSegmentView alloc] init];
        _lrcSegmentView.tag = 800;
        _lrcSegmentView.delegate = self;
        _lrcSegmentView.type = SegmentViewTypeScore;
        _lrcSegmentView.subText = KTVLocalizedString(@"ktv_lrc_level");
    }
    return _lrcSegmentView;
}

- (VLKTVSegmentView *)vqsSegmentView {
    if (!_vqsSegmentView) {
        _vqsSegmentView = [[VLKTVSegmentView alloc] init];
        _vqsSegmentView.tag = 801;
        _vqsSegmentView.delegate = self;
        _vqsSegmentView.type = SegmentViewTypeVQS;
        _vqsSegmentView.subText = KTVLocalizedString(@"ktv_per_vol_quality");
    }
    return _vqsSegmentView;
}

- (VLKTVSegmentView *)ansSegmentView {
    if (!_ansSegmentView) {
        _ansSegmentView = [[VLKTVSegmentView alloc] init];
        _ansSegmentView.tag = 802;
        _ansSegmentView.delegate = self;
        _ansSegmentView.type = SegmentViewTypeAns;
        _ansSegmentView.subText = KTVLocalizedString(@"ktv_per_ans");
    }
    return _ansSegmentView;
}

- (VLKTVKindsView *)kindsView {
    if (!_kindsView) {
        _kindsView = [[VLKTVKindsView alloc] init];
        _kindsView.delegate = self;
        _kindsView.list = [VLKTVKindsModel kinds];
    }
    return _kindsView;
}

-(VLSettingAIAECSwitcherView *)aiAecSwitcherView {
    if(!_aiAecSwitcherView){
        _aiAecSwitcherView = [VLSettingAIAECSwitcherView new];
        _aiAecSwitcherView.delegate = self;
    }
    return _aiAecSwitcherView;
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

-(void)aecSwitcherView:(VLSettingAIAECSwitcherView *)switcherView on:(BOOL)on{
    self.setting.enableAec = on;
    if([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]){
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLKTVValueDidChangedTypeaiaec];
    }
}

-(void)aecSwitcherView:(VLSettingAIAECSwitcherView *)switcherView level:(NSInteger)level{
    self.setting.aecLevel = level;
    if([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]){
        [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLKTVValueDidChangedTypeAecLevel];
    }
}

- (void)switcherSetView:(VLSettingSwitcherView *)switcherView on:(BOOL)on{
    if(switcherView == self.delaySwitcherView){
        self.setting.isDelay = on;
        if([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]){
            [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLKTVValueDidChangedTypeDelay];
        }
    } else if (switcherView == self.perBroSwitcherView){
        self.setting.isPerBro = on;
        if([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]){
            [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLKTVValueDidChangedTypebro];
        }
    }
}

-(void)segmentView:(VLKTVSegmentView *)view DidSelectIndex:(NSInteger)index{
    if(view.tag == 800){
        self.lrcLevel = index;
        self.setting.lrcLevel = index;
        if([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]){
            [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLKTVValueDidChangedTypeLrc];
        }
    } else if(view.tag == 801){
        self.vqs = index;
        self.setting.vqs = index;
        if([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]){
            [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLKTVValueDidChangedTypeVqs];
        }
    } else {
        self.ans = index;
        self.setting.ans = index;
        if([self.delegate respondsToSelector:@selector(settingViewSettingChanged:valueDidChangedType:)]){
            [self.delegate settingViewSettingChanged:self.setting valueDidChangedType:VLKTVValueDidChangedTypeAns];
        }
    }
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [super touchesBegan:touches withEvent:event];
    [self endEditing:true];
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
    self.lrcLevel = 1;
    self.vqs = 0;
    self.ans = 0;
    self.isDelay = false;
    self.isPerBro = false;
    self.enableAec = false;
    self.enableMultipath = true;
    self.aecLevel = 0;
    self.kindIndex = kKindUnSelectedIdentifier;
}

@end
