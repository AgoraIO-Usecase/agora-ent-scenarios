//
//  VLPrivacyCustomView.m
//  VoiceOnLine
//

#import "VLPrivacyCustomView.h"
#import "VLFontUtils.h"
#import "VLMacroDefine.h"
#import "MenuUtils.h"
@import YYText;
@import Masonry;
@import QMUIKit;

@interface VLPrivacyCustomView()

@property (nonatomic, strong) UIScrollView *scrollView;
@property (nonatomic, strong) YYLabel *label;
@property (nonatomic, strong) UIButton *disButton;
@property (nonatomic, strong) UIButton *agreeButton;

@end

@implementation VLPrivacyCustomView

- (instancetype)initWithPass:(int)pass {
    if (self = [super init]) {
        _pass = pass;
        [self initSubViews];
        [self addSubViewConstraints];
    }
    return self;
}

- (void)initSubViews {
    [self addSubview:self.scrollView];
    [self.scrollView addSubview:self.label];
    
    [self addSubview:self.disButton];
    [self addSubview:self.agreeButton];
}

- (void)addSubViewConstraints {
    [self.scrollView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.left.right.mas_equalTo(self);
        if(_pass == 0) {
            make.height.mas_greaterThanOrEqualTo(200);
        }
        else {
            make.height.mas_greaterThanOrEqualTo(50);
        }
    }];
    
    [self.label mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self.scrollView);
    }];
    
    [self.disButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.mas_equalTo(self.scrollView.mas_bottom).offset(20);
        make.left.mas_equalTo(0);
        make.width.mas_equalTo(115);
        make.height.mas_equalTo(40);
    }];
    
    [self.agreeButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.mas_equalTo(115);
        make.height.mas_equalTo(40);
        make.top.mas_equalTo(self.scrollView.mas_bottom).offset(20);
        make.right.mas_equalTo(0);
    }];
}

- (void)buttonClick:(UIButton *)sender {
    if ([self.delegate respondsToSelector:@selector(privacyCustomViewDidClick:)]) {
        VLPrivacyClickType type;
        if (sender == self.agreeButton) {
            type = VLPrivacyClickTypeAgree;
            [AppContext shared].isAgreeLicense = YES;
        } else {
            type = VLPrivacyClickTypeDisagree;
        }
        [self.delegate privacyCustomViewDidClick:type];
    }
}

#pragma mark - Lazy

- (UIScrollView *)scrollView {
    if (!_scrollView) {
        _scrollView = [[UIScrollView alloc] init];
    }
    return _scrollView;
}

- (YYLabel *)label {
    if (!_label) {
        _label = [[YYLabel alloc] init];
        _label.numberOfLines = 0;
        _label.textColor = UIColorMakeWithHex(@"#6C7192");
        _label.font = VLUIFontMake(12);
        _label.preferredMaxLayoutWidth = 250;
        NSString *_str4Total = nil;
        if(_pass == 0) {
            _str4Total = AGLocalizedString(@"声动互娱软件是一款用于向声网客户展示产品使用效果的测试产品，仅用于测试产品的功能、性能和可用性，而非提供给大众使用的正式产品。\n1.我们将依据《用户协议》及《隐私政策》来帮助您了解我们在收集、使用、存储您个人信息的情况以及您享有的相关权利。\n2.在您使用本测试软件时，我们将收集您的设备信息、日志信息等，同时根据不同使用场景，你可以授予我们获取您设备的麦克风权限、摄像头权限等信息。\n\n您可通过阅读完整的《用户协议》及《隐私政策》来了解详细信息。");
        }
        else {
            _str4Total = AGLocalizedString(@"同意 用户协议 及 隐私政策 后，声动互娱才能为您提供协作服务。");
        }
        
        NSString *_str4Highlight1 = AGLocalizedString(@"用户协议");
        NSString *_str4Highlight2 = AGLocalizedString(@"隐私政策");
        NSMutableAttributedString *_mattrStr = [NSMutableAttributedString new];
        
        [_mattrStr appendAttributedString:[[NSAttributedString alloc] initWithString:_str4Total attributes:@{NSFontAttributeName : VLUIFontMake(12), NSForegroundColorAttributeName : UIColorMakeWithHex(@"#6C7192")}]];
        _mattrStr.yy_lineSpacing = 6;
        
        NSArray *array = [_str4Total componentsSeparatedByString:_str4Highlight1];
        int d=0;
        for(int i=0; i<array.count-1; i++) {
            NSString *subString = array[i];
            NSNumber *number = [NSNumber numberWithInt:d += subString.length];
            d += _str4Highlight1.length;
            
            NSRange range1 = NSMakeRange([number intValue], _str4Highlight1.length);
            [_mattrStr addAttribute:NSUnderlineStyleAttributeName value:[NSNumber numberWithInteger:NSUnderlineStyleSingle] range:range1];
            kWeakSelf(self)
            [_mattrStr yy_setTextHighlightRange:range1 color:UIColorMakeWithHex(@"#009FFF") backgroundColor:[UIColor clearColor] tapAction:^(UIView * _Nonnull containerView, NSAttributedString * _Nonnull text, NSRange range, CGRect rect) {
                if ([weakself.delegate respondsToSelector:@selector(privacyCustomViewDidClick:)]) {
                    [weakself.delegate privacyCustomViewDidClick:VLPrivacyClickTypeUserAgreement];
                }
            }];
        }
        
        NSArray *array2 = [_str4Total componentsSeparatedByString:_str4Highlight2];
        d=0;
        for(int i=0; i<array.count-1; i++) {
            NSString *subString = array2[i];
            NSNumber *number = [NSNumber numberWithInt:d += subString.length];
            d += _str4Highlight2.length;
            
            NSRange range2 = NSMakeRange([number intValue], _str4Highlight2.length);
            [_mattrStr addAttribute:NSUnderlineStyleAttributeName value:[NSNumber numberWithInteger:NSUnderlineStyleSingle] range:range2];
            kWeakSelf(self)
            [_mattrStr yy_setTextHighlightRange:range2 color:UIColorMakeWithHex(@"#009FFF") backgroundColor:[UIColor clearColor] tapAction:^(UIView * _Nonnull containerView, NSAttributedString * _Nonnull text, NSRange range, CGRect rect) {
                if ([weakself.delegate respondsToSelector:@selector(privacyCustomViewDidClick:)]) {
                    [weakself.delegate privacyCustomViewDidClick:VLPrivacyClickTypePrivacy];
                }
            }];
        }
        
        _label.lineBreakMode = NSLineBreakByWordWrapping;
        _label.attributedText = _mattrStr;
    }
    return _label;
}

