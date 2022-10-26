//
//  YGViewDisplayer.h
//  YGKitSwift
//
//  Created by edz on 2021/6/28.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

/// 布局
typedef NS_ENUM(NSInteger, YGViewDisplayOptionsPositionConstraints) {
/// 宽度拉满
  YGViewDisplayOptionsPositionConstraintsFullWidth = 0,
/// 充满屏幕
  YGViewDisplayOptionsPositionConstraintsFullScreen = 1,
/// 悬浮
  YGViewDisplayOptionsPositionConstraintsFloat = 2,
};

/// safeArea
typedef NS_ENUM(NSInteger, YGViewDisplayOptionsSafeArea) {
/// 空
  YGViewDisplayOptionsSafeAreaEmpty = 0,
/// 空且使用background填充
  YGViewDisplayOptionsSafeAreaEmptyFill = 1,
/// 内容覆盖
  YGViewDisplayOptionsSafeAreaOverridden = 2,
};

typedef NS_ENUM(NSInteger, YGViewDisplayOptionsScroll) {
  YGViewDisplayOptionsScrollDisabled = 0,
  YGViewDisplayOptionsScrollSwap = 1,
};

/// 状态栏
typedef NS_ENUM(NSInteger, YGViewDisplayOptionsStatusBar) {
/// 隐藏
  YGViewDisplayOptionsStatusBarHidden = 0,
/// 高亮
  YGViewDisplayOptionsStatusBarLight = 1,
/// 置灰
  YGViewDisplayOptionsStatusBarDark = 2,
/// ignore
  YGViewDisplayOptionsStatusBarIgnored = 3,
};

/// 用户交互
typedef NS_ENUM(NSInteger, YGViewDisplayOptionsUserInteraction) {
/// 消失
  YGViewDisplayOptionsUserInteractionDismiss = 0,
/// 向下层view传递事件
  YGViewDisplayOptionsUserInteractionForward = 1,
/// 拦截
  YGViewDisplayOptionsUserInteractionAbsorb = 2,
};

typedef NS_ENUM(NSInteger, YGViewDisplayOptionsWindowLevel) {
  YGViewDisplayOptionsWindowLevelAlert = 0,
  YGViewDisplayOptionsWindowLevelStatusBar = 1,
  YGViewDisplayOptionsWindowLevelNormal = 2,
};

/// 优先级
typedef NS_ENUM(NSInteger, YGViewDisplayPriority) {
  YGViewDisplayPriorityMax = 0,
  YGViewDisplayPriorityHigh = 1,
  YGViewDisplayPriorityNormal = 2,
  YGViewDisplayPriorityLow = 3,
  YGViewDisplayPriorityMin = 4,
};

@interface YGViewDisplayOptions : NSObject
@property (nonatomic, strong) NSString *identity;
/// 多久后自动消失，默认为MAXFLOAT
@property (nonatomic, assign) float duration;
/// 遮罩区颜色，默认为UIColor(white: 100.0/255.0, alpha: 0.2)
@property (nonatomic, strong) UIColor * _Nonnull screenBackgroundColor;
/// 内容区背景色，默认为white
@property (nonatomic, strong) UIColor * _Nonnull backgroundColor;
/// 圆角，默认20，浮窗为四角，底部推出为上部分
@property (nonatomic, assign) CGFloat cornerRidus;
/// 布局fullWidth
@property (nonatomic, assign) YGViewDisplayOptionsPositionConstraints positionConstraints;
/// 点击内容区 默认为absorb
@property (nonatomic, assign) YGViewDisplayOptionsUserInteraction interaction;
/// 点击屏幕 默认为absorb
@property (nonatomic, assign) YGViewDisplayOptionsUserInteraction screenInteraction;
/// safearea
@property (nonatomic, assign) YGViewDisplayOptionsSafeArea safeArea;
/// statusBar
@property (nonatomic, assign) YGViewDisplayOptionsStatusBar statusBar;
/// windowlevel
@property (nonatomic, assign) YGViewDisplayOptionsWindowLevel windowLevel;
/// 最大宽度
@property (nonatomic, assign) CGSize maxSize;
/// 优先级
@property (nonatomic, assign) YGViewDisplayPriority priority;
@end


@interface YGViewDisplayer : NSObject
/// 默认为NO
+ (void)useWindowScene:(BOOL)value;
//普通弹出
+ (void)display:(UIView * _Nonnull)view setupBlock:(void (^ _Nullable)(YGViewDisplayOptions * _Nonnull))setupBlock;
/// 底部弹出到中间
+ (void)popupCenter:(UIView * _Nonnull)view setupBlock:(void (^ _Nullable)(YGViewDisplayOptions * _Nonnull))setupBlock;
/// 底部弹出固定在底部，默认宽度充满屏幕
+ (void)popupBottom:(UIView * _Nonnull)view setupBlock:(void (^ _Nullable)(YGViewDisplayOptions * _Nonnull))setupBlock;
/// 淡出在中间
+ (void)fadeCenter:(UIView * _Nonnull)view setupBlock:(void (^ _Nullable)(YGViewDisplayOptions * _Nonnull))setupBlock;
/// dismiss某一个view
+ (void)dismiss:(UIView * _Nonnull)view completionHandler:(void (^ _Nullable)(void))completionHandler;
/// 根据identity
+ (void)dismissWithIdentity:(NSString * _Nonnull)identity completionHandler:(void (^ _Nullable)(void))completionHandler;
/// dismiss当前
+ (void)dismissDisplayed:(void (^ _Nullable)(void))completionHandler;
/// 移出所有，当前显示的和队列中的
+ (void)dismissAll:(void (^ _Nullable)(void))completionHandler;
@end


NS_ASSUME_NONNULL_END
