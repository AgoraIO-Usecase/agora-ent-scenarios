//
//  VLVoicePerShowView.h
//  AgoraEntScenarios
//
//  Created by CP on 2023/3/3.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
NS_ASSUME_NONNULL_BEGIN

@class VLSBGVoicePerShowView;
@protocol VLSBGVoicePerShowViewDelegate <NSObject>

@optional
- (void)voicePerItemSelectedAction:(BOOL)isSelected;
@end

@interface VLSBGVoicePerShowView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGVoicePerShowViewDelegate>)delegate;

-(void)setPerSelected:(BOOL)isSelected;

@end

NS_ASSUME_NONNULL_END
