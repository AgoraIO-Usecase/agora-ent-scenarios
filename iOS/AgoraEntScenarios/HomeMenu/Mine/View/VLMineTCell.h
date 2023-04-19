//
//  VLMineTCell.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface VLMineTCell : UITableViewCell

@property (nonatomic, strong) NSDictionary *dict;

@property (nonatomic, strong) UIImageView *iconImgView;
@property (nonatomic, strong) UILabel *itemTitleLabel;
@property (nonatomic, strong) UIImageView *arrowImgView;

- (void)setIconImageName:(NSString *)imgStr title:(NSString *)title;

@end

NS_ASSUME_NONNULL_END
