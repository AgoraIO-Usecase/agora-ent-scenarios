//
//  VLAlert.m
//  testAlert
//
//  Created by CP on 2023/1/6.
//

#import "VLKTVAlert.h"

@interface VLKTVAlert()
@property (nonatomic, strong) UIView *bgView;
@property (nonatomic, strong) UIView *alertView;
@property (nonatomic, strong) UILabel *mesLabel;
@property (nonatomic, strong) UIButton *confirmBtn;
@property (nonatomic, strong) UIButton *cancleBtn;
@property (nonatomic, strong) NSString *message;
//KTV独有
@property (nonatomic, strong) UIImageView *iconView;
@property (nonatomic, copy) OnCallback completion;
@property (nonatomic, assign) bool isShowing;
@end

@implementation VLKTVAlert

static VLKTVAlert *_alert = nil;
+ (instancetype)shared
{
    if (!_alert) {
        _alert = [[VLKTVAlert alloc] init];
    }
    return _alert;
}

-(void)showKTVToastWithFrame:(CGRect)frame image:(UIImage *)image message:(NSString *_Nullable)message buttonTitle:(NSString *)buttonTitle completion:(OnCallback _Nullable)completion {
    if(self.isShowing){return;}
    [self layoutUI];
    self.completion = completion;
    self.message = message;
    self.iconView.image = image;
    self.mesLabel.text = message;
    [self.confirmBtn setTitle:buttonTitle forState:UIControlStateNormal];
    [UIApplication.sharedApplication.delegate.window addSubview:self];
    self.isShowing = true;
}

-(void)layoutUI {
    
    self.backgroundColor = [UIColor clearColor];
    
    self.bgView = [[UIView alloc]init];
    self.bgView.backgroundColor = [UIColor blackColor];
    self.bgView.alpha = 0.2;
    [self addSubview:self.bgView];
    
    self.alertView = [[UIView alloc]init];
    self.alertView.backgroundColor = [UIColor whiteColor];
    self.alertView.layer.cornerRadius = 10;
    self.alertView.layer.masksToBounds = true;
    [self addSubview:self.alertView];
    
    self.iconView = [[UIImageView alloc]init];
    [self.alertView addSubview:self.iconView];
    
    self.mesLabel = [[UILabel alloc]init];
    self.mesLabel.numberOfLines = 0;
    self.mesLabel.textColor = [UIColor colorWithRed:0.235 green:0.257 blue:0.403 alpha:1];
    self.mesLabel.font = [UIFont systemFontOfSize:14];
    self.mesLabel.lineBreakMode = NSLineBreakByCharWrapping;
    self.mesLabel.textAlignment = NSTextAlignmentCenter;
    [self.alertView addSubview:self.mesLabel];

    
    self.confirmBtn = [[UIButton alloc]init];
    self.confirmBtn.backgroundColor = [self colorWithHexString:@"#2753FF"];
    [self.confirmBtn setTitleColor:[self colorWithHexString:@"#FFFFFF"] forState:UIControlStateNormal];
    [self.confirmBtn setFont:[UIFont systemFontOfSize:16 weight:UIFontWeightBold]];
    self.confirmBtn.layer.cornerRadius = 20;
    self.confirmBtn.layer.masksToBounds = true;
    self.confirmBtn.tag = 101;
    [self.confirmBtn addTarget:self action:@selector(click:) forControlEvents:UIControlEventTouchUpInside];
    [self.alertView addSubview:self.confirmBtn];

}


-(void)click:(UIButton *)btn {
    self.completion(btn.tag == 101, nil);
}

-(void)dismiss {
    [self removeFromSuperview];
    _alert = nil;
}

-(void)layoutSubviews{
    [super layoutSubviews];
    self.frame = UIScreen.mainScreen.bounds;
    self.bgView.frame = UIScreen.mainScreen.bounds;
    //1.判断是那种alert，然后计算高度
    CGFloat contentHeight = 20 + 120 + 10;
    CGFloat mesHeight = [self heightWithString:self.message];
     
    contentHeight += mesHeight;

    contentHeight += 20;
    contentHeight += 40;
    contentHeight += 20;
    self.alertView.frame = CGRectMake(40, ([UIScreen mainScreen].bounds.size.height - contentHeight) / 2.0, [[UIScreen mainScreen] bounds].size.width - 80, contentHeight);
    self.iconView.frame = CGRectMake(20, 20, self.alertView.bounds.size.width - 40, 120);
    self.mesLabel.frame = CGRectMake(20, 150, self.alertView.bounds.size.width - 40, mesHeight);

    self.confirmBtn.frame = CGRectMake(self.alertView.bounds.size.width / 2.0 - 60, contentHeight - 60,120, 40);

}

- (CGFloat)heightWithString:(NSString *)text {
    CGSize textSize = CGSizeMake([[UIScreen mainScreen] bounds].size.width - 120, 0);
    NSDictionary *font = @{NSFontAttributeName : [UIFont systemFontOfSize:15]};
    CGRect rect = [text boundingRectWithSize:textSize options:NSStringDrawingUsesLineFragmentOrigin | NSStringDrawingUsesFontLeading attributes:font context:nil];
    return rect.size.height;
    
}

- (UIColor *)colorWithHexString:(NSString *)color alpha:(CGFloat)alpha {
    // 删除字符串中的空格
    NSString * colorStr = [[color stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] uppercaseString];
    
    // String should be 6 or 8 characters
    if ([colorStr length] < 6) {
        return [UIColor clearColor];
    }
    
    // strip 0X if it appears
    // 如果是0x开头的，那么截取字符串，字符串从索引为2的位置开始，一直到末尾
    if ([colorStr hasPrefix:@"0X"]) {
        colorStr = [colorStr substringFromIndex:2];
    }
    
    // 如果是#开头的，那么截取字符串，字符串从索引为1的位置开始，一直到末尾
    if ([colorStr hasPrefix:@"#"]) {
        colorStr = [colorStr substringFromIndex:1];
    }
    
    // 除去所有开头字符后 再判断字符串长度
    if ([colorStr length] != 6) {
        return [UIColor clearColor];
    }
    
    // Separate into r, g, b substrings
    NSRange range;
    range.location = 0;
    range.length = 2;
    //red
    NSString * redStr = [colorStr substringWithRange:range];
    //green
    range.location = 2;
    NSString * greenStr = [colorStr substringWithRange:range];
    //blue
    range.location = 4;
    NSString * blueStr = [colorStr substringWithRange:range];
    
    // Scan values 将十六进制转换成二进制
    unsigned int r, g, b;
    [[NSScanner scannerWithString:redStr] scanHexInt:&r];
    [[NSScanner scannerWithString:greenStr] scanHexInt:&g];
    [[NSScanner scannerWithString:blueStr] scanHexInt:&b];
    return [UIColor colorWithRed:((float)r / 255.0f) green:((float)g / 255.0f) blue:((float)b / 255.0f) alpha:alpha];
}

- (UIColor *)colorWithHexString:(NSString *)color {
    return [self colorWithHexString:color alpha:1.0f];
}

@end
