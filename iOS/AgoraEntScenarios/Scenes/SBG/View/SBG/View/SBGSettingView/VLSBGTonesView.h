//
//  VLSBGTonesView.h
//  VoiceOnLine
//

#import "VLSBGItemBaseView.h"
@class VLSBGTonesView;

NS_ASSUME_NONNULL_BEGIN

@protocol VLSBGTonesViewDelegate <NSObject>

- (void)tonesViewValueChanged:(NSInteger)value;

@end

@interface VLSBGTonesView : VLSBGItemBaseView

- (instancetype)initWithMaxLevel:(NSInteger)maxLevel currentLevel:(NSInteger)currentLevel;

@property (nonatomic, weak) id <VLSBGTonesViewDelegate> delegate;

@property (nonatomic, assign, readonly) NSInteger value;

@end

NS_ASSUME_NONNULL_END
