//
//  VLSRTonesView.h
//  VoiceOnLine
//

#import "VLSRItemBaseView.h"
@class VLSRTonesView;

NS_ASSUME_NONNULL_BEGIN

@protocol VLSRTonesViewDelegate <NSObject>

- (void)tonesViewValueChanged:(NSInteger)value;

@end

@interface VLSRTonesView : VLSRItemBaseView

- (instancetype)initWithMaxLevel:(NSInteger)maxLevel currentLevel:(NSInteger)currentLevel;

@property (nonatomic, weak) id <VLSRTonesViewDelegate> delegate;

@property (nonatomic, assign, readonly) NSInteger value;

@end

NS_ASSUME_NONNULL_END
