//
//  VLSRSwitcherView.m
//  VoiceOnLine
//

#import "VLSettingNetSwitcherView.h"
#import "AESMacro.h"
@import Masonry;

@interface VLSettingNetSwitcherView()

@property (nonatomic, strong) UISwitch *switcher;
@property (nonatomic, strong) UILabel *nameLabel;
@property (nonatomic, strong) UILabel *subLabel;
@property (nonatomic, strong) UIView *lineView;
@end

@implementation VLSettingNetSwitcherView


- (instancetype)init {
    if (self = [super init]) {
        [self initSubViews];
        [self addSubViewConstraints];
    }
    return self;
}

- (void)initSubViews {
    [self addSubview:self.switcher];
    [self addSubview:self.nameLabel];
    [self addSubview:self.subLabel];
    self.lineView = [UIView new];
    self.lineView.backgroundColor = [UIColor separatorColor];
    [self addSubview:self.lineView];
}

- (void)addSubViewConstraints {
    [self.switcher mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(self);
        make.right.mas_equalTo(-10);
    }];
    
    [self.nameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(-10);
        make.left.mas_equalTo(20);
        make.top.mas_equalTo(8);
    }];
    
    [self.subLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(-10);
        make.left.mas_equalTo(20);
        make.top.mas_equalTo(self.nameLabel.mas_bottom);
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
    if ([self.delegate respondsToSelector:@selector(switcherNetView:on:)]) {
        [self.delegate switcherNetView:self on:switcher.on];
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

- (UILabel *)nameLabel {
    if (!_nameLabel) {
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.font = [UIFont systemFontOfSize:15];
        _nameLabel.text = KTVLocalizedString(@"ktv_use_4G");
        _nameLabel.textColor = [UIColor whiteColor];
    }
    return _nameLabel;
}


- (UILabel *)subLabel {
    if (!_subLabel) {
        _subLabel = [[UILabel alloc] init];
        _subLabel.font = [UIFont systemFontOfSize:13];
        _subLabel.text = KTVLocalizedString(@"ktv_bad_wifi");
        _subLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    }
    return _subLabel;
}

@end
