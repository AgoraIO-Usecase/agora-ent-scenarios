//
//  VLEffectView.h
//  AgoraEntScenarios
//
//  Created by CP on 2023/3/3.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
NS_ASSUME_NONNULL_BEGIN

@class VLSREffectView;
@protocol VLSREffectViewDelegate <NSObject>

@optional
- (void)effectItemClickAction:(NSInteger)effect;
@end

@interface VLSREffectView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSREffectViewDelegate>)delegate;

-(void)setSelectedIndex:(NSInteger)index;

@end

NS_ASSUME_NONNULL_END
