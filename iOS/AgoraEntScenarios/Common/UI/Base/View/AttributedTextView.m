//
//  AttributedTextView.m
//  text
//
//  Created by CP on 2023/1/4.
//

#import "AttributedTextView.h"

@implementation AttributedTextView

-(instancetype)initWithFrame:(CGRect)frame
                        text:(NSString *)text
                        font: (UIFont *)font
           AttributedStringS:(NSArray *)strings
                      ranges:(NSArray *)ranges
                   textColor:(UIColor *)textColor
          attributeTextColor:(UIColor *)attributeTextColor {
    return [self initWithFrame:frame
                          text:text
                          font: font
             AttributedStringS:strings
                        ranges:ranges
                     textColor:textColor
            attributeTextColor:attributeTextColor
                         attrs:nil];
}

-(instancetype)initWithFrame:(CGRect)frame
                        text:(NSString *)text
                        font: (UIFont *)font
           AttributedStringS:(NSArray *)strings
                      ranges:(NSArray *)ranges
                   textColor:(UIColor *)textColor
          attributeTextColor:(UIColor *)attributeTextColor
                       attrs:(nullable NSMutableAttributedString *)attrs {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = [UIColor clearColor];
        self.editable = NO;
        self.textAlignment = NSTextAlignmentLeft;
        self.dataDetectorTypes = UIDataDetectorTypeAll;

        NSMutableParagraphStyle *style = [[NSMutableParagraphStyle alloc] init];
        style.firstLineHeadIndent = 1;
        style.lineSpacing= 5;
        NSDictionary *attachDic = @{
                                   NSFontAttributeName: font,
                                   NSForegroundColorAttributeName:textColor,
                                   NSParagraphStyleAttributeName:style
                                   };
        NSMutableAttributedString *strM = [[NSMutableAttributedString alloc] initWithString:text attributes:attachDic];
        if (attrs) {
            [strM appendAttributedString:attrs];
        }
        //设置文字颜色
        for(int i=0;i< ranges.count; i++){
            NSValue *value = ranges[i];
            [strM addAttribute:NSForegroundColorAttributeName value:attributeTextColor range:value.rangeValue];
           // NSString *link = [[NSString stringWithFormat:@"%@://%@",[NSString stringWithFormat:@"%@_%i",strings[i],i],strings[i]] stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLFragmentAllowedCharacterSet]];
            NSString *link = [[NSString stringWithFormat:@"%i",i] stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLFragmentAllowedCharacterSet]];
            [strM addAttribute:NSLinkAttributeName value:link range:value.rangeValue];
            [strM addAttribute:NSUnderlineStyleAttributeName value:@(NSUnderlineStyleNone) range:value.rangeValue];
        }
        
        //清除超链接本身的颜色
        self.linkTextAttributes = @{};
        self.attributedText = strM;
    }
    return self;
}

@end
