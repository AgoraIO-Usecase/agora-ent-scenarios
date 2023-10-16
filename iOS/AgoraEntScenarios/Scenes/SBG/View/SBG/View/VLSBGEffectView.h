//
//  VLEffectView.h
//  AgoraEntScenarios
//
//  Created by CP on 2023/3/3.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
NS_ASSUME_NONNULL_BEGIN

@class VLEffectView;
@protocol VLSBGEffectViewDelegate <NSObject>

@optional
- (void)effectItemClickAction:(NSInteger)effect;
@end

@interface VLSBGEffectView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGEffectViewDelegate>)delegate;

-(void)setSelectedIndex:(NSInteger)index;

@end

NS_ASSUME_NONNULL_END
