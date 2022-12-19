//
//  KTVMacro.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/8.
//

#ifndef KTVMacro_h
#define KTVMacro_h

@import YYCategories;
#import "AgoraEntScenarios-Swift.h"
#import "QMUICommonDefines.h"
#import "QMUIConfiguration.h"
#import "QMUIConfigurationMacros.h"
#import "VLDeviceUtils.h"
#import "QMUIHelper.h"

/// toolBar相关frame
#define ToolBarHeight (IS_IPAD ? (IS_NOTCHED_SCREEN ? 70 : (IOS_VERSION >= 12.0 ? 50 : 44)) : (IS_LANDSCAPE ? PreferredValueForVisualDevice(44, 32) : 44) + SafeAreaInsetsConstantForDeviceWithNotch.bottom)

/// tabBar相关frame
#define TabBarHeight (IS_IPAD ? (IS_NOTCHED_SCREEN ? 65 : (IOS_VERSION >= 12.0 ? 50 : 49)) : (IS_LANDSCAPE ? PreferredValueForVisualDevice(49, 32) : 49) + SafeAreaInsetsConstantForDeviceWithNotch.bottom)

#define KTVLocalizedString(s) ([s toSceneLocalization])

#define KTVLogInfo(format, ...)  ([KTVLog infoWithText:[NSString stringWithFormat:(format), ##__VA_ARGS__] tag: nil])



#define UIColorMakeWithHex(s) [UIColor colorWithHexString:s]

#endif /* KTVMacro_h */
