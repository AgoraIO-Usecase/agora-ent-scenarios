//
//  VLEarSettingView.m
//  AgoraEntScenarios
//
//  Created by CP on 2023/6/5.
//

#import "VLEarSettingView.h"
#import "VLKTVSliderView.h"
#import <Masonry/Masonry.h>
#import "AESMacro.h"
#import "VLMacroDefine.h"
#import "HeadSetManager.h"
#define withEarWarning @"开启耳返可实时听到自己的声音，唱歌的时候及时调整"
#define withoutEarWarning @"使用耳返必须插入耳机，当前未检测到耳机"
@interface VLEarSettingView()<VLKTVSliderViewDelegate>
@property (nonatomic,strong) UILabel *titleLabel;
@property (nonatomic,strong) UILabel *earLabel;
@property (nonatomic,strong) UISwitch *earSwitch;
@property (nonatomic,strong) UIButton *earWarningBtn;
@property (nonatomic, strong) VLKTVSliderView *earSlider;
@property (nonatomic,strong) UILabel *earSetLabel;
@property (nonatomic,strong) UIView *earSetView;
@property (nonatomic, assign) BOOL isEarOn;
@property (nonatomic, assign) float sliderValue;
@end
@implementation VLEarSettingView

- (instancetype)initWithFrame:(CGRect)frame isEarOn:(BOOL)isEarOn vol:(CGFloat)vol withDelegate:(id<VLEarSettingViewViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        [self initSubViews];
        [self addSubViewConstraints];
        self.isEarOn = isEarOn;
        self.sliderValue = vol;
        self.earSlider.value = vol / 100.0;
        self.delegate = delegate;
    }
    return self;
}

- (void)initSubViews {
    
    self.backgroundColor = UIColorMakeWithHex(@"#152164");
    
    _titleLabel = [[UILabel alloc]init];
    _titleLabel.text = @"耳返";
    _titleLabel.textAlignment = NSTextAlignmentCenter;
    _titleLabel.textColor = [UIColor whiteColor];
    [self addSubview:_titleLabel];
    
    _earLabel = [[UILabel alloc]init];
    _earLabel.text = @"耳返开关";
    _earLabel.textColor = [UIColor whiteColor];
    [self addSubview:_earLabel];
    
    _earSwitch = [[UISwitch alloc]init];
    _earSwitch.onTintColor = UIColorMakeWithHex(@"#099DFD");
    [_earSwitch addTarget:self action:@selector(earChange:) forControlEvents:UIControlEventTouchUpInside];
    _earSwitch.userInteractionEnabled = [HeadSetManager hasHeadset];
    _earSwitch.alpha = [HeadSetManager hasHeadset] ? 1: 0.6;
    [self addSubview:_earSwitch];
    
    _earWarningBtn = [[UIButton alloc]init];
    [_earWarningBtn setTitle:withoutEarWarning forState:UIControlStateNormal] ;
    [_earWarningBtn setTitle:withEarWarning forState:UIControlStateSelected] ;
    _earWarningBtn.font = [UIFont systemFontOfSize:12];
    _earWarningBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
    _earWarningBtn.imageView.contentMode = UIViewContentModeScaleAspectFit;
    [_earWarningBtn setTitleColor:[UIColor redColor] forState:UIControlStateNormal];
    [_earWarningBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateSelected];
    [_earWarningBtn setImage:[UIImage sceneImageWithName:@"ktv_add_circle"] forState:UIControlStateNormal];
    [_earWarningBtn setImage:[UIImage sceneImageWithName:@"ktv_add_circle_in"] forState:UIControlStateSelected];
    _earWarningBtn.selected = [HeadSetManager hasHeadset];
    kWeakSelf(self);
    [HeadSetManager addHeadsetObserverWithCallback:^(BOOL inserted) {
        weakself.earWarningBtn.selected = inserted;
        if(!inserted){
            weakself.earSwitch.on = false;
            weakself.earSwitch.userInteractionEnabled = false;
            weakself.earSlider.alpha =  0.6;
            //如果已经开启了 但是拔下耳机了 强制关闭
            if(weakself.isEarOn){
                if([weakself.delegate respondsToSelector:@selector(onVLKTVEarSettingViewSwitchChanged:)]){
                    [weakself.delegate onVLKTVEarSettingViewSwitchChanged:false];
                }
            }
        } else {
            weakself.earSwitch.userInteractionEnabled = true;
            weakself.earSlider.alpha = weakself.isEarOn ? 1 : 0.6;
        }
        weakself.earSwitch.alpha = inserted ? 1 : 0.6;
        weakself.earWarningBtn.titleLabel.textColor = inserted ? [UIColor whiteColor] : [UIColor redColor];
    }];
    [self addSubview:_earWarningBtn];
    
    _earSetView = [[UIView alloc]init];
    _earSetView.backgroundColor = [UIColor colorWithRed:0.031 green:0.028 blue:0.185 alpha:0.2];
    [self addSubview:_earSetView];
    
    _earSetLabel = [[UILabel alloc]init];
    _earSetLabel.text = @"耳返设置";
    _earSetLabel.textColor = [UIColor grayColor];
    _earSetLabel.font = [UIFont systemFontOfSize:12];
    [_earSetView addSubview:_earSetLabel];
    
    [self addSubview:self.earSlider];
}

