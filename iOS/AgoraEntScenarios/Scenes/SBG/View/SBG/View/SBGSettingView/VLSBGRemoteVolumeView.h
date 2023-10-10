//
//  VLSBGRemoteVolumeView.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/9.
//

#import <Foundation/Foundation.h>
#import "VLSBGItemBaseView.h"

NS_ASSUME_NONNULL_BEGIN

@class VLSBGRemoteVolumeView;
@protocol VLSBGRemoteVolumeViewDelegate <NSObject>
- (void)view:(VLSBGRemoteVolumeView *)view remoteVolumeValueChanged:(int)value;
@end

@interface VLSBGRemoteVolumeView : VLSBGItemBaseView

- (id)initWithMin:(int)min withMax:(int)max withCurrent:(int)current;
-(void)setCurrent:(int)current;
@property (nonatomic, weak) id<VLSBGRemoteVolumeViewDelegate> delegate;
@end

NS_ASSUME_NONNULL_END
