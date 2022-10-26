//
//  VLCache.m
//  VoiceOnLine
//

#import "VLCache.h"
#import <YYCache/YYCache.h>
static VLCache *VLCache_App = nil;
static VLCache *VLCache_User = nil;

@interface VLCache ()
@property (nonatomic, strong) YYCache *yy;
@end

@implementation VLCache

+ (VLCache *)system {
    if (VLCache_App == nil) {
        VLCache_App = [[VLCache alloc] init];
        VLCache_App.yy = [[YYCache alloc] initWithName:@"vl.cache.system"];
    }
    return VLCache_App;
}

+ (VLCache *)user {
    return VLCache_User;
}

+ (void)setupUserIdentity:(NSString *)identity {
    if (VLCache_User != nil && [VLCache_User.yy.name isEqualToString:identity]) {
        return;
    }
    VLCache_User = [[VLCache alloc] init];
    VLCache_User.yy = [[YYCache alloc] initWithName:identity];
}

+ (void)removeUser {
    VLCache_User.yy = nil;
    VLCache_User = nil;
}

- (BOOL)containsObjectForKey:(NSString *)key {
    return [self.yy containsObjectForKey:key];
}

- (void)containsObjectForKey:(NSString *)key withBlock:(nullable void(^)(NSString *key, BOOL contains))block {
    [self.yy containsObjectForKey:key withBlock:block];
}

- (nullable id<NSCoding>)objectForKey:(NSString *)key {
    return [self.yy objectForKey:key];
}

- (void)objectForKey:(NSString *)key withBlock:(nullable void(^)(NSString *key, id<NSCoding> object))block {
    [self.yy objectForKey:key withBlock:block];
}

- (void)setObject:(nullable id<NSCoding>)object forKey:(NSString *)key {
    [self.yy setObject:object forKey:key];
}

- (void)setObject:(nullable id<NSCoding>)object forKey:(NSString *)key withBlock:(nullable void(^)(void))block {
    [self.yy setObject:object forKey:key withBlock:block];
}

- (void)removeObjectForKey:(NSString *)key {
    [self.yy removeObjectForKey:key];
}

- (void)removeObjectForKey:(NSString *)key withBlock:(nullable void(^)(NSString *key))block {
    [self.yy removeObjectForKey:key withBlock:block];
}

- (void)removeAllObjects {
    [self.yy removeAllObjects];
}

- (void)removeAllObjectsWithBlock:(void(^)(void))block {
    [self.yy removeAllObjectsWithBlock:block];
}

- (void)removeAllObjectsWithProgressBlock:(nullable void(^)(int removedCount, int totalCount))progress
                                 endBlock:(nullable void(^)(BOOL error))end {
    [self.yy removeAllObjectsWithProgressBlock:progress endBlock:end];
}

@end
