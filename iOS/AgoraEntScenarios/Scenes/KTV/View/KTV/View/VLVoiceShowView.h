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
@protocol VLVoiceShowViewDelegate <NSObject>

@optional
- (void)voiceItemClickAction:(NSInteger)effect;
@end

@interface VLVoiceShowView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLVoiceShowViewDelegate>)delegate imgSource:(NSArray *)imgSource nameSource:(NSArray *)nameSource userNoArray: (NSArray *)userNoArray selectUserNo:(NSString *)selectUserNo UIUpdateAble:(BOOL)UIUpdateAble;
-(void)setSelectedIndex:(NSInteger)index;

@end

NS_ASSUME_NONNULL_END
