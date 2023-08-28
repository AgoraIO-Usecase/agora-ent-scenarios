//
//  VLUserCenter.m
//  VoiceOnLine
//

#import "VLUserCenter.h"

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

+ (VLUserCenter* )shared {
    return [self center];
}

- (BOOL)isLogin {
    if (!_loginModel) {
        NSString* ret = [[NSUserDefaults standardUserDefaults] objectForKey:kLocalLoginKey];
        _loginModel = [VLLoginModel yy_modelWithJSON:ret];
    }
    return _loginModel ? YES : NO;
}

- (void)storeUserInfo:(VLLoginModel *)user {
    _loginModel = user;
    NSString* ret = [_loginModel yy_modelToJSONString];
    [[NSUserDefaults standardUserDefaults] setObject:ret forKey:kLocalLoginKey];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

- (void)logout {
    [self cleanUserInfo];
}

- (void)cleanUserInfo {
    _loginModel = nil;
    [[NSUserDefaults standardUserDefaults] removeObjectForKey:kLocalLoginKey];
}

+ (VLLoginModel *)user {
    return [VLUserCenter center].loginModel;
}

+ (void)clearUserRoomInfo {
    VLUserCenter.user.ifMaster = NO;
    [VLUserCenter.center storeUserInfo:VLUserCenter.user];
}

@end
