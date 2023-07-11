//
//  VLLoginInputVerifyCodeView.m
//  VoiceOnLine
//

#import "VLLoginInputVerifyCodeView.h"
#import "VLMacroDefine.h"
#import "VLFontUtils.h"
#import "MenuUtils.h"
#import "AESMacro.h"
@import Masonry;

@interface VLLoginInputVerifyCodeView()

@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UITextField *vTextField;
@property (nonatomic, strong) UIButton *vSendbutton;
@property (nonatomic, strong) dispatch_source_t timer;

@end

@implementation VLLoginInputVerifyCodeView

- (instancetype)init {
    if (self = [super init]) {
        [self initSubViews];
        [self addSubViewConstraints];
        
        _isVerifyCodeSent = NO;
    }
    return self;
}

- (void)initSubViews {
    [self addSubview:self.titleLabel];
    [self addSubview:self.containerView];
    [self.containerView addSubview:self.vTextField];
    [self.containerView addSubview:self.vSendbutton];
    
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
    
    [self.vTextField mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(20);
        make.top.bottom.mas_equalTo(0);
        make.right.mas_equalTo(self.vSendbutton.mas_right);
    }];
    
    [self.vSendbutton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.bottom.mas_equalTo(0);
        make.right.mas_equalTo(-20);
    }];
}

#pragma mark - Action

- (void)buttonClick:(UIButton *)button {
    if ([self.delegate respondsToSelector:@selector(verifyCodeViewDidClickSendVerifyCode:)]) {
        [self.delegate verifyCodeViewDidClickSendVerifyCode:button];
        _isVerifyCodeSent = YES;
    }
}

-(void)startTime:(UIButton *)sender {
    __block int timeout = 59; //倒计时时间
    dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    self.timer = dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0, 0,queue);
    dispatch_source_set_timer(self.timer,dispatch_walltime(NULL, 0), 1.0 * NSEC_PER_SEC, 0); //每秒执行
    kWeakSelf(self)
    dispatch_source_set_event_handler(self.timer, ^{
        kStrongSelf(self)
        if(timeout <= 0){ //倒计时结束，关闭
            dispatch_source_cancel(self.timer);
            dispatch_async(dispatch_get_main_queue(), ^{
                //设置界面的按钮显示 根据自己需求设置（倒计时结束后调用）
                [sender setTitle:AGLocalizedString(@"发送验证码") forState:UIControlStateNormal];
                //设置不可点击
                sender.userInteractionEnabled = YES;
            });
        } else {
            int seconds = timeout % 60;
            NSString *strTime = [NSString stringWithFormat:@"%d", seconds];
            dispatch_async(dispatch_get_main_queue(), ^{
                //设置界面的按钮显示 根据自己需求设置
                VLLog(@"verify timer%@",strTime);
                [sender setTitle:[NSString stringWithFormat:AGLocalizedString(@"%@秒后可重新发送"), strTime] forState:UIControlStateNormal];
                    //设置可点击
                sender.userInteractionEnabled = NO;
            });
            timeout--;
        }
    });
    dispatch_resume(self.timer);
}

#pragma mark - Public

- (NSString *)verifyCode {
    return self.vTextField.text;
}

#pragma mark - Lazy

- (UILabel *)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] init];
        _titleLabel.text = AGLocalizedString(@"验证码");
        _titleLabel.textColor = UIColorMakeWithHex(@"#979CBB");
        _titleLabel.font = VLUIFontMake(14);
    }
    return _titleLabel;
}

- (UITextField *)vTextField {
    if (!_vTextField) {
        _vTextField = [[UITextField alloc] init];
        _vTextField.attributedPlaceholder = [[NSAttributedString alloc] initWithString:AGLocalizedString(@"请输入验验证码") attributes:@{NSFontAttributeName:VLUIFontMake(15),NSForegroundColorAttributeName:UIColorMakeWithHex(@"#979CBB")}];
        _vTextField.keyboardType = UIKeyboardTypeNumberPad;
        _vTextField.textColor = UIColorMakeWithHex(@"#3C4267");
        _vTextField.font = VLUIFontMake(15);
    }
    return _vTextField;
}

- (UIButton *)vSendbutton {
    if (!_vSendbutton) {
        _vSendbutton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_vSendbutton setTitle:AGLocalizedString(@"发送验证码") forState:UIControlStateNormal];
        [_vSendbutton setTitleColor:UIColorMakeWithHex(@"#009FFF") forState:UIControlStateNormal];
        _vSendbutton.titleLabel.font = VLUIFontMake(14);
        [_vSendbutton addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _vSendbutton;
}

- (void)dealloc {
    if (_timer) {
        dispatch_source_cancel(_timer);
        _timer = nil;
    }
    NSLog(@"VLLoginInputVerifyCodeView dealloc");
}

@end
