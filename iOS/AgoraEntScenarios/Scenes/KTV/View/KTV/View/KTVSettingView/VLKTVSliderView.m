//
//  VLKTVSliderView.m
//  VoiceOnLine
//

#import "VLKTVSliderView.h"
#import "AgoraEntScenarios-Swift.h"
@import Masonry;

@interface VLKTVSliderView()

@property (nonatomic, strong) UISlider *sliderView;
@property (nonatomic, assign) float max;
@property (nonatomic, assign) float min;
@property (nonatomic, assign) int minValue;
@property (nonatomic, assign) int maxValue;
@property (nonatomic, assign) int currentValue;
@property (nonatomic, strong) UIButton *addButton;
@property (nonatomic, strong) UIButton *reduceButton;
@end

@implementation VLKTVSliderView

- (void)setAccessibilityIdentifier:(NSString *)accessibilityIdentifier {
    [super setAccessibilityIdentifier:accessibilityIdentifier];
    self.addButton.accessibilityIdentifier = [NSString stringWithFormat:@"%@_add_button_id", accessibilityIdentifier];
    self.reduceButton.accessibilityIdentifier = [NSString stringWithFormat:@"%@_reduce_button_id", accessibilityIdentifier];
}

- (instancetype)initWithMax:(float)max min:(float)min {
    if (self = [super init]) {
        self.max = max;
        self.min = min;
        [self initSubViews];
        [self addSubViewConstraints];
    }
    return self;
}

- (void)initSubViews {
    [self addSubview:self.sliderView];
    [self addSubview:self.reduceButton];
    [self addSubview:self.addButton];
}

- (void)addSubViewConstraints {
    CGFloat padding = 8;
    [self.reduceButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(90);
        make.width.mas_equalTo(@(26));
        make.centerY.mas_equalTo(self);
    }];
    
    [self.addButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(self).offset(-padding);
        make.width.mas_equalTo(@(26));
        make.centerY.mas_equalTo(self);
    }];
    
    [self.sliderView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.equalTo(self.addButton.mas_left).offset(-padding);
        make.centerY.mas_equalTo(self);
        make.left.mas_equalTo(self.reduceButton.mas_right).offset(padding);
        make.height.mas_equalTo(15);
    }];
    
}

- (void)setValue:(float)value {
    _value = value;
    _currentValue = (int)(_value * 100);
    self.sliderView.value = value;
}

//处理
- (void)sliderValurChanged:(UISlider*)slider {
    [self sliderClick:slider];
}

- (void)sliderClick:(UISlider *)slider {
    if ([self.delegate respondsToSelector:@selector(sliderView:valueChanged:)]) {
        self.currentValue = (int)(slider.value * 100);
        [self.delegate sliderView:self valueChanged:(int)(slider.value * 100)];
    }
}

- (void)buttonClcik:(UIButton *)sender {
    if (sender == self.addButton) {
        if (self.currentValue == 100) return;
        self.currentValue++;
    } else {
        if (self.currentValue == 0) return;
        self.currentValue--;
    }
    self.sliderView.value = (CGFloat)self.currentValue / 100.0;
    if ([self.delegate respondsToSelector:@selector(sliderView:valueChanged:)]) {
        [self.delegate sliderView:self valueChanged:self.currentValue];
    }
}

#pragma mark - Lazy

- (UISlider *)sliderView {
    if (!_sliderView) {
        _sliderView = [[UISlider alloc]init];
        [_sliderView setThumbImage:[UIImage ktv_sceneImageWithName:@"icon_ktv_slider" ] forState:UIControlStateNormal];
        [_sliderView setThumbImage:[UIImage ktv_sceneImageWithName:@"icon_ktv_slider" ] forState:UIControlStateHighlighted];
        _sliderView.maximumValue = self.max;
        _sliderView.minimumValue = self.min;
        [_sliderView addTarget:self action:@selector(sliderValurChanged:) forControlEvents:UIControlEventValueChanged];
    }
    return _sliderView;
}

- (UIButton *)addButton {
    if (!_addButton) {
        _addButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_addButton setImage:[UIImage ktv_sceneImageWithName:@"icon_ktv_add" ] forState:UIControlStateNormal];
        [_addButton addTarget:self action:@selector(buttonClcik:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _addButton;
}

- (UIButton *)reduceButton {
    if (!_reduceButton) {
        _reduceButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_reduceButton setImage:[UIImage ktv_sceneImageWithName:@"icon_ktv_reduce" ] forState:UIControlStateNormal];
        [_reduceButton addTarget:self action:@selector(buttonClcik:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _reduceButton;
}

@end
