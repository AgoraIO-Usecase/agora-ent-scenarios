//
//  VLFontUtils.m
//  VoiceOnLine
//

#import "VLFontUtils.h"
#import <CoreText/CoreText.h>
#import <objc/runtime.h>
@import UIKit;

NSUInteger const VLFontTag = 7101746;

@implementation VLFontUtils

//系统默认使用字体
UIFont * SystemRegularFontSize(CGFloat size) {
    
    return [UIFont systemFontOfSize:size weight:UIFontWeightRegular];
}

UIFont * SystemBoldFontSize(CGFloat size) {
    
    return [UIFont systemFontOfSize:size weight:UIFontWeightSemibold];
}

UIFont * SystemMediumFontSize(CGFloat size) {
    
    return [UIFont systemFontOfSize:size weight:UIFontWeightMedium];
}

UIFont * AppSystemFont(BOOL isBold,
                    CGFloat inch_3_5,
                    CGFloat inch_4_0,
                    CGFloat inch_4_7,
                    CGFloat inch_5_5,
                    CGFloat inch_5_8,
                    CGFloat inch_6_1,
                    CGFloat inch_6_5) {
    if (VLIS_35INCH_SCREEN) {
        
        return isBold ? SystemBoldFontSize(inch_3_5) : SystemRegularFontSize(inch_3_5);
    }
    if (VLIS_40INCH_SCREEN) {
        
        return isBold ? SystemBoldFontSize(inch_4_0) : SystemRegularFontSize(inch_4_0);
    }
    if (VLIS_47INCH_SCREEN) {
        
        return isBold ? SystemBoldFontSize(inch_4_7) : SystemRegularFontSize(inch_4_7);
    }
    if (VLIS_55INCH_SCREEN) {
        
        return isBold ? SystemBoldFontSize(inch_5_5) : SystemRegularFontSize(inch_5_5);
    }
    if (VLIS_58INCH_SCREEN) {
        
        return isBold ? SystemBoldFontSize(inch_5_8) : SystemRegularFontSize(inch_5_8);
    }
    if (VLIS_61INCH_SCREEN) {
        
        return isBold ? SystemBoldFontSize(inch_6_1) : SystemRegularFontSize(inch_6_1);
    }
    if (VLIS_65INCH_SCREEN) {
        
        return isBold ? SystemBoldFontSize(inch_6_5) : SystemRegularFontSize(inch_6_5);
    }
    return isBold ? SystemBoldFontSize(inch_4_7) : SystemRegularFontSize(inch_4_7);
}


/*************************************************宏定义字体Function******************************************************************/

/*
 * 系统默认字体
 */
//使用常规字体
UIFont * VLSystemRegularFont(CGFloat inch_3_5,
                            CGFloat inch_4_0,
                            CGFloat inch_4_7,
                            CGFloat inch_5_5,
                            CGFloat inch_5_8,
                            CGFloat inch_6_1,
                            CGFloat inch_6_5) {
    
    return AppSystemFont(NO, inch_3_5, inch_4_0, inch_4_7, inch_5_5, inch_5_8, inch_6_1, inch_6_5);
}

//使用加粗体
UIFont * VLSystemBoldFont(CGFloat inch_3_5,
                          CGFloat inch_4_0,
                          CGFloat inch_4_7,
                          CGFloat inch_5_5,
                          CGFloat inch_5_8,
                          CGFloat inch_6_1,
                          CGFloat inch_6_5) {
    return AppSystemFont(YES, inch_3_5, inch_4_0, inch_4_7, inch_5_5, inch_5_8, inch_6_1, inch_6_5);
}

//使用中粗体
UIFont * VLSystemMediumFont(CGFloat inch_3_5,
                          CGFloat inch_4_0,
                          CGFloat inch_4_7,
                          CGFloat inch_5_5,
                          CGFloat inch_5_8,
                          CGFloat inch_6_1,
                          CGFloat inch_6_5) {
    if (VLIS_35INCH_SCREEN) {
        
        return SystemMediumFontSize(inch_3_5);
    }
    if (VLIS_40INCH_SCREEN) {
        
        return SystemMediumFontSize(inch_4_0);
    }
    if (VLIS_47INCH_SCREEN) {
        
        return SystemMediumFontSize(inch_4_7);
    }
    if (VLIS_55INCH_SCREEN) {
        
        return SystemMediumFontSize(inch_5_5);
    }
    if (VLIS_58INCH_SCREEN) {
        
        return SystemMediumFontSize(inch_5_8);
    }
    if (VLIS_61INCH_SCREEN) {
        
        return SystemMediumFontSize(inch_6_1);
    }
    if (VLIS_65INCH_SCREEN) {
        
        return SystemMediumFontSize(inch_6_5);
    }
    return SystemMediumFontSize(inch_4_7);
}


@end
