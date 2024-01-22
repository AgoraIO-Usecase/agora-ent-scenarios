//
//  VLDebugView.h
//  AgoraEntScenarios
//
//  Created by CP on 2023/3/3.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "DHCDebugView.h"
NS_ASSUME_NONNULL_BEGIN

@class DHCDebugView;
@protocol DHCDebugViewDelegate <NSObject>

@optional
-(void)didDumpModeChanged:(BOOL)enable;
-(void)didExportLogWith:(NSString *)path;
-(void)didParamsSetWith:(NSString *)key value:(NSString *)value;
@end

@interface DHCDebugView : UIView
- (instancetype)initWithFrame:(CGRect)frame isDumpMode:(BOOL)isDumpMode withDelegate:(id<DHCDebugViewDelegate>)delegate;
@end

NS_ASSUME_NONNULL_END
