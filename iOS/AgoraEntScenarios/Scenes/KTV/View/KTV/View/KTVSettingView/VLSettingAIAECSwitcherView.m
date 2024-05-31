//
//  VLSRSwitcherView.m
//  VoiceOnLine
//

#import "VLSettingAIAECSwitcherView.h"
#import "AESMacro.h"
#import "VLToast.h"
@import Masonry;

@interface VLSettingAIAECSwitcherView()<UITextFieldDelegate>

@property (nonatomic, strong) UISwitch *switcher;
@property (nonatomic, strong) UILabel *nameLabel;
@property (nonatomic, strong) UILabel *subLabel;
@property (nonatomic, strong) UILabel *placeLabel;
@property (nonatomic, strong) UITextField *tf;
@property (nonatomic, assign) NSInteger level;
@property (nonatomic, strong) UIView *lineView;
@end

@implementation VLSettingAIAECSwitcherView


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
    self.subLabel.hidden = true;
    self.aecValue = 0;
    self.tf = [UITextField new];
    self.tf.keyboardType = UIKeyboardTypeNumberPad;
    self.tf.layer.cornerRadius = 5;
    self.tf.layer.masksToBounds = true;
    self.tf.textColor = [UIColor whiteColor];
    self.tf.delegate = self;
    self.tf.textAlignment = UITextAlignmentCenter;
    self.tf.backgroundColor = [UIColor colorWithRed:216/255.0 green:216/255.0 blue:216/255.0 alpha:0.08];
    self.tf.hidden = true;
    
    self.lineView = [UIView new];
    self.lineView.backgroundColor = [UIColor separatorColor];
    [self addSubview:self.lineView];
    
    [self addSubview:self.tf];
    [self addSubview:self.placeLabel];
    self.placeLabel.hidden = true;
}

- (void)addSubViewConstraints {
    [self.switcher mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(self).offset(10);
        make.right.mas_equalTo(-10);
    }];
    
    [self.nameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(self.switcher);
        make.left.mas_equalTo(20);
    }];
    
    [self.tf mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.mas_equalTo(self).offset(-10);
        make.right.mas_equalTo(self).offset(-10);
        make.width.mas_equalTo(@(50));
        make.height.mas_equalTo(@(28));
    }];
    
    [self.subLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(self.tf);
        make.left.mas_equalTo(20);
    }];
    
    [self.placeLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(self.tf);
        make.right.mas_equalTo(self.tf.mas_left).offset(-10);
    }];
    
    [self.lineView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo(1);
        make.left.mas_equalTo(self).offset(20);
        make.right.mas_equalTo(self).offset(-10);
        make.top.mas_equalTo(self).offset(54);
    }];
    
}

- (void)setOn:(BOOL)on {
    _on = on;
    self.switcher.on = on;
    [self updateUIWith:on];
}

-(void)setAecValue:(NSInteger)aecValue {
    _aecValue = aecValue;
    self.tf.text = [NSString stringWithFormat:@"%ld", aecValue];
}

-(void)updateUIWith:(BOOL)on{
    [self.lineView mas_updateConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(self).offset(on ? 90 : 54);
    }];
    self.subLabel.hidden = !on;
    self.placeLabel.hidden = !on;
    self.tf.hidden = !on;
}

- (void)setSubText:(NSString *)subText {
    _subText = subText;
    self.subLabel.text = _subText;
}

- (void)valueChanged:(UISwitch *)switcher {
    _on = switcher.on;
    [self updateUIWith:_on];
    if ([self.delegate respondsToSelector:@selector(aecSwitcherView:on:)]) {
        [self.delegate aecSwitcherView:self on:switcher.on];
    }
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    NSInteger level = [string integerValue];
    self.tf.text = @"0";
    [self endEditing:true];
    if (string.length == 0) {
        return true;
    }
    
    if (level < 0 || level > 4) {
        self.tf.text = [NSString stringWithFormat:@"%li", (long)_aecValue];
        [VLToast toast:KTVLocalizedString(@"ktv_aiaec_rule")];
        self.level = _aecValue;
        return true;
    }
    
    self.level = level;
    self.tf.text = string;
    
    if ([self.delegate respondsToSelector:@selector(aecSwitcherView:level:)]) {
        [self.delegate aecSwitcherView:self level:self.level];
    }
    
    return true;
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
        _nameLabel.text = KTVLocalizedString(@"ktv_aiaec_switch");
        _nameLabel.textColor = [UIColor whiteColor];
    }
    return _nameLabel;
}


- (UILabel *)subLabel {
    if (!_subLabel) {
        _subLabel = [[UILabel alloc] init];
        _subLabel.font = [UIFont systemFontOfSize:13];
        _subLabel.text = KTVLocalizedString(@"ktv_aiaec_level");
        _subLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    }
    return _subLabel;
}

- (UILabel *)placeLabel {
    if (!_placeLabel) {
        _placeLabel = [[UILabel alloc] init];
        _placeLabel.font = [UIFont systemFontOfSize:13];
        _placeLabel.text = KTVLocalizedString(@"ktv_aiaec_rule");
        _placeLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    }
    return _placeLabel;
}

@end
