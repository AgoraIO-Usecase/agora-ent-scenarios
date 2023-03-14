//
//  KTVDebugManager.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/17.
//

#import <Foundation/Foundation.h>
@import UIKit;

NS_ASSUME_NONNULL_BEGIN

@interface KTVDebugManager : NSObject
+ (UIGestureRecognizer*)createStartGesture;
+ (void)reLoadParamAll;
@end

NS_ASSUME_NONNULL_END
