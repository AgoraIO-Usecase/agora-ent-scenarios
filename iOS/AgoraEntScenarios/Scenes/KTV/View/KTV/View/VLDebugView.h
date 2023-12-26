//
//  VLDebugView.h
//  AgoraEntScenarios
//
//  Created by CP on 2023/3/3.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
NS_ASSUME_NONNULL_BEGIN

@class VLDebugView;
@protocol VLDebugViewDelegate <NSObject>

@optional
-(void)didDumpModeChanged:(BOOL)enable;
-(void)didExportLogWith:(NSString *)path;
-(void)didParamsSetWith:(NSString *)key value:(NSString *)value;
@end

@interface VLDebugView : UIView
- (instancetype)initWithFrame:(CGRect)frame channelName:(NSString *)name sdkVer:(NSString *)ver isDumpMode:(BOOL)isDumpMode withDelegate:(id<VLDebugViewDelegate>)delegate;
@end

NS_ASSUME_NONNULL_END
