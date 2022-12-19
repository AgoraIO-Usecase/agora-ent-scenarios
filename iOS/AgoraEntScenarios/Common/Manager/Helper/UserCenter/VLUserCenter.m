//
//  VLUserCenter.m
//  VoiceOnLine
//

#import "VLUserCenter.h"
#import "VLCache.h"

@interface VLUserCenter()

@property (nonatomic, strong) VLLoginModel *loginModel;

@end

static NSString *kLocalLoginKey = @"kLocalLoginKey";

@implementation VLUserCenter

+ (VLUserCenter *)center{
    static VLUserCenter *instancel = nil;
    static dispatch_once_t oneToken;
    dispatch_once(&oneToken, ^{
        instancel = [[VLUserCenter alloc]init];
    });
    return instancel;
}

- (BOOL)isLogin {
    if (!_loginModel) {
        _loginModel = (VLLoginModel *)[VLCache.system objectForKey:kLocalLoginKey];
    }
    return _loginModel ? YES : NO;
}

- (void)storeUserInfo:(VLLoginModel *)user {
    _loginModel = user;
    [VLCache.system setObject:_loginModel forKey:kLocalLoginKey];
}

- (void)logout {
    [self cleanUserInfo];
}

- (void)cleanUserInfo {
    _loginModel = nil;
    [VLCache.system removeObjectForKey:kLocalLoginKey];
}

+ (VLLoginModel *)user {
    return [VLUserCenter center].loginModel;
}

+ (void)clearUserRoomInfo {
    VLUserCenter.user.ifMaster = NO;
    [VLCache.system setObject:VLUserCenter.user forKey:kLocalLoginKey];
}

@end
