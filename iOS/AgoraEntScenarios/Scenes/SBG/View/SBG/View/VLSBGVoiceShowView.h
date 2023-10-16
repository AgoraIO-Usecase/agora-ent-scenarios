//
//  VLVoiceShowView.h
//  AgoraEntScenarios
//
//  Created by CP on 2023/3/3.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
NS_ASSUME_NONNULL_BEGIN

@class VLVoiceShowView;
@protocol VLSBGVoiceShowViewDelegate <NSObject>

@optional
- (void)voiceItemClickAction:(NSInteger)effect;
@end

@interface VLSBGVoiceShowView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGVoiceShowViewDelegate>)delegate dataSource:(NSArray *)dataSource;

-(void)setSelectedIndex:(NSInteger)index;

@end

NS_ASSUME_NONNULL_END
