//
//  UILabel+Font.m
//  VoiceOnLine
//

#import "UILabel+Font.h"
#import "VLFontUtils.h"
#import <objc/runtime.h>

@implementation UILabel (Font)

+ (void)load {
    
    Method imp = class_getInstanceMethod([self class], @selector(initWithCoder:));
    Method myImp = class_getInstanceMethod([self class], @selector(fontInitWithCoder:));
    method_exchangeImplementations(imp, myImp);
}

- (id)fontInitWithCoder:(NSCoder *)aDecode {
    
    [self fontInitWithCoder:aDecode];
    if (self) {
        
        if (self.tag != VLFontTag) {
            
            NSArray *nameArray = @[@"PingFangSC-Semibold",
                                   @".SFUIDisplay-Bold",
                                   @".SFUIDisplay-Semibold",
                                   @".SFUIText-Semibold",
                                   @".SFUIText-Bold"];
            
            CGFloat fontSize = self.font.pointSize;
            if ([nameArray containsObject:self.font.fontName]) {

                self.font = VLUIFontBoldMake(fontSize);
            } else if ([self.font.fontName isEqualToString:@".SFUIDisplay-Medium"] ||
                       [self.font.fontName isEqualToString:@".SFUIText-Medium"]) {
                
                self.font = VLUIFontMediumMake(fontSize);
            } else {
                
                self.font = VLUIFontMake(fontSize);
            }
        }
    }
    return self;
}

@end
