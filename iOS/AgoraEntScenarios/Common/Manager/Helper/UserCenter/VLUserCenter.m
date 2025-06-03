//
//  VLUserCenter.m
//  VoiceOnLine
//

#import "VLUserCenter.h"
#import <Security/Security.h>
@interface VLUserCenter()

@property (nonatomic, strong) VLLoginModel *loginModel;

@end

static NSString *const kUserDefaultsLoginModelKey = @"loginModel";

@implementation VLUserCenter

+ (VLUserCenter *)center{
    static VLUserCenter *instancel = nil;
    static dispatch_once_t oneToken;
    dispatch_once(&oneToken, ^{
        instancel = [[VLUserCenter alloc]init];
    });
    return instancel;
}

+ (VLUserCenter* )shared {
    return [self center];
}

- (BOOL)isLogin {
    if (!_loginModel) {
        NSData *data = [[NSUserDefaults standardUserDefaults] objectForKey:kUserDefaultsLoginModelKey];
        if (data) {
            _loginModel = [VLLoginModel yy_modelWithJSON:data];
        }
    }
    return _loginModel ? YES : NO;
}

- (void)storeUserInfo:(VLLoginModel *)user {
    _loginModel = user;
    NSData *data = [_loginModel yy_modelToJSONData];
    [[NSUserDefaults standardUserDefaults] setObject:data forKey:kUserDefaultsLoginModelKey];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

- (void)cleanUserInfo {
    [[NSUserDefaults standardUserDefaults] removeObjectForKey:kUserDefaultsLoginModelKey];
    [[NSUserDefaults standardUserDefaults] synchronize];
    _loginModel = nil;
}

- (void)logout {
    [self cleanUserInfo];
}

+ (VLLoginModel *)user {
    return [VLUserCenter center].loginModel;
}

+ (void)clearUserRoomInfo {
    VLUserCenter.user.ifMaster = NO;
    [VLUserCenter.center storeUserInfo:VLUserCenter.user];
}

@end
