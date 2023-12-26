//
//  VLSRRemoteVolumeView.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/9.
//

#import <Foundation/Foundation.h>
#import "VLSRItemBaseView.h"

NS_ASSUME_NONNULL_BEGIN

@class VLSRRemoteVolumeView;
@protocol VLSRRemoteVolumeViewDelegate <NSObject>
- (void)view:(VLSRRemoteVolumeView *)view remoteVolumeValueChanged:(int)value;
@end

@interface VLSRRemoteVolumeView : VLSRItemBaseView

- (id)initWithMin:(int)min withMax:(int)max withCurrent:(int)current;
-(void)setCurrent:(int)current;
@property (nonatomic, weak) id<VLSRRemoteVolumeViewDelegate> delegate;
@end

NS_ASSUME_NONNULL_END
