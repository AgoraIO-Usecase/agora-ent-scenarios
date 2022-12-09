//
//  KTVMacro.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/8.
//

#ifndef KTVMacro_h
#define KTVMacro_h
#import "AgoraEntScenarios-Swift.h"

#define KTVLocalizedString(s) ([s toSceneLocalization])

#define KTVLogInfo(format, ...)  ([KTVLog infoWithText:[NSString stringWithFormat:(format), ##__VA_ARGS__] tag: nil])

#endif /* KTVMacro_h */
