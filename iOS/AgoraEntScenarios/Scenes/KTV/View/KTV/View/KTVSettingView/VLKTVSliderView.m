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

@end

@implementation VLKTVSliderView

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
}

- (void)addSubViewConstraints {
    [self.sliderView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(79);
        make.centerY.mas_equalTo(self);
        make.right.mas_equalTo(-20);
        make.height.mas_equalTo(15);
    }];
}

- (void)setValue:(float)value {
    _value = value;
    self.sliderView.value = value;
}

//处理
- (void)sliderValurChanged:(UISlider*)slider {
    [self sliderClick:slider];
}

- (void)sliderClick:(UISlider *)slider {
    NSLog(@"===== %f", slider.value);
    if ([self.delegate respondsToSelector:@selector(sliderView:valueChanged:)]) {
        [self.delegate sliderView:self valueChanged:slider.value];
    }
}

#pragma mark - Lazy

- (UISlider *)sliderView {
    if (!_sliderView) {
        _sliderView = [[UISlider alloc]init];
        [_sliderView setThumbImage:[UIImage sceneImageWithName:@"icon_ktv_slider"] forState:UIControlStateNormal];
        [_sliderView setThumbImage:[UIImage sceneImageWithName:@"icon_ktv_slider"] forState:UIControlStateHighlighted];
        _sliderView.maximumValue = self.max;
        _sliderView.minimumValue = self.min;
        [_sliderView addTarget:self action:@selector(sliderValurChanged:) forControlEvents:UIControlEventValueChanged];
    }
    return _sliderView;
}

@end
