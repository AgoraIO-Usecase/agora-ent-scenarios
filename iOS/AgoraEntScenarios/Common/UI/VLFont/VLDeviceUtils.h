//
//  VLDeviceUtils.h
//  VoiceOnLine
//

#import <Foundation/Foundation.h>
@import CoreGraphics;

NS_ASSUME_NONNULL_BEGIN

@interface VLDeviceUtils : NSObject

+ (nonnull NSString *)deviceModel;

+ (BOOL)isIPad;
+ (BOOL)isIPod;
+ (BOOL)isIPone;
+ (BOOL)isSimulator;

// Checking device types.
+ (BOOL)is65InchScreen;
+ (BOOL)is61InchScreen;
+ (BOOL)is58InchScreen;
+ (BOOL)is55InchScreen;
+ (BOOL)is47InchScreen;
+ (BOOL)is40InchScreen;
+ (BOOL)is35InchScreen;

+ (CGSize)screenSizeFor65Inch;
+ (CGSize)screenSizeFor61Inch;
+ (CGSize)screenSizeFor58Inch;
+ (CGSize)screenSizeFor55Inch;
+ (CGSize)screenSizeFor47Inch;
+ (CGSize)screenSizeFor40Inch;
+ (CGSize)screenSizeFor35Inch;

@end

NS_ASSUME_NONNULL_END
