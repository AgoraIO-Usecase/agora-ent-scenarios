//
//  VLMacroDefine.h
//  VoiceOnLine
//

#ifndef VLMacroDefine_h
#define VLMacroDefine_h

#define __MAIN_SCREEN_WIDTH__       MIN([UIScreen mainScreen].bounds.size.width, [UIScreen mainScreen].bounds.size.height)

#define  VLSCALE_W                 (__MAIN_SCREEN_WIDTH__ / 375.0)

#define  VLREALVALUE_WIDTH(w)      (VLSCALE_W * w)

#define VL_IS_IPHONE_X ((IOS_VERSION >= 11.f) && IS_IPHONE && (MIN([UIScreen mainScreen].bounds.size.width, [UIScreen mainScreen].bounds.size.height) >= 375 && MAX([UIScreen mainScreen].bounds.size.width, [UIScreen mainScreen].bounds.size.height) >= 812))

#define VLTABBAR_HEIGHT  (VL_IS_IPHONE_X ? 89.0 : 55.0)

#define SafeAreaTopHeight (VL_IS_IPHONE_X ? 88 : 64)
#define SafeAreaBottomHeight (VL_IS_IPHONE_X ? 34 : 0)
#define SafeAreaStatusHeight (VL_IS_IPHONE_X ? 24 : 0)

#define IPHONE_X  [[UIApplication sharedApplication] delegate].window.safeAreaInsets.bottom > 0.0
#define kStatusBarHeight    (IPHONE_X ? 44.f : 20.f)
#define kTopNavHeight    (kStatusBarHeight + 44.f)
#define kSafeAreaBottomHeight  (IPHONE_X ? 34.f : 0.f)
#define kBottomTabBarHeight    (kSafeAreaBottomHeight + 49.f)
        
#define VL(weakSelf)  __weak __typeof(&*self)weakSelf = self

#define kWeakSelf(object) __weak typeof(object) weak##object = object;
#define kStrongSelf(object) __strong typeof(weak##object) object = weak##object;

#ifdef DEBUG
#define VLLog NSLog
#else
#define VLLog
#endif

#endif /* VLMacroDefine_h */
