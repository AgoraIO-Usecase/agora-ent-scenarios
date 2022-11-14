//
//  Xcode14.m
//  AgoraEntScenarios
//
//  Created by 朱继超 on 2022/11/14.
//

#import "Xcode14.h"
#import <objc/runtime.h>

#if DEBUG


@implementation Xcode14

+ (void)load
{
    Class cls = NSClassFromString(@"_UINavigationBarContentViewLayout");
    SEL selector = @selector(valueForUndefinedKey:);
    Method impMethod = class_getInstanceMethod([self class], selector);

    if (impMethod) {
        class_addMethod(cls, selector, method_getImplementation(impMethod), method_getTypeEncoding(impMethod));
    }
}

- (id)valueForUndefinedKey:(NSString *)key
{
    return nil;
}

@end
#endif
