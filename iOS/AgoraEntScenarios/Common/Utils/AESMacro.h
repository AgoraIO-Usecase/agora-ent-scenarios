//
//  KTVMacro.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/8.
//

#ifndef AESMacro_h
#define AESMacro_h

@import YYCategories;
#import "QMUICommonDefines.h"
#import "VLDeviceUtils.h"
#import "QMUIHelper.h"


// 基础颜色
#define UIColorClear                [UIColor clearColor]
#define UIColorWhite                [UIColor whiteColor]
#define UIColorBlack                [UIColor blackColor]
#define UIColorGray                 [UIColor grayColor]
#define UIColorGrayDarken           [UIColor grayDarkenColor]
#define UIColorGrayLighten          [UIColor grayLightenColor]
#define UIColorRed                  [UIColor redColor]
#define UIColorGreen                [UIColor greenColor]
#define UIColorBlue                 [UIColor blueColor]
#define UIColorYellow               [UIColor yellowColor]
#define VLUIFontMake(s)             ([UIFont systemFontOfSize:s])


/// toolBar相关frame
//#define ToolBarHeight (IS_IPAD ? (IS_NOTCHED_SCREEN ? 70 : (IOS_VERSION >= 12.0 ? 50 : 44)) : (IS_LANDSCAPE ? PreferredValueForVisualDevice(44, 32) : 44) + SafeAreaInsetsConstantForDeviceWithNotch.bottom)

/// tabBar相关frame
//#define TabBarHeight (IS_IPAD ? (IS_NOTCHED_SCREEN ? 65 : (IOS_VERSION >= 12.0 ? 50 : 49)) : (IS_LANDSCAPE ? PreferredValueForVisualDevice(49, 32) : 49) + SafeAreaInsetsConstantForDeviceWithNotch.bottom)

#define NullToString(s) (s == NULL ? @"": s)

#define KTVLocalizedString(s) ([s toSceneLocalizationWith:@"KtvResource"])
#define DHCLocalizedString(s) ([s toSceneLocalizationWith:@"DHCResource"])
#define SRLocalizedString(s) ([s toSceneLocalizationWith:@"SRResource"])
#define SRLogInfo(format, ...)  ([SRLog infoWithText:[NSString stringWithFormat:(format), ##__VA_ARGS__] tag: @"SingRelay"])
#define SRLogError(format, ...)  ([SRLog errorWithText:[NSString stringWithFormat:(format), ##__VA_ARGS__] tag: @"SingRelay"])
#define KTVLogInfo(format, ...)  ([KTVLog infoWithText:[NSString stringWithFormat:(format), ##__VA_ARGS__] tag: @"UI"])
#define KTVLogError(format, ...)  ([KTVLog errorWithText:[NSString stringWithFormat:(format), ##__VA_ARGS__] tag: @"UI"])
#define KTVLogWarn(format, ...)  ([KTVLog warningWithText:[NSString stringWithFormat:(format), ##__VA_ARGS__] tag: @"UI"])
#define KTVLogDebug(format, ...)  ([KTVLog debugWithText:[NSString stringWithFormat:(format), ##__VA_ARGS__] tag: @"UI"])



#define UIColorMakeWithHex(s) [UIColor colorWithHexString:s]

#endif /* KTVMacro_h */
