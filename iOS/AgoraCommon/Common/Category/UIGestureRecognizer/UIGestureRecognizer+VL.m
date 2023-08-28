//
//  UIGestureRecognizer+VL.m
//  VoiceOnLine
//

#import "UIGestureRecognizer+VL.h"
#import <objc/runtime.h>

static const void *VLGestureRecognizerBlockKey = &VLGestureRecognizerBlockKey;
static const void *VLGestureRecognizerDelayKey = &VLGestureRecognizerDelayKey;
static const void *VLGestureRecognizerShouldHandleActionKey = &VLGestureRecognizerShouldHandleActionKey;

@interface UIGestureRecognizer (Private)

@property (nonatomic, setter = vl_setShouldHandleAction:) BOOL vl_shouldHandleAction;

- (void)vl_handleAction:(UIGestureRecognizer *)recognizer;

@end

@implementation UIGestureRecognizer (VL)

+ (id)vl_recognizerWithHandler:(void (^)(UIGestureRecognizer *sender, UIGestureRecognizerState state, CGPoint location))block delay:(NSTimeInterval)delay
{
    return [[[self class] alloc] vl_initWithHandler:block delay:delay];
}

- (id)vl_initWithHandler:(void (^)(UIGestureRecognizer *sender, UIGestureRecognizerState state, CGPoint location))block delay:(NSTimeInterval)delay
{
    self = [self initWithTarget:self action:@selector(vl_handleAction:)];
    if (!self) return nil;

    self.vl_handler = block;
    self.vl_handlerDelay = delay;

    return self;
}

+ (id)vl_recognizerWithHandler:(void (^)(UIGestureRecognizer *sender, UIGestureRecognizerState state, CGPoint location))block
{
    return [self vl_recognizerWithHandler:block delay:0.0];
}

- (id)vl_initWithHandler:(void (^)(UIGestureRecognizer *sender, UIGestureRecognizerState state, CGPoint location))block
{
    return (self = [self vl_initWithHandler:block delay:0.0]);
}

- (void)vl_handleAction:(UIGestureRecognizer *)recognizer
{
    void (^handler)(UIGestureRecognizer *sender, UIGestureRecognizerState state, CGPoint location) = recognizer.vl_handler;
    if (!handler) return;
    
    NSTimeInterval delay = self.vl_handlerDelay;
    CGPoint location = [self locationInView:self.view];
    void (^block)(void) = ^{
        if (!self.vl_shouldHandleAction) return;
        handler(self, self.state, location);
    };

    self.vl_shouldHandleAction = YES;

    if (!delay) {
        block();
        return;
    }

    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delay * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), block);
}

- (void)vl_setHandler:(void (^)(UIGestureRecognizer *sender, UIGestureRecognizerState state, CGPoint location))handler
{
    objc_setAssociatedObject(self, VLGestureRecognizerBlockKey, handler, OBJC_ASSOCIATION_COPY_NONATOMIC);
}

- (void (^)(UIGestureRecognizer *sender, UIGestureRecognizerState state, CGPoint location))vl_handler
{
    return objc_getAssociatedObject(self, VLGestureRecognizerBlockKey);
}

- (void)vl_setHandlerDelay:(NSTimeInterval)delay
{
    NSNumber *delayValue = delay ? @(delay) : nil;
    objc_setAssociatedObject(self, VLGestureRecognizerDelayKey, delayValue, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (NSTimeInterval)vl_handlerDelay
{
    return [objc_getAssociatedObject(self, VLGestureRecognizerDelayKey) doubleValue];
}

- (void)vl_setShouldHandleAction:(BOOL)flag
{
    objc_setAssociatedObject(self, VLGestureRecognizerShouldHandleActionKey, @(flag), OBJC_ASSOCIATION_COPY_NONATOMIC);
}

- (BOOL)vl_shouldHandleAction
{
    return [objc_getAssociatedObject(self, VLGestureRecognizerShouldHandleActionKey) boolValue];
}

- (void)vl_cancel
{
    self.vl_shouldHandleAction = NO;
}

@end
