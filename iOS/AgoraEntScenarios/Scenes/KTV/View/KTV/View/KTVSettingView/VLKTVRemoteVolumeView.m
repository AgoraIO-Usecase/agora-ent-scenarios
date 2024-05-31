//
//  VLKTVRemoteVolumeView.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/9.
//

#import "VLKTVRemoteVolumeView.h"
#import "AgoraEntScenarios-Swift.h"
#import "AESMacro.h"
@import Masonry;

@interface VLKTVRemoteVolumeView() <UITextFieldDelegate>
@property (nonatomic, assign) int minValue;
@property (nonatomic, assign) int maxValue;
@property (nonatomic, assign) int currentValue;

@property (nonatomic, strong) UITextField* inputField;
@property (nonatomic, strong) UIButton *addButton;
@property (nonatomic, strong) UIButton *reduceButton;
@property (nonatomic, strong) UILabel *inputTipsLabel;
@end

@implementation VLKTVRemoteVolumeView

- (UITextField*)inputField {
    if (!_inputField) {
        _inputField = [[UITextField alloc] init];
        _inputField.textAlignment = NSTextAlignmentCenter;
        _inputField.textColor = [UIColor whiteColor];
        //TODO: optimize
        _inputField.layer.cornerRadius = 5;
        _inputField.backgroundColor = UIColorMakeWithHex(@"#979CBB66");
        _inputField.keyboardType = UIKeyboardTypeNumberPad;
        _inputField.delegate = self;
//        _inputField.nu = 0;
    }
    
    return _inputField;
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

- (UILabel*)inputTipsLabel {
    if (!_inputTipsLabel) {
        _inputTipsLabel = [[UILabel alloc] init];
        _inputTipsLabel.font = self.titleLabel.font;
        _inputTipsLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    }
    return _inputTipsLabel;
}

- (void)setCurrentValue:(int)currentValue {
    _currentValue = currentValue;
    
    self.inputField.text = [NSString stringWithFormat:@"%d", self.currentValue];
}

- (id)initWithMin:(int)min withMax:(int)max withCurrent:(int)current {
    if (self = [super init]) {
        self.minValue = min;
        self.maxValue = max;
        self.currentValue = current;
        [self initSubViews];
        self.inputTipsLabel.text = [NSString stringWithFormat:KTVLocalizedString(@"RemoteVolumeTipsFormat"), min, max];
        [self addSubViewConstraints];
    }
    
    return self;
}

- (void)initSubViews {
    [self addSubview:self.inputField];
    [self addSubview:self.reduceButton];
    [self addSubview:self.addButton];
    [self addSubview:self.inputTipsLabel];
}

-(void)setCurrent:(int)current{
    self.inputField.text = [NSString stringWithFormat:@"%i", current];
}

- (void)addSubViewConstraints {
    CGFloat padding = 8;
    [self.reduceButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(79);
        make.centerY.mas_equalTo(self);
    }];
    
    [self.inputField mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.reduceButton.mas_right).offset(padding);
        make.width.mas_equalTo(40);
        make.height.equalTo(self);
        make.centerY.mas_equalTo(self);
    }];
    
    [self.addButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.inputField.mas_right).offset(padding);
        make.centerY.mas_equalTo(self);
    }];
    [self.inputTipsLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.addButton.mas_right).offset(padding);
        make.right.mas_lessThanOrEqualTo(0);
        make.centerY.mas_equalTo(self);
    }];
}

- (void)buttonClcik:(UIButton *)sender {
    if (sender == self.addButton) {
        if (self.currentValue == self.maxValue) return;
        self.currentValue++;
    } else {
        if (self.currentValue == 0) return;
        self.currentValue--;
    }
    if ([self.delegate respondsToSelector:@selector(view:remoteVolumeValueChanged:)]) {
        [self.delegate view:self remoteVolumeValueChanged:self.currentValue];
    }
}

#pragma mark UITextFieldDelegate
- (BOOL)textField:(UITextField *)textField
shouldChangeCharactersInRange:(NSRange)range
replacementString:(NSString *)string {
    NSMutableString* str = [NSMutableString stringWithString:textField.text];
    [str replaceCharactersInRange:range withString:string];
    NSLog(@"textField %@", str);
    int value = [str intValue];
    if (value >= self.minValue && value <= self.maxValue) {
        _currentValue = value;
        if ([self.delegate respondsToSelector:@selector(view:remoteVolumeValueChanged:)]) {
            [self.delegate view:self remoteVolumeValueChanged:self.currentValue];
        }
        return YES;
    }
    return NO;
}

@end
