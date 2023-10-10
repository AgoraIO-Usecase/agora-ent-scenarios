//
//  SBGDebugManager.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/17.
//

#import "SBGDebugManager.h"
#import "SBGDebugViewController.h"
#import "SBGDebugInfo.h"
#import "SBGMacro.h"
@import AgoraRtcKit;

@implementation SBGDebugManager
//[AgoraRtcEngineKit sharedEngineWithAppId:[AppContext.shared appId] delegate:self]
+ (UIGestureRecognizer*)createStartGesture {
    [self reLoadParamAll];
    
    UITapGestureRecognizer* gesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(startDebugAction:)];
    gesture.numberOfTapsRequired = 3;
    return gesture;
}

+ (void)startDebugAction:(UIGestureRecognizer*)ges {
    SBGDebugViewController* vc = [SBGDebugViewController new];
    UINavigationController* nv = [[UINavigationController alloc] initWithRootViewController:vc];
    UIResponder* responder = ges.view;
    while (responder.nextResponder != nil) {
        responder = responder.nextResponder;
        if ([responder isKindOfClass:[UIViewController class]]) {
            break;
        }
    }
    
    UIViewController* parentVC = (UIViewController*)responder;
    nv.view.frame = parentVC.view.bounds;
    [parentVC addChildViewController:nv];
    [parentVC.view addSubview:nv.view];
}

+ (void)reLoadParamAll {
    AgoraRtcEngineKit* engine = [AgoraRtcEngineKit performSelector:NSSelectorFromString(@"getAgoraRtcEngineKit")];
    
    NSArray* dataArray = [SBGDebugInfo debugDataArray];
    [dataArray enumerateObjectsUsingBlock:^(NSDictionary* obj, NSUInteger idx, BOOL * stop) {
        NSString* key = obj[sTitleKey];
        BOOL status = [SBGDebugInfo getSelectedStatusForKey:key];
        NSArray* paramArray = status ? obj[sSelectedParamKey] : obj[sUnselectedParamKey];
        [paramArray enumerateObjectsUsingBlock:^(NSString* obj, NSUInteger idx, BOOL * _Nonnull stop) {
            SBGLogInfo(@"reLoadParamAll: %@, %@", key, obj);
            [engine setParameters:obj];
        }];
    }];
}

@end
