//
//  VLKTVSliderView.h
//  VoiceOnLine
//

#import "VLSBGItemBaseView.h"
@class VLSBGSliderView;

NS_ASSUME_NONNULL_BEGIN

@protocol VLSBGSliderViewDelegate <NSObject>

- (void)sliderView:(VLSBGSliderView *)sliderView valueChanged:(float)value;

@end

@interface VLSBGSliderView : VLSBGItemBaseView

- (instancetype)initWithMax:(float)max min:(float)min;

@property (nonatomic, assign) float value;

@property (nonatomic, weak) id <VLSBGSliderViewDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
