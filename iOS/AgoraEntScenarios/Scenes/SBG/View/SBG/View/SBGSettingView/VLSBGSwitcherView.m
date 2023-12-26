//
//  VLSBGSwitcherView.m
//  VoiceOnLine
//

#import "VLSBGSwitcherView.h"
#import "VLFontUtils.h"
@import Masonry;
#import "SBGMacro.h"

@interface VLSBGSwitcherView()

@property (nonatomic, strong) UISwitch *switcher;
@property (nonatomic, strong) UILabel *subLabel;

@end

@implementation VLSBGSwitcherView


- (instancetype)init {
    if (self = [super init]) {
        [self initSubViews];
        [self addSubViewConstraints];
    }
    return self;
}

- (void)initSubViews {
    [self addSubview:self.switcher];
    [self addSubview:self.subLabel];
}

- (void)addSubViewConstraints {
    [self.switcher mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.bottom.mas_equalTo(self);
        make.left.mas_equalTo(79);
    }];
    
    [self.subLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(self);
        make.left.mas_equalTo(self.switcher.mas_right).offset(10);
    }];
    
}

- (void)setOn:(BOOL)on {
    _on = on;
    self.switcher.on = on;
}

- (void)setSubText:(NSString *)subText {
    _subText = subText;
    self.subLabel.text = _subText;
}

- (void)valueChanged:(UISwitch *)switcher {
    if ([self.delegate respondsToSelector:@selector(switcherView:on:)]) {
        [self.delegate switcherView:self on:switcher.on];
    }
}

- (UISwitch *)switcher {
    if (!_switcher) {
        _switcher = [[UISwitch alloc] init];
        _switcher.onTintColor = UIColorMakeWithHex(@"#009FFF");
        [_switcher addTarget:self action:@selector(valueChanged:) forControlEvents:UIControlEventValueChanged];
    }
    return _switcher;
}

- (UILabel *)subLabel {
    if (!_subLabel) {
        _subLabel = [[UILabel alloc] init];
        _subLabel.font = VLUIFontMake(12);
        _subLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    }
    return _subLabel;
}

@end