- (UIButton *)disButton {
    if (!_disButton) {
        _disButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_disButton addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        if(_pass == 0) {
            [_disButton setTitle:AGLocalizedString(@"不同意") forState:UIControlStateNormal];
        }
        else {
            [_disButton setTitle:AGLocalizedString(@"不同意并退出") forState:UIControlStateNormal];
        }
        
        [_disButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        _disButton.titleLabel.font = VLUIFontMake(16);
        [_disButton setBackgroundColor:UIColorMakeWithHex(@"#EFF4FF")];
        _disButton.layer.backgroundColor = [UIColor colorWithRed:239/255.0 green:244/255.0 blue:255/255.0 alpha:1.0].CGColor;
        _disButton.layer.cornerRadius = 20;
        _disButton.layer.shadowColor = [UIColor colorWithRed:0/255.0 green:0/255.0 blue:0/255.0 alpha:0.23].CGColor;
        _disButton.layer.shadowOffset = CGSizeMake(0,5);
        _disButton.layer.shadowOpacity = 1;
        _disButton.layer.shadowRadius = 20;
    }
    return _disButton;
}

- (UIButton *)agreeButton {
    if (!_agreeButton) {
        _agreeButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_agreeButton addTarget:self action:@selector(buttonClick:) forControlEvents:UIControlEventTouchUpInside];
        if(_pass == 0) {
            [_agreeButton setTitle:AGLocalizedString(@"同意") forState:UIControlStateNormal];
        }
        else {
            [_agreeButton setTitle:AGLocalizedString(@"同意并继续") forState:UIControlStateNormal];
        }
        
        [_agreeButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _agreeButton.titleLabel.font = VLUIFontMake(16);
        [_agreeButton setBackgroundColor:UIColorMakeWithHex(@"#2753FF")];
        // gradient
        CAGradientLayer *gl = [CAGradientLayer layer];
        gl.frame = CGRectMake(197.5,498.5,115,40);
        gl.startPoint = CGPointMake(0.43, 0);
        gl.endPoint = CGPointMake(0.43, 1);
        gl.colors = @[(__bridge id)[UIColor colorWithRed:11/255.0 green:138/255.0 blue:242/255.0 alpha:1].CGColor, (__bridge id)[UIColor colorWithRed:39/255.0 green:83/255.0 blue:255/255.0 alpha:1].CGColor];
        gl.locations = @[@(0), @(1.0f)];
        _agreeButton.layer.cornerRadius = 20;
        _agreeButton.layer.shadowColor = [UIColor colorWithRed:0/255.0 green:0/255.0 blue:0/255.0 alpha:0.1000].CGColor;
        _agreeButton.layer.shadowOffset = CGSizeMake(0,5);
        _agreeButton.layer.shadowOpacity = 1;
        _agreeButton.layer.shadowRadius = 20;
    }
    return _agreeButton;
}

@end
