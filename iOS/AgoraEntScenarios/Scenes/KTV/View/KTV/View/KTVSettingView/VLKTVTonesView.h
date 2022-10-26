//
//  VLKTVTonesView.h
//  VoiceOnLine
//

#import "VLKTVItemBaseView.h"
@class VLKTVTonesView;

NS_ASSUME_NONNULL_BEGIN

@protocol VLKTVTonesViewDelegate <NSObject>

- (void)tonesViewValueChanged:(NSInteger)value;

@end

@interface VLKTVTonesView : VLKTVItemBaseView

- (instancetype)initWithMaxLevel:(NSInteger)maxLevel currentLevel:(NSInteger)currentLevel;

@property (nonatomic, weak) id <VLKTVTonesViewDelegate> delegate;

@property (nonatomic, assign, readonly) NSInteger value;

@end

NS_ASSUME_NONNULL_END
