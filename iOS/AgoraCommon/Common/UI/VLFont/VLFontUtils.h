//
//  VLFontUtils.h
//  VoiceOnLine
//

#import <Foundation/Foundation.h>
#import "VLDeviceUtils.h"
@import UIKit;

NS_ASSUME_NONNULL_BEGIN

#define VLIS_35INCH_SCREEN [VLDeviceUtils is35InchScreen]
#define VLIS_40INCH_SCREEN [VLDeviceUtils is40InchScreen]
#define VLIS_47INCH_SCREEN [VLDeviceUtils is47InchScreen]
#define VLIS_55INCH_SCREEN [VLDeviceUtils is55InchScreen]
#define VLIS_58INCH_SCREEN [VLDeviceUtils is58InchScreen]
#define VLIS_61INCH_SCREEN [VLDeviceUtils is61InchScreen]
#define VLIS_65INCH_SCREEN [VLDeviceUtils is65InchScreen]

FOUNDATION_EXPORT NSUInteger const VLFontTag;

FOUNDATION_EXTERN UIFont * VLSystemRegularFont(CGFloat inch_3_5,
                             CGFloat inch_4_0,
                             CGFloat inch_4_7,
                             CGFloat inch_5_5,
                             CGFloat inch_5_8,
                             CGFloat inch_6_1,
                             CGFloat inch_6_5);

FOUNDATION_EXTERN UIFont * VLSystemBoldFont(CGFloat inch_3_5,
                          CGFloat inch_4_0,
                          CGFloat inch_4_7,
                          CGFloat inch_5_5,
                          CGFloat inch_5_8,
                          CGFloat inch_6_1,
                          CGFloat inch_6_5);

FOUNDATION_EXTERN UIFont * VLSystemMediumFont(CGFloat inch_3_5,
                            CGFloat inch_4_0,
                            CGFloat inch_4_7,
                            CGFloat inch_5_5,
                            CGFloat inch_5_8,
                            CGFloat inch_6_1,
                            CGFloat inch_6_5);

static inline UIFont * VLUIFontMake(CGFloat font) {
    
    return VLSystemRegularFont((font - 2), (font - 2), font, (font + 1), font, font, (font + 1));
}

static inline UIFont * VLUIFontBoldMake(CGFloat font) {
    
    return VLSystemBoldFont((font - 2), (font - 2), font, (font + 1), font, font, (font + 1));
}

static inline UIFont * VLUIFontMediumMake(CGFloat font) {
    
    return VLSystemMediumFont((font - 2), (font - 2), font, (font + 1), font, font, (font + 1));
}

@interface VLFontUtils : NSObject

@end

NS_ASSUME_NONNULL_END
