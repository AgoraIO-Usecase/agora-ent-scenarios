//
//  VLToast.h
//  VoiceOnLine
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface VLToast : NSObject

+ (void)toast:(NSString *)msg;
+ (void)toast:(NSString *)msg duration:(float)duration;

@end

NS_ASSUME_NONNULL_END
