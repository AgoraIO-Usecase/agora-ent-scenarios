//
//  VLEmptyView.m
//  VoiceOnLine
//

#import "VLEmptyView.h"
#import "VLMacroDefine.h"
#import "NSString+Helper.h"
#import "MenuUtils.h"
#import "AESMacro.h"
@import Masonry;

@interface VLEmptyView ()

@property (nonatomic, weak) id <VLEmptyViewDelegate>delegate;
@property (nonatomic, strong) UIImageView *showImageView;
@property (nonatomic, strong) UILabel *textLabel;
@property (nonatomic, strong) UIButton *button;
@end

@implementation VLEmptyView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLEmptyViewDelegate>)delegate {
    
    if (self = [super initWithFrame:frame]) {
        
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (instancetype)initWithFrame:(CGRect)frame {
    
    if (self = [super initWithFrame:frame]) {
        [self setupView];
    }
    return self;
}
#pragma mark - Intial Methods
- (void)setupView {
    
    [self makeConstraintsSubViews];
}
- (void)makeConstraintsSubViews {
    
    [self addSubview:self.showImageView];
    [self.showImageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self).offset(VLREALVALUE_WIDTH(84));
        make.left.equalTo(self).offset(VLREALVALUE_WIDTH(97));
        make.width.mas_offset(VLREALVALUE_WIDTH(180));
        make.height.mas_offset(VLREALVALUE_WIDTH(180));
    }];
    
    [self addSubview:self.textLabel];
    [self.textLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.showImageView.mas_bottom).offset(VLREALVALUE_WIDTH(24));
        make.left.right.equalTo(self);
        make.height.mas_offset(VLREALVALUE_WIDTH(21));
    }];
    
    [self addSubview:self.detailTextLabel];
    [self.detailTextLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.textLabel.mas_bottom).offset(VLREALVALUE_WIDTH(10));
        make.left.right.equalTo(self);
        make.height.mas_offset(VLREALVALUE_WIDTH(17));
    }];
    
    [self addSubview:self.button];
    [self.button mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.detailTextLabel.mas_bottom).offset(VLREALVALUE_WIDTH(118));
        make.centerX.equalTo(self.mas_centerX);
        make.width.mas_offset(VLREALVALUE_WIDTH(92));
        make.height.mas_offset(VLREALVALUE_WIDTH(45));
    }];
}
#pragma mark - Events

#pragma mark - Public Methods
- (void)setupViewByImage:(UIImage *)image text:(NSString *)text detailText:(NSString *)detailText butttonTitle:(NSString *)buttonTitle {
    
    self.showImageView.image = image;
    self.textLabel.text = text;
    self.detailTextLabel.text = detailText;
    if ([NSString isBlankString:buttonTitle]) self.button.hidden = true;
    [self.button setTitle:buttonTitle forState:UIControlStateNormal];
}
- (void)buttonEvent {
    if ([self.button.titleLabel.text isEqualToString:AGLocalizedString(@"点击重试")]) {
        if (self.emptyViewButtonBlock) {
            self.emptyViewButtonBlock();
        }
    } else {
        [[self vj_viewController].navigationController popViewControllerAnimated:true];
    }
}
#pragma mark - Private Method

#pragma mark - External Delegate

#pragma mark – Getters and Setters
- (UIImageView *)showImageView {
    
    if (!_showImageView) {
        _showImageView = [[UIImageView alloc] init];
        _showImageView.contentMode = UIViewContentModeScaleAspectFit;
    }
    return _showImageView;
}

- (UIButton *)button {
    if (!_button) {
//        _button = [[QMUIButton alloc] qmui_initWithImage:nil title:AGLocalizedString(@"返回")];
        _button = [UIButton buttonWithType:UIButtonTypeCustom];
        [_button setTitle:AGLocalizedString(@"返回") forState:UIControlStateNormal];
        _button.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
        [_button addTarget:self action:@selector(buttonEvent) forControlEvents:UIControlEventTouchUpInside];
        [_button setTitleColor:UIColorMakeWithHex(@"#FFFFFF") forState:UIControlStateNormal];
        [_button setBackgroundColor:UIColorMakeWithHex(@"#00B58A")];
        _button.titleLabel.font = UIFontMake(12);
        _button.layer.cornerRadius = VLREALVALUE_WIDTH(45)/2.0;
    }
    return _button;
}

- (UILabel *)textLabel{
    if (!_textLabel) {
        _textLabel = [[UILabel alloc] init];
        _textLabel.textColor = UIColorMakeWithHex(@"#0E0E0E");
        _textLabel.font = UIFontMake(15);
        _textLabel.textAlignment = 1;
    }
    return _textLabel;
}

- (UILabel *)detailTextLabel{
    if (!_detailTextLabel) {
        _detailTextLabel = [[UILabel alloc] init];
        _detailTextLabel.textColor = UIColorMakeWithHex(@"#999999");
        _detailTextLabel.font = UIFontMake(12);
        _detailTextLabel.textAlignment = 1;
    }
    return _detailTextLabel;
}

@end
