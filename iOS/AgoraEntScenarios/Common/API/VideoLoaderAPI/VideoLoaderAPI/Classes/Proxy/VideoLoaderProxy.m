//
//  VideoLoaderProxy.m
//  VideoLoaderAPI
//
//  Created by wushengtao on 2023/7/27.
//

#import <objc/runtime.h>
#import "VideoLoaderProxy.h"

@interface VideoLoaderProxy ()
@property(nonatomic, strong) NSHashTable* listeners;

@end

@implementation VideoLoaderProxy

- (instancetype)init {
    if (self = [super init]) {
        self.listeners = [NSHashTable weakObjectsHashTable];
    }
    
    return self;
}

- (void)addListener:(id <NSObject>)listener {
    if ([self.listeners containsObject:listener]) {
        return;
    }
    
    [self.listeners addObject:listener];
}

- (void)removeListener:(id <NSObject>)listener {
    [self.listeners removeObject:listener];
}

- (void)removeAllListener {
    [self.listeners removeAllObjects];
}

- (BOOL)respondsToSelector:(SEL)aSelector {
    return [self methodSignatureForSelector:aSelector] != nil;
}

- (void)forwardInvocation:(NSInvocation *)invocation {
    [invocation retainArguments];
    
    for (id listener in self.listeners.objectEnumerator) {
        if ([listener respondsToSelector:invocation.selector]) {
            [invocation invokeWithTarget:listener];
        }
    }
}

@end
