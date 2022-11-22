//
//  AEACategoryTitleCell.h
//  AgoraEditAvatar
//
//  Created by FanPengpeng on 2022/9/20.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface AEACategoryTitleCell : UICollectionViewCell

@property (copy, nonatomic) NSString *title;

@property (nullable,strong, nonatomic) UIFont *titleFont;
@property (nullable,strong, nonatomic) UIFont *titleSelectedFont;
@property (nullable,strong, nonatomic) UIColor *titleColor;
@property (nullable,strong, nonatomic) UIColor *titleSelectedColor;
@property (nullable,strong, nonatomic) UIView *indicator;

@end

NS_ASSUME_NONNULL_END
