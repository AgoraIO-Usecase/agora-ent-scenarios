//
//  SRDebugManager.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/17.
//

#import "SRDebugManager.h"
#import "SRDebugViewController.h"
#import "SRDebugInfo.h"
#import "AESMacro.h"
@import AgoraRtcKit;

@implementation SRDebugManager
//[AgoraRtcEngineKit sharedEngineWithAppId:[AppContext.shared appId] delegate:self]
+ (UIGestureRecognizer*)createStartGesture {
    [self reLoadParamAll];
    
    UITapGestureRecognizer* gesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(startDebugAction:)];
    gesture.numberOfTapsRequired = 3;
    return gesture;
}

+ (void)startDebugAction:(UIGestureRecognizer*)ges {
    SRDebugViewController* vc = [SRDebugViewController new];
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
    
    NSArray* dataArray = [SRDebugInfo debugDataArray];
    [dataArray enumerateObjectsUsingBlock:^(NSDictionary* obj, NSUInteger idx, BOOL * stop) {
        NSString* key = obj[rTitleKey];
        BOOL status = [SRDebugInfo getSelectedStatusForKey:key];
        NSArray* paramArray = status ? obj[rSelectedParamKey] : obj[rUnselectedParamKey];
        [paramArray enumerateObjectsUsingBlock:^(NSString* obj, NSUInteger idx, BOOL * _Nonnull stop) {
            [engine setParameters:obj];
        }];
    }];
}

@end
