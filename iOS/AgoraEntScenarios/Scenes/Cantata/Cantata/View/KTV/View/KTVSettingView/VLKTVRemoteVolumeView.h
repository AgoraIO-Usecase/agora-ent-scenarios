//
//  VLKTVRemoteVolumeView.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/9.
//

#import <Foundation/Foundation.h>
#import "VLKTVItemBaseView.h"

NS_ASSUME_NONNULL_BEGIN

@class VLKTVRemoteVolumeView;
@protocol VLKTVRemoteVolumeViewDelegate <NSObject>
- (void)view:(VLKTVRemoteVolumeView *)view remoteVolumeValueChanged:(int)value;
@end

@interface VLKTVRemoteVolumeView : VLKTVItemBaseView

- (id)initWithMin:(int)min withMax:(int)max withCurrent:(int)current;
-(void)setCurrent:(int)current;
@property (nonatomic, weak) id<VLKTVRemoteVolumeViewDelegate> delegate;
@end

NS_ASSUME_NONNULL_END
