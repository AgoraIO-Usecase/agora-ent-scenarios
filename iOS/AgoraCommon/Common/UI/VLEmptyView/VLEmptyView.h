//
//  VLEmptyView.h
//  VoiceOnLine
//

#import "VLUIView.h"

typedef void(^VLEmptyViewButtonBlock)(void);
NS_ASSUME_NONNULL_BEGIN

@protocol VLEmptyViewDelegate <NSObject>

@optional

@end

@interface VLEmptyView : VLUIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLEmptyViewDelegate>)delegate;
@property (nonatomic, copy) VLEmptyViewButtonBlock emptyViewButtonBlock;
@property (nonatomic, strong) UILabel *detailTextLabel;
- (void)setupViewByImage:(UIImage *)image text:(NSString *)text detailText:(NSString *)detailText butttonTitle:(NSString *)buttonTitle;

@end

NS_ASSUME_NONNULL_END
