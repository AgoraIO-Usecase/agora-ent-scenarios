//
//  VLVoicePerShowView.h
//  AgoraEntScenarios
//
//  Created by CP on 2023/3/3.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class VLVoicePerShowView;
@protocol VLVoicePerShowViewDelegate <NSObject>

@optional
- (void)voicePerItemSelectedAction:(BOOL)isSelected;
- (void)didAIAECGradeChangedWithIndex:(NSInteger)index;
- (void)didVolQualityGradeChangedWithIndex:(NSInteger)index;
- (void)voiceDelaySelectedAction:(BOOL)isSelected;
-(void)didAECStateChange:(BOOL)enable;
-(void)didAECLevelSetWith:(NSInteger)level;
@end

@interface VLVoicePerShowView : UIView
- (instancetype)initWithFrame:(CGRect)frame isProfessional:(BOOL)isProfessional aecState:(BOOL)state aecLevel:(NSInteger)level isDelay:(BOOL)isDelay isRoomOwner:(BOOL)isRoomOwner volGrade:(NSInteger)vol aecGrade:(NSInteger)grade withDelegate:(id<VLVoicePerShowViewDelegate>)delegate;
@end

NS_ASSUME_NONNULL_END
