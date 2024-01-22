//
//  VLKTVTonesView.m
//  VoiceOnLine
//

#import "VLKTVTonesView.h"
#import "AESMacro.h"
#import "AgoraEntScenarios-Swift.h"
@import Masonry;

@interface VLKTVTonesView()

@property (nonatomic, strong) UIButton *addButton;
@property (nonatomic, strong) UIButton *reduceButton;
@property (nonatomic, assign) NSInteger maxLevel;
@property (nonatomic, assign) NSInteger currentLevel;
@property (nonatomic, strong) NSMutableArray *levels;

@end

@implementation VLKTVTonesView

- (instancetype)initWithMaxLevel:(NSInteger)maxLevel currentLevel:(NSInteger)currentLevel {
    if (self == [super init]) {
        self.maxLevel = maxLevel;
        self.currentLevel = currentLevel;
        [self initSubViews];
        [self addSubViewConstraints];
        [self configViewsForLevel];
    }
    return self;
}

- (void)initSubViews {
    [self addSubview:self.reduceButton];
    [self addSubview:self.addButton];
}

- (void)addSubViewConstraints {
    [self.reduceButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(79);
        make.centerY.mas_equalTo(self);
    }];
    
    [self.addButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(-20);
        make.centerY.mas_equalTo(self);
    }];
}

- (void)configViewsForLevel {
    if (self.maxLevel <= 0) return;
    
    NSMutableArray *array = [NSMutableArray array];
    for (int i = 0; i < self.maxLevel; i++) {
        UIView *line = [[UIView alloc] init];
        line.tag = 500 + i;
        if (i < self.currentLevel) {
            line.backgroundColor = UIColorMakeWithHex(@"#009FFF");
        } else {
            line.backgroundColor = [UIColor whiteColor];
        }
        [line mas_makeConstraints:^(MASConstraintMaker *make) {
            make.height.mas_equalTo(2 + i * 2);
        }];
        [self addSubview:line];
        [array addObject:line];
    }
    self.levels = array;
    
    // withFixedItemLength 控件宽度或者高度固定，间距不固定
    [array mas_distributeViewsAlongAxis:MASAxisTypeHorizontal withFixedItemLength:2 leadSpacing:117 tailSpacing:57];
    [array mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.mas_equalTo(self.addButton.mas_bottom);
    }];
}

- (void)buttonClcik:(UIButton *)sender {
    if (self.maxLevel <= 0) return;
    UIColor *color;
    if (sender == self.addButton) {
        if (self.currentLevel == self.maxLevel) return;
        color = UIColorMakeWithHex(@"#009FFF");
        self.currentLevel++;
        UIView *indexView = [self indexView];
        indexView.backgroundColor = color;
    } else {
        if (self.currentLevel == 0) return;
        color = [UIColor whiteColor];
        UIView *indexView = [self indexView];
        indexView.backgroundColor = color;
        self.currentLevel--;
    }
    
    if ([self.delegate respondsToSelector:@selector(tonesViewValueChanged:)]) {
        [self.delegate tonesViewValueChanged:self.currentLevel];
    }
}

- (NSInteger)value {
    return self.currentLevel;
}

- (UIView *)indexView {
    return [self viewWithTag:500 + self.currentLevel - 1];
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