-(void)addSubViewConstraints {
    [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.mas_equalTo(self);
        make.width.mas_equalTo(50);
        make.height.mas_equalTo(20);
        make.top.mas_equalTo(30);
    }];
    
    [self.earLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(20);
        make.top.mas_equalTo(65);
        make.width.mas_equalTo(100);
        make.height.mas_equalTo(21);
    }];
    
    [self.earSwitch mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(_earLabel);
        make.right.mas_equalTo(self).offset(-20);
        make.width.mas_equalTo(46);
        make.height.mas_equalTo(28);
    }];
    
    [self.earWarningBtn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(20);
        make.top.mas_equalTo(_earLabel.mas_bottom).offset(17);
        make.width.mas_equalTo(self.bounds.size.width - 40);
        make.height.mas_equalTo(18);
    }];
    
    [self.earSetView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(_earWarningBtn.mas_bottom).offset(17);
        make.height.mas_equalTo(32);
    }];
    
    [self.earSetLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self).offset(20);
        make.centerY.mas_equalTo(_earSetView);
        make.width.mas_equalTo(100);
    }];
    
    [self.earSlider mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.mas_equalTo(self);
        make.top.mas_equalTo(self.earSetView.mas_bottom).offset(25);
        make.height.mas_equalTo(22);
    }];
}

- (VLKTVSliderView *)earSlider {
    if (!_earSlider) {
        _earSlider = [[VLKTVSliderView alloc] initWithMax:1 min:0];
        _earSlider.titleLabel.text = @"耳返音量";
        _earSlider.titleLabel.textColor = [UIColor whiteColor];
        _earSlider.delegate = self;
    }
    return _earSlider;
}

-(void)earChange:(UISwitch *)swich {
    self.isEarOn = swich.on;
    if([self.delegate respondsToSelector:@selector(onVLKTVEarSettingViewSwitchChanged:)]){
        [self.delegate onVLKTVEarSettingViewSwitchChanged:swich.on];
    }
}

-(void)setIsEarOn:(BOOL)isEarOn {
    _isEarOn = isEarOn;
    if([HeadSetManager hasHeadset] == NO){
        self.earSwitch.on = false;
        _earSlider.alpha =  0.6;
        _earSlider.userInteractionEnabled = false;
    } else {
        _earSwitch.on = isEarOn;
        _earSlider.alpha = isEarOn ? 1 : 0.6;
        _earSlider.userInteractionEnabled = isEarOn;
    }
}

//-(void)setSliderValue:(float)sliderValue {
//    _sliderValue = sliderValue;
//}

- (void)sliderView:(VLKTVSliderView *)sliderView valueChanged:(float)value{
    self.sliderValue = value ;
    if([self.delegate respondsToSelector:@selector(onVLKTVEarSettingViewValueChanged:)]){
        [self.delegate onVLKTVEarSettingViewValueChanged:value];
    }
}
@end
