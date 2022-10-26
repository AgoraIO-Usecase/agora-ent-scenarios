//
//  VLRequestRoute.h
//  VoiceOnLine
//

#import <Foundation/Foundation.h>
#import "VLNetworkConfig.h"

NS_ASSUME_NONNULL_BEGIN

@interface VLRequestRoute : NSObject

+ (NSString *)doRoute:(NSString *)route andMethod:(NSString *)method;

+ (NSString *)getToken;

@end

NS_ASSUME_NONNULL_END
