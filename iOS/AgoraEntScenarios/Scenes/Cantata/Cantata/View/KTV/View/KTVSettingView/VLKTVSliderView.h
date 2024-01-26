//
//  VLKTVSliderView.h
//  VoiceOnLine
//

#import "VLKTVItemBaseView.h"
@class VLKTVSliderView;

NS_ASSUME_NONNULL_BEGIN

@protocol VLKTVSliderViewDelegate <NSObject>

- (void)sliderView:(VLKTVSliderView *)sliderView valueChanged:(float)value;

@end

@interface VLKTVSliderView : VLKTVItemBaseView

- (instancetype)initWithMax:(float)max min:(float)min;

@property (nonatomic, assign) float value;

@property (nonatomic, weak) id <VLKTVSliderViewDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
