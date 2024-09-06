//
//  VLSRSwitcherView.m
//  VoiceOnLine
//

#import "VLSettingSwitcherView.h"
#import "AESMacro.h"
@import Masonry;

@interface VLSettingSwitcherView()

@property (nonatomic, strong) UISwitch *switcher;
@property (nonatomic, strong) UILabel *subLabel;
@property (nonatomic, strong) UIView *lineView;
@end

@implementation VLSettingSwitcherView


- (instancetype)init {
    if (self = [super init]) {
        [self initSubViews];
        [self addSubViewConstraints];
    }
    return self;
}

- (void)initSubViews {
    [self addSubview:self.switcher];
    self.subLabel.font = [UIFont systemFontOfSize:15];
    self.subLabel.textColor = [UIColor whiteColor];
    [self addSubview:self.subLabel];
    self.lineView = [UIView new];
    self.lineView.backgroundColor = [UIColor separatorColor];
    [self addSubview:self.lineView];
}

- (void)addSubViewConstraints {
    [self.switcher mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(self);
        make.right.mas_equalTo(self).offset(-10);
    }];
    
    [self.subLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(self);
        make.left.mas_equalTo(self).offset(20);
    }];
    [self.lineView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo(1);
        make.left.mas_equalTo(self).offset(20);
        make.right.mas_equalTo(self).offset(-10);
        make.bottom.mas_equalTo(self);
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
    if ([self.delegate respondsToSelector:@selector(switcherSetView:on:)]) {
        [self.delegate switcherSetView:self on:switcher.on];
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
        _subLabel.font = [UIFont systemFontOfSize:12];
        _subLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    }
    return _subLabel;
}

@end
