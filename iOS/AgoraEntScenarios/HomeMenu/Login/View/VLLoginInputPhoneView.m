//
//  VLLoginInputPhoneView.m
//  VoiceOnLine
//

#import "VLLoginInputPhoneView.h"
#import "VLFontUtils.h"
#import "MenuUtils.h"
@import Masonry;
#import "AESMacro.h"

@interface VLLoginInputPhoneView()

@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UILabel *areaCodeLabel;
@property (nonatomic, strong) UIView *line;
@property (nonatomic, strong) UITextField *vTextField;

@end

@implementation VLLoginInputPhoneView

- (instancetype)init {
    if (self = [super init]) {
        [self initSubViews];
        [self addSubViewConstraints];
    }
    return self;
}

- (void)initSubViews {
    [self addSubview:self.titleLabel];
    [self addSubview:self.containerView];
    [self.containerView addSubview:self.areaCodeLabel];
    [self.containerView addSubview:self.line];
    [self.containerView addSubview:self.vTextField];
    
    self.containerView.backgroundColor = [UIColor whiteColor];
    self.containerView.layer.cornerRadius = 24;
    self.containerView.layer.shadowColor = [UIColor colorWithRed:168/255.0 green:195/255.0 blue:222/255.0 alpha:0.1500].CGColor;
    self.containerView.layer.shadowOffset = CGSizeMake(0,2);
    self.containerView.layer.shadowOpacity = 1;
    self.containerView.layer.shadowRadius = 8;
}

- (void)addSubViewConstraints {
    [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.left.mas_equalTo(0);
    }];
    
    [self.containerView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(self.titleLabel.mas_bottom).offset(5);
        make.left.right.mas_equalTo(0);
        make.height.mas_equalTo(48);
        make.bottom.mas_equalTo(self);
    }];
    
    [self.areaCodeLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(20);
        make.centerY.mas_equalTo(self.containerView);
    }];
    
    [self.line mas_makeConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo(15);
        make.centerY.mas_equalTo(self.areaCodeLabel);
        make.width.mas_equalTo(1);
        make.left.mas_equalTo(self.areaCodeLabel.mas_right).offset(10).priorityHigh();
    }];
    
    [self.vTextField mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self.line.mas_right).offset(10);
        make.top.bottom.mas_equalTo(0);
        make.right.mas_equalTo(-20);
    }];
    
    [self.line setContentCompressionResistancePriority:UILayoutPriorityDefaultLow
                                            forAxis:UILayoutConstraintAxisHorizontal];
    [self.vTextField setContentCompressionResistancePriority:UILayoutPriorityRequired
                                            forAxis:UILayoutConstraintAxisHorizontal];
}

- (NSString *)phoneNo {
    return _vTextField.text;
}

#pragma mark - delegate

- (void)textFieldDidChange:(UITextField *)textField {
    textField.text = [self truncateStringToElevenCharacters:textField.text];
}

- (NSString *)truncateStringToElevenCharacters:(NSString *)string {
    if (string.length > 11) {
        return [string substringToIndex:11];
    } else {
        return string;
    }
}

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] init];
        _titleLabel.text = AGLocalizedString(@"app_mobile_number");
        _titleLabel.textColor = UIColorMakeWithHex(@"#979CBB");
        _titleLabel.font = VLUIFontMake(15);
    }
    return _titleLabel;
}

- (UILabel *)areaCodeLabel {
    if (!_areaCodeLabel) {
        _areaCodeLabel = [[UILabel alloc] init];
        _areaCodeLabel.text = @"+86";
        _areaCodeLabel.textColor = UIColorMakeWithHex(@"#3C4267");
        _areaCodeLabel.font = VLUIFontMake(15);
    }
    return _areaCodeLabel;
}

- (UIView *)line {
    if (!_line) {
        _line = [[UIView alloc] init];
        _line.backgroundColor = UIColorMakeWithHex(@"#8A8B9B");
    }
    return _line;
}

- (UITextField *)vTextField {
    if (!_vTextField) {
        _vTextField = [[UITextField alloc] init];
        _vTextField.keyboardType = UIKeyboardTypeNumberPad;
        _vTextField.textColor = UIColorMakeWithHex(@"#3C4267");
        _vTextField.font = VLUIFontMake(15);
        [_vTextField addTarget:self action:@selector(textFieldDidChange:) forControlEvents:UIControlEventEditingChanged];
    }
    return _vTextField;
}

@end
