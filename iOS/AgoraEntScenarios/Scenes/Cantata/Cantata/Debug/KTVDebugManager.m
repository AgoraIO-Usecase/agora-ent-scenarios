//
//  KTVDebugManager.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/17.
//

#import "KTVDebugManager.h"
#import "KTVDebugViewController.h"
#import "KTVDebugInfo.h"
#import <Cantata/Cantata-Swift.h>
@import AgoraRtcKit;

@implementation KTVDebugManager
//[AgoraRtcEngineKit sharedEngineWithAppId:[AppContext.shared appId] delegate:self]
+ (UIGestureRecognizer*)createStartGesture {
    [self reLoadParamAll];
    
    UITapGestureRecognizer* gesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(startDebugAction:)];
    gesture.numberOfTapsRequired = 3;
    return gesture;
}

+ (void)startDebugAction:(UIGestureRecognizer*)ges {
    KTVDebugViewController* vc = [KTVDebugViewController new];
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
    
    NSArray* dataArray = [KTVDebugInfo debugDataArray];
    [dataArray enumerateObjectsUsingBlock:^(NSDictionary* obj, NSUInteger idx, BOOL * stop) {
        NSString* key = obj[kTitleKey];
        BOOL status = [KTVDebugInfo getSelectedStatusForKey:key];
        NSArray* paramArray = status ? obj[kSelectedParamKey] : obj[kUnselectedParamKey];
        [paramArray enumerateObjectsUsingBlock:^(NSString* obj, NSUInteger idx, BOOL * _Nonnull stop) {
            KTVLogInfo(@"reLoadParamAll: %@, %@", key, obj);
            [engine setParameters:obj];
        }];
    }];
}

@end
