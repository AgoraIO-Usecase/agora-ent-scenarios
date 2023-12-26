//
//  VLSRSliderView.h
//  VoiceOnLine
//

#import "VLSRItemBaseView.h"
@class VLSRSliderView;

NS_ASSUME_NONNULL_BEGIN

@protocol VLSRSliderViewDelegate <NSObject>

- (void)sliderView:(VLSRSliderView *)sliderView valueChanged:(float)value;

@end

@interface VLSRSliderView : VLSRItemBaseView

- (instancetype)initWithMax:(float)max min:(float)min;

@property (nonatomic, assign) float value;

@property (nonatomic, weak) id <VLSRSliderViewDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
