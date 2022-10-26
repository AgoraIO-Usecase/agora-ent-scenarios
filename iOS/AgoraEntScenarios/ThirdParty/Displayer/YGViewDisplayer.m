//
//  YGViewDisplayer.m
//  YGKitSwift
//
//  Created by edz on 2021/6/28.
//

#import "YGViewDisplayer.h"
#import "AgoraEntScenarios-Swift.h"
//#import <YGKit/YGKit-Swift.h>

//#ifdef YG_MODULE_DEMO
//#import <YGKit/YGKit-Swift.h>
//#else
//#import "YGKit-Swift.h"
//#endif

@implementation YGViewDisplayOptions
- (void)setupSelfWithSwiftOptions:(_swiftYGViewDisplayOptions *)options {
    self.identity = options.identity;
    self.duration = options.duration;
    self.screenBackgroundColor = options.screenBackgroundColor;
    self.backgroundColor = options.backgroundColor;
    self.cornerRidus = options.cornerRidus;
    self.positionConstraints = @(options.positionConstraints).intValue;
    self.interaction = @(options.interaction).intValue;
    self.screenInteraction = @(options.screenInteraction).intValue;
    self.safeArea = @(options.safeArea).intValue;
    self.statusBar = @(options.statusBar).intValue;
    self.windowLevel = @(options.windowLevel).intValue;
    self.maxSize = options.maxSize;
    self.priority = @(options.priority).intValue;
}

- (void)setupSwiftOptionsWithSelf:(_swiftYGViewDisplayOptions *)options {
    options.identity = self.identity;
    options.duration = self.duration;
    options.screenBackgroundColor = self.screenBackgroundColor;
    options.backgroundColor = self.backgroundColor;
    options.cornerRidus = self.cornerRidus;
    options.positionConstraints = @(self.positionConstraints).intValue;
    options.interaction = @(self.interaction).intValue;
    options.screenInteraction = @(self.screenInteraction).intValue;
    options.safeArea = @(self.safeArea).intValue;
    options.statusBar = @(self.statusBar).intValue;
    options.windowLevel = @(self.windowLevel).intValue;
    options.maxSize = self.maxSize;
    options.priority = @(self.priority).intValue;
}
@end

@implementation YGViewDisplayer

+ (void)useWindowScene:(BOOL)value {
    [_swiftYGViewDisplayer useWindowScene:value];
}

+ (void)display:(UIView *)view setupBlock:(void (^)(YGViewDisplayOptions * _Nonnull))setupBlock {
    [_swiftYGViewDisplayer display:view setupBlock:^(_swiftYGViewDisplayOptions * _Nonnull swiftOptions) {
        if (setupBlock == nil) {
            return;
        }
        YGViewDisplayOptions *options = [[YGViewDisplayOptions alloc] init];
        [options setupSelfWithSwiftOptions:swiftOptions];
        setupBlock(options);
        [options setupSwiftOptionsWithSelf:swiftOptions];
    }];
}

/// 底部弹出到中间
+ (void)popupCenter:(UIView * _Nonnull)view setupBlock:(void (^ _Nullable)(YGViewDisplayOptions * _Nonnull))setupBlock {
    
    [_swiftYGViewDisplayer popupCenter:view setupBlock:^(_swiftYGViewDisplayOptions * _Nonnull swiftOptions) {
        if (setupBlock == nil) {
            return;
        }
        YGViewDisplayOptions *options = [[YGViewDisplayOptions alloc] init];
        [options setupSelfWithSwiftOptions:swiftOptions];
        setupBlock(options);
        [options setupSwiftOptionsWithSelf:swiftOptions];
    }];
}

/// 底部弹出固定在底部，默认宽度充满屏幕
+ (void)popupBottom:(UIView * _Nonnull)view setupBlock:(void (^ _Nullable)(YGViewDisplayOptions * _Nonnull))setupBlock {
    [_swiftYGViewDisplayer popupBottom:view setupBlock:^(_swiftYGViewDisplayOptions * _Nonnull swiftOptions) {
        if (setupBlock == nil) {
            return;
        }
        YGViewDisplayOptions *options = [[YGViewDisplayOptions alloc] init];
        [options setupSelfWithSwiftOptions:swiftOptions];
        setupBlock(options);
        [options setupSwiftOptionsWithSelf:swiftOptions];
    }];
}
/// 淡出在中间
+ (void)fadeCenter:(UIView * _Nonnull)view setupBlock:(void (^ _Nullable)(YGViewDisplayOptions * _Nonnull))setupBlock {
    [_swiftYGViewDisplayer fadeCenter:view setupBlock:^(_swiftYGViewDisplayOptions * _Nonnull swiftOptions) {
        if (setupBlock == nil) {
            return;
        }
        YGViewDisplayOptions *options = [[YGViewDisplayOptions alloc] init];
        [options setupSelfWithSwiftOptions:swiftOptions];
        setupBlock(options);
        [options setupSwiftOptionsWithSelf:swiftOptions];
    }];
}
/// dismiss某一个view
+ (void)dismiss:(UIView * _Nonnull)view completionHandler:(void (^ _Nullable)(void))completionHandler {
    [_swiftYGViewDisplayer dismiss:view completionHandler:completionHandler];
}

+ (void)dismissWithIdentity:(NSString * _Nonnull)identity completionHandler:(void (^)(void))completionHandler {
    [_swiftYGViewDisplayer dismissWithIdentity:identity completionHandler:completionHandler];
}

/// dismiss当前
+ (void)dismissDisplayed:(void (^ _Nullable)(void))completionHandler {
    [_swiftYGViewDisplayer dismissDisplayed:completionHandler];
}
/// 移出所有，当前显示的和队列中的
+ (void)dismissAll:(void (^ _Nullable)(void))completionHandler {
    [_swiftYGViewDisplayer dismissAll:completionHandler];
}

@end
