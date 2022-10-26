//
//  VLCache.h
//  VoiceOnLine
//

#import <Foundation/Foundation.h>
#define VLCacheSystem VLCache.system
#define VLCacheUser VLCache.user
NS_ASSUME_NONNULL_BEGIN

@interface VLCache : NSObject

/// app cache
@property (nonatomic, strong, class, readonly) VLCache *system;

/// 用户 cache
@property (nonatomic, strong, class, nullable, readonly) VLCache *user;

/// 设置用户的标识符
/// @param identity 标识符
+ (void)setupUserIdentity:(NSString *)identity;

/// 删除用户
+ (void)removeUser;

/// 判断是否存在
/// @param key key
- (BOOL)containsObjectForKey:(NSString *)key;

/// 判断是否存在、子线程
/// @param key key
/// @param block 子线程返回结果
- (void)containsObjectForKey:(NSString *)key withBlock:(nullable void(^)(NSString *key, BOOL contains))block;

/// 读取
/// @param key key
- (nullable id<NSCoding>)objectForKey:(NSString *)key;

/// 读取、子线程
/// @param key key
/// @param block 子线程回调
- (void)objectForKey:(NSString *)key withBlock:(nullable void(^)(NSString *key, id<NSCoding> object))block;

/// 存储
/// @param object object
/// @param key key
- (void)setObject:(nullable id<NSCoding>)object forKey:(NSString *)key;

/// 存储、子线程
/// @param object object
/// @param key key
/// @param block 子线程回调
- (void)setObject:(nullable id<NSCoding>)object forKey:(NSString *)key withBlock:(nullable void(^)(void))block;

/// 删除
/// @param key key
- (void)removeObjectForKey:(NSString *)key;

/// 删除、子线程
/// @param key key
/// @param block 子线程回调
- (void)removeObjectForKey:(NSString *)key withBlock:(nullable void(^)(NSString *key))block;

/// 清除
- (void)removeAllObjects;

/// 清除、子线程
/// @param block  子线程回调
- (void)removeAllObjectsWithBlock:(void(^)(void))block;

/// 清除、子线程
/// @param progress 子线程progress
/// @param end 子线程回调
- (void)removeAllObjectsWithProgressBlock:(nullable void(^)(int removedCount, int totalCount))progress
                                 endBlock:(nullable void(^)(BOOL error))end;
@end

NS_ASSUME_NONNULL_END
