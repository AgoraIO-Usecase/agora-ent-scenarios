//
//  DevViewController.h
//  AgoraEntScenarios
//
//  Created by CP on 2024/4/8.
//

#import <UIKit/UIKit.h>

typedef NS_ENUM(NSInteger, ToolboxEnv) {
    ToolboxEnvDev = 0,
    ToolboxEnvProd = 1,
};

NS_ASSUME_NONNULL_BEGIN

@interface DevViewController : UIViewController

+ (ToolboxEnv)toolboxEnv;

@end

NS_ASSUME_NONNULL_END
