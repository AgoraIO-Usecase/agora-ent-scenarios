//
//  QDThemeManager.h
//  qmuidemo
//
//  Created by QMUI Team on 2017/5/9.
//  Copyright © 2017年 QMUI Team. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "QDThemeProtocol.h"
@import UIKit;

/// 简单对 QMUIThemeManager 做一层业务的封装，省去类型转换的工作量
@interface QDThemeManager : NSObject

@property(class, nonatomic, readonly, nullable) NSObject<QDThemeProtocol> *currentTheme;
@end

@interface UIColor (QDTheme)

NS_ASSUME_NONNULL_BEGIN

@property(class, nonatomic, strong, readonly) UIColor *qd_backgroundColor;
@property(class, nonatomic, strong, readonly) UIColor *qd_backgroundColorLighten;
@property(class, nonatomic, strong, readonly) UIColor *qd_backgroundColorHighlighted;
@property(class, nonatomic, strong, readonly) UIColor *qd_tintColor;
@property(class, nonatomic, strong, readonly) UIColor *qd_titleTextColor;
@property(class, nonatomic, strong, readonly) UIColor *qd_mainTextColor;
@property(class, nonatomic, strong, readonly) UIColor *qd_descriptionTextColor;
@property(class, nonatomic, strong, readonly) UIColor *qd_placeholderColor;
@property(class, nonatomic, strong, readonly) UIColor *qd_codeColor;
@property(class, nonatomic, strong, readonly) UIColor *qd_separatorColor;
@property(class, nonatomic, strong, readonly) UIColor *qd_gridItemTintColor;

NS_ASSUME_NONNULL_END

@end

@interface UIImage (QDTheme)

NS_ASSUME_NONNULL_BEGIN
@property(class, nonatomic, strong, readonly) UIImage *qd_navigationBarBackgroundImage;
@property(class, nonatomic, strong, readonly) UIImage *qd_navigationBarBackIndicatorImage;
@property(class, nonatomic, strong, readonly) UIImage *qd_navigationBarCloseImage;
@property(class, nonatomic, strong, readonly) UIImage *qd_navigationBarDisclosureIndicatorImage;
@property(class, nonatomic, strong, readonly) UIImage *qd_tableViewCellDisclosureIndicatorImage;
@property(class, nonatomic, strong, readonly) UIImage *qd_tableViewCellCheckmarkImage;
@property(class, nonatomic, strong, readonly) UIImage *qd_tableViewCellDetailButtonImage;
@property(class, nonatomic, strong, readonly) UIImage *qd_searchBarTextFieldBackgroundImage;
@property(class, nonatomic, strong, readonly) UIImage *qd_searchBarBackgroundImage;

NS_ASSUME_NONNULL_END

@end

@interface UIVisualEffect (QDTheme)

NS_ASSUME_NONNULL_BEGIN
@property(class, nonatomic, strong, readonly) UIVisualEffect *qd_standardBlurEffect;
NS_ASSUME_NONNULL_END
@end
