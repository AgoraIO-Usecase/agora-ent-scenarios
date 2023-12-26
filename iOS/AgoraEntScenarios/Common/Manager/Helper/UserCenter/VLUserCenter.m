//
//  VLUserCenter.m
//  VoiceOnLine
//

#import "VLUserCenter.h"
#import <Security/Security.h>
@interface VLUserCenter()

@property (nonatomic, strong) VLLoginModel *loginModel;

@end

static NSString *const kKeychainServiceName = @"com.agora.app";
static NSString *const kKeychainLoginModelKey = @"loginModel";

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
        NSDictionary *query = @{
            (__bridge id)kSecClass: (__bridge id)kSecClassGenericPassword,
            (__bridge id)kSecAttrService: kKeychainServiceName,
            (__bridge id)kSecAttrAccount: kKeychainLoginModelKey,
            (__bridge id)kSecReturnData: @(YES)
        };
        
        CFTypeRef result = NULL;
        OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)query, &result);
        
        if (status == noErr && result != NULL) {
            NSData *data = (__bridge_transfer NSData *)result;
            _loginModel = [VLLoginModel yy_modelWithJSON:data];
        }
    }
    
    return _loginModel ? YES : NO;
}

- (void)storeUserInfo:(VLLoginModel *)user {
    _loginModel = user;
    NSData *data = [_loginModel yy_modelToJSONData];
    
    NSDictionary *query = @{
        (__bridge id)kSecClass: (__bridge id)kSecClassGenericPassword,
        (__bridge id)kSecAttrService: kKeychainServiceName,
        (__bridge id)kSecAttrAccount: kKeychainLoginModelKey,
    };
    
    OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)query, NULL);
    
    if (status == noErr) {
        NSDictionary *attributesToUpdate = @{
            (__bridge id)kSecValueData: data
        };
        
        status = SecItemUpdate((__bridge CFDictionaryRef)query, (__bridge CFDictionaryRef)attributesToUpdate);
    } else if (status == errSecItemNotFound) {
        NSMutableDictionary *attributes = [query mutableCopy];
        [attributes setObject:data forKey:(__bridge id)kSecValueData];
        
        status = SecItemAdd((__bridge CFDictionaryRef)attributes, NULL);
    }
}

- (void)cleanUserInfo {
    NSDictionary *query = @{
        (__bridge id)kSecClass: (__bridge id)kSecClassGenericPassword,
        (__bridge id)kSecAttrService: kKeychainServiceName,
        (__bridge id)kSecAttrAccount: kKeychainLoginModelKey
    };
    
    OSStatus status = SecItemDelete((__bridge CFDictionaryRef)query);
    
    if (status == noErr) {
        _loginModel = nil;
    }
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
