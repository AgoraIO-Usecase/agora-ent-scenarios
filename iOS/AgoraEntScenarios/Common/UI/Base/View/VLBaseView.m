//
//  VLBaseView.m
//  VoiceOnLine
//

#import "VLBaseView.h"

@implementation VLBaseView

- (UIView *)containerView {
    if (_containerView == nil) {
        _containerView = [[UIView alloc] init];
    }
    return _containerView;
}

- (void)initSubViews {}
- (void)addSubViewConstraints {}

@end
