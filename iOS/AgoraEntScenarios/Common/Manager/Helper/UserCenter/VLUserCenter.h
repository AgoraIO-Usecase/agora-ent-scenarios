//
//  VLUserCenter.h
//  VoiceOnLine
//

#import <Foundation/Foundation.h>
#import "VLLoginModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface VLUserCenter : NSObject

+ (instancetype)center;
+ (instancetype)shared;

@property (nonatomic, strong, class, readonly) VLLoginModel *user;

- (BOOL)isLogin;
- (void)storeUserInfo:(VLLoginModel *)user;
- (void)logout;

+ (void)clearUserRoomInfo;


@end

NS_ASSUME_NONNULL_END
