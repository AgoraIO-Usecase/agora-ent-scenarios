
//
//  WMZCodeView.m
//  WMZCode
//
//  Created by wmz on 2018/12/14.
//  Copyright © 2018年 wmz. All rights reserved.
//


//间距
#define margin 10

//滑块大小
#define codeSize 50

//贝塞尔曲线偏移
#define offset 9

//字体
#define WMZfont 22

#import "WMZCodeView.h"

@interface WMZCodeView()
{
    dispatch_source_t timer; //定时器
}

@property(nonatomic,copy)callBack block;                      //回调

@property(nonatomic,strong)UIImageView *mainImage;            //背景图片

@property(nonatomic,strong)UIImageView *moveImage;            //可移动图片

@property(nonatomic,strong)CAShapeLayer *maskLayer;           //遮罩层layer

@property(nonatomic,strong)UIView *maskView;                  //遮罩层

@property(nonatomic,assign)CGPoint randomPoint;               //随机位置

@property(nonatomic,strong)WMZSlider *slider;                 //滑动

@property(nonatomic,strong)UIButton *refresh;                 //刷新按钮

@property(nonatomic,assign)CGFloat width;                     //self的frame的width

@property(nonatomic,assign)CGFloat height;                    //self的frame的height

@property(nonatomic,assign)CGFloat seconds;                   //秒数

@end
@implementation WMZCodeView
/*
 * 初始化
 */
+ (instancetype)sharedInstance {
    static dispatch_once_t onceToken;
    static WMZCodeView *instance = nil;
    dispatch_once(&onceToken,^{
        instance = [[super allocWithZone:NULL] init];
    });
    return instance;
}

+ (id)allocWithZone:(struct _NSZone *)zone{
    return [self sharedInstance];
}

/*
 * 调用方法
 *
 * @param  rect      frame
 * @param  block     回调
 *
 */
- (WMZCodeView*)addCodeViewWithFrame:(CGRect)rect withBlock:(callBack)block{
    self.frame = rect;
    self.block = block;
    [self CodeTypeImageView];
    return self;
}

//CodeTypeImage
- (void)CodeTypeImageView{
    
    [self addSubview:({
        self.mainImage.frame = CGRectMake(margin, margin, self.width-margin*2, (self.width-margin*2)*0.6);
        self.mainImage.contentMode =  UIViewContentModeScaleAspectFill;
        self.mainImage.clipsToBounds = YES;
        self.mainImage;
    })];
    
    [self addSubview:({
        self.slider.frame = CGRectMake(margin, CGRectGetMaxY(self.mainImage.frame), self.width-margin*2, 55);
        [self.slider addTarget:self action:@selector(buttonAction:forEvent:) forControlEvents:UIControlEventAllTouchEvents];
        self.slider;
    })];
    
    [self addSubview:({
        self.refresh.frame = CGRectMake(self.width-margin-50, CGRectGetMaxY(self.slider.frame), 40, 40);
        [self.refresh setImage:[UIImage imageNamed:@"refresh"] forState:UIControlStateNormal];
        [self.refresh addTarget:self action:@selector(refreshAction) forControlEvents:UIControlEventTouchUpInside];
        self.refresh;
    })];
    
    CGRect rect = self.frame;
    rect.size.height = CGRectGetMaxY(self.refresh.frame)+margin;
    self.frame = rect;
    
    [self refreshAction];

}

//添加可移动的图片
- (void)addMoveImage{
    
    NSString *imageName = [NSString stringWithFormat:@"code_%u",arc4random() % 3];
    
    UIImage *normalImage = [UIImage imageNamed:imageName];
    normalImage = [normalImage dw_RescaleImageToSize:CGSizeMake(self.width-margin*2, (self.width-margin*2)*0.57)];
    self.mainImage.image = normalImage;
    
    [self.mainImage addSubview:({
        self.maskView.frame = CGRectMake(self.randomPoint.x, self.randomPoint.y, codeSize, codeSize);
        self.maskView;
    })];
    
    UIBezierPath *path = getCodePath();
    
    CGRect frame = self.maskView.frame;
    frame.size.width += offset;
    frame.size.height += offset;
    frame.origin.y -= offset;
    
    UIImage * thumbImage = [self.mainImage.image dw_SubImageWithRect:frame];
    thumbImage = [thumbImage dw_ClipImageWithPath:path mode:(DWContentModeScaleToFill)];
    [self.mainImage addSubview:({
        self.moveImage.frame = CGRectMake(0, self.randomPoint.y-offset, codeSize+offset, codeSize+offset);
        self.moveImage.image = thumbImage;
        //加阴影
        self.moveImage.layer.shadowColor = [UIColor blackColor].CGColor;
        self.moveImage.layer.shadowOpacity = 1.f;
        self.moveImage.layer.shadowRadius = 2.f;
        self.moveImage.layer.shadowOffset = CGSizeMake(0,0);
        //设置阴影路径
        UIBezierPath * newPath = [path copy];
        [newPath applyTransform:CGAffineTransformMakeTranslation(-path.bounds.origin.x, -path.bounds.origin.y)];
        self.moveImage.layer.shadowPath = newPath.CGPath;
        self.moveImage;
    })];
    
    [self.maskView.layer addSublayer:({
        self.maskLayer.frame = CGRectMake(0, 0, codeSize, codeSize);
        self.maskLayer.path = path.CGPath;
        self.maskLayer.strokeColor = [UIColor whiteColor].CGColor;
        self.maskLayer;
    })];
   
}

//图片验证滑块的所有事件
- (void)buttonAction:(UISlider*)slider forEvent:(UIEvent *)event{
    UITouchPhase phase = event.allTouches.anyObject.phase;
    if (phase == UITouchPhaseBegan) {
        dispatch_queue_t global = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
        self.seconds = 0;
        timer = dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0, 0, global);
        dispatch_source_set_timer(timer, DISPATCH_TIME_NOW, 0.1 * NSEC_PER_SEC, 0 * NSEC_PER_SEC);
        dispatch_source_set_event_handler(timer, ^{
            self.seconds+=0.1;
            NSLog(@"%.1f",self.seconds);
        });
        dispatch_resume(timer);
    }
    else if(phase == UITouchPhaseEnded){
        if (timer) {
            dispatch_source_cancel(timer);
        }
        
        CGFloat x = self.maskView.frame.origin.x;
        if (fabs(self.moveImage.frame.origin.x-x)<=5.00) {
            [self.layer addAnimation:successAnimal() forKey:@"successAnimal"];
            [self successShow];
        }else{
            [self.layer addAnimation:failAnimal() forKey:@"failAnimal"];
            [self defaultSlider];
            [self failShow];
        }
    }else if (phase == UITouchPhaseMoved){
        if (slider.value>self.width-margin*2-codeSize) {
            slider.value = self.width-margin*2-codeSize;
            return;
        }
        [self changeSliderWithVlue:slider.value];
        
    }
}

//设置默认的滑动
- (void)defaultSlider{
    self.slider.value = 0.05;
    [self changeSliderWithVlue:self.slider.value];
}

//图片位置随着Slider滑动改变frame
- (void)changeSliderWithVlue:(CGFloat)value{
    CGRect rect = self.moveImage.frame;
    CGFloat x = value * (self.mainImage.frame.size.width)-(value*codeSize);
    rect.origin.x = x;
    self.moveImage.frame = rect;
}

//刷新按钮事件
- (void)refreshAction{
    self.seconds = 0;
    if (timer) {
        dispatch_source_cancel(timer);
    }
    
    [self getRandomPoint];
    [self addMoveImage];
    [self defaultSlider];
}

//成功动画
static inline CABasicAnimation *successAnimal(){
    CABasicAnimation * animation = [CABasicAnimation animationWithKeyPath:@"opacity"];
    animation.duration = 0.2;
    animation.autoreverses = YES;
    animation.fromValue = @1;
    animation.toValue = @0;
    animation.removedOnCompletion = YES;
    return animation;
}

//失败动画
static inline CABasicAnimation *failAnimal(){
    CABasicAnimation *animation = [CABasicAnimation animationWithKeyPath:@"transform.rotation.z"];
    [animation setDuration:0.08];
    animation.fromValue = @(-M_1_PI/16);
    animation.toValue = @(M_1_PI/16);
    animation.repeatCount = 2;
    animation.autoreverses = YES;
    return animation;
}

//成功的操作
- (void)successShow{
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        __weak __typeof(self)weakSelf = self;
        NSString *tip = @"";
        if (self.seconds>0) {
            tip = [NSString stringWithFormat:@"耗时%.1fs",self.seconds];
        }
        
        UIImageView *successView;
        [self.mainImage addSubview:({
            successView = [[UIImageView alloc] initWithFrame:self.mainImage.bounds];
            [successView setImage:[UIImage imageNamed:@"success"]];
            
            UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(0, self.mainImage.bounds.size.height-60, self.mainImage.bounds.size.width, 50)];
            label.textAlignment = NSTextAlignmentCenter;
            label.font = [UIFont systemFontOfSize:16];
            label.text = tip;
            label.textColor = [UIColor whiteColor];
            [successView addSubview:label];
            
            successView;
        })];
        
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            if (weakSelf.block) {
                weakSelf.block(YES);
            }
            [successView removeFromSuperview];
            [weakSelf refreshAction];
        });
    });
   
}

- (void)failShow
{
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        __weak __typeof(self)weakSelf = self;
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            if (weakSelf.block) {
                weakSelf.block(NO);
            }
        });
    });
}

//获取当前VC
- (UIViewController *)getCurrentVC
{
    UIViewController *rootViewController = [UIApplication sharedApplication].keyWindow.rootViewController;
    
    UIViewController *currentVC = [self getCurrentVCFrom:rootViewController];
    
    return currentVC;
}

- (UIViewController *)getCurrentVCFrom:(UIViewController *)rootVC
{
    UIViewController *currentVC;
    
    if ([rootVC presentedViewController]) {
        // 视图是被presented出来的
        rootVC = [rootVC presentedViewController];
    }
    
    if ([rootVC isKindOfClass:[UITabBarController class]]) {
        // 根视图为UITabBarController
        currentVC = [self getCurrentVCFrom:[(UITabBarController *)rootVC selectedViewController]];
        
    } else if ([rootVC isKindOfClass:[UINavigationController class]]){
        // 根视图为UINavigationController
        currentVC = [self getCurrentVCFrom:[(UINavigationController *)rootVC visibleViewController]];
        
    } else {
        // 根视图为非导航类
        currentVC = rootVC;
    }
    return currentVC;
}

/**
 配置滑块贝塞尔曲线
 */
static inline UIBezierPath* getCodePath(){
    UIBezierPath *path = [UIBezierPath bezierPath];
    [path moveToPoint:CGPointMake(0, 0)];
    [path addLineToPoint:CGPointMake(codeSize*0.5-offset,0)];
    [path addQuadCurveToPoint:CGPointMake(codeSize*0.5+offset, 0) controlPoint:CGPointMake(codeSize*0.5, -offset*2)];
    [path addLineToPoint:CGPointMake(codeSize, 0)];
    
    
    [path addLineToPoint:CGPointMake(codeSize,codeSize*0.5-offset)];
    [path addQuadCurveToPoint:CGPointMake(codeSize, codeSize*0.5+offset) controlPoint:CGPointMake(codeSize+offset*2, codeSize*0.5)];
    [path addLineToPoint:CGPointMake(codeSize, codeSize)];
    
    [path addLineToPoint:CGPointMake(codeSize*0.5+offset,codeSize)];
    [path addQuadCurveToPoint:CGPointMake(codeSize*0.5-offset, codeSize) controlPoint:CGPointMake(codeSize*0.5, codeSize-offset*2)];
    [path addLineToPoint:CGPointMake(0, codeSize)];
    
    [path addLineToPoint:CGPointMake(0,codeSize*0.5+offset)];
    [path addQuadCurveToPoint:CGPointMake(0, codeSize*0.5-offset) controlPoint:CGPointMake(0+offset*2, codeSize*0.5)];
    [path addLineToPoint:CGPointMake(0, 0)];
    
    [path stroke];
    return path;
}

//获取随机位置
- (void)getRandomPoint{
    CGFloat widthMax =  self.mainImage.frame.size.width-margin-codeSize;
    CGFloat heightMax = self.mainImage.frame.size.height-codeSize*2;
    
    self.randomPoint = CGPointMake([self getRandomNumber:margin+codeSize*2 to:widthMax], [self getRandomNumber:offset*2 to:heightMax]);
    NSLog(@"%f %f",self.randomPoint.x,self.randomPoint.y);
}

//获取一个随机整数，范围在[from, to]，包括from，包括to
- (int)getRandomNumber:(int)from to:(int)to {
    return (int)(from + (arc4random() % (to - from + 1)));
}

- (UIImageView *)mainImage{
    if (!_mainImage) {
        _mainImage = [UIImageView new];
    }
    return _mainImage;
}

- (UIView *)maskView{
    if (!_maskView) {
        _maskView = [UIView new];
        _maskView.alpha = 0.5;
    }
    return _maskView;
}

- (UIImageView *)moveImage{
    if (!_moveImage) {
        _moveImage = [UIImageView new];
    }
    return _moveImage;
}

- (WMZSlider *)slider{
    if (!_slider) {
        _slider = [WMZSlider new];
        [_slider setThumbImage:[UIImage imageNamed:@"login_slide_thumb"] forState:UIControlStateNormal];
        [_slider setMinimumTrackTintColor:[UIColor colorWithWhite:0.89 alpha:1]];
        [_slider setMaximumTrackTintColor:[UIColor colorWithWhite:0.89 alpha:1]];
    }
    return _slider;
}


-(UIButton *)refresh{
    if (!_refresh) {
        _refresh = [UIButton buttonWithType:UIButtonTypeCustom];
        [_refresh setAdjustsImageWhenHighlighted:NO];
    }
    return _refresh;
}

- (CGFloat)width{
    if (!_width) {
        _width = self.frame.size.width;
    }
    return _width;
}


- (CGFloat)height{
    if (!_height) {
        _height = self.frame.size.height;
    }
    return _height;
}


- (CAShapeLayer *)maskLayer{
    if (!_maskLayer) {
        _maskLayer = [CAShapeLayer layer];
    }
    return _maskLayer;
}

@end

@implementation WMZSlider
//改变滑动条高度
- (CGRect)trackRectForBounds:(CGRect)bounds{
    CGRect result = [super trackRectForBounds:bounds];
    result.size.height=12;
    result.origin.y = (CGRectGetHeight(bounds)-CGRectGetHeight(result))/2;
    return result;
}

//调整滑块坐标
- (CGRect)thumbRectForBounds:(CGRect)bounds trackRect:(CGRect)rect value:(float)value {
    rect.origin.x = rect.origin.x - 10 ;
    rect.size.width = rect.size.width +20;
    return [super thumbRectForBounds:bounds
                           trackRect:rect
                               value:value];
}

@end


@implementation UIImage (Expand)

///截取当前image对象rect区域内的图像
-(UIImage *)dw_SubImageWithRect:(CGRect)rect{
    CGFloat scale = self.scale;
    
    CGRect scaleRect = CGRectMake(rect.origin.x * scale, rect.origin.y * scale, rect.size.width * scale, rect.size.height * scale);
    CGImageRef newImageRef = CGImageCreateWithImageInRect(self.CGImage, scaleRect);
    UIImage *newImage = [[UIImage imageWithCGImage:newImageRef] dw_RescaleImageToSize:rect.size];
    CGImageRelease(newImageRef);
    return newImage;
}

///压缩图片至指定尺寸
-(UIImage *)dw_RescaleImageToSize:(CGSize)size{
    CGRect rect = (CGRect){CGPointZero, size};
    
    UIGraphicsBeginImageContextWithOptions(size, NO, [UIScreen mainScreen].scale);
    
    [self drawInRect:rect];
    
    UIImage *resImage = UIGraphicsGetImageFromCurrentImageContext();
    
    UIGraphicsEndImageContext();
    
    return resImage;
}

///按给定path剪裁图片
/**
 path:路径，剪裁区域。
 mode:填充模式
 */
-(UIImage *)dw_ClipImageWithPath:(UIBezierPath *)path mode:(DWContentMode)mode{
    CGFloat originScale = self.size.width * 1.0 / self.size.height;
    CGRect boxBounds = path.bounds;
    CGFloat width = boxBounds.size.width;
    CGFloat height = width / originScale;
    switch (mode) {
        case DWContentModeScaleAspectFit:
        {
            if (height > boxBounds.size.height) {
                height = boxBounds.size.height;
                width = height * originScale;
            }
        }
            break;
        case DWContentModeScaleAspectFill:
        {
            if (height < boxBounds.size.height) {
                height = boxBounds.size.height;
                width = height * originScale;
            }
        }
            break;
        default:
            if (height != boxBounds.size.height) {
                height = boxBounds.size.height;
            }
            break;
    }
    
    ///开启上下文
    UIGraphicsBeginImageContextWithOptions(boxBounds.size, NO, [UIScreen mainScreen].scale);
    CGContextRef bitmap = UIGraphicsGetCurrentContext();
    
    ///归零path
    UIBezierPath * newPath = [path copy];
    [newPath applyTransform:CGAffineTransformMakeTranslation(-path.bounds.origin.x, -path.bounds.origin.y)];
    [newPath addClip];
    
    ///移动原点至图片中心
    CGContextTranslateCTM(bitmap, boxBounds.size.width / 2.0, boxBounds.size.height / 2.0);
    CGContextScaleCTM(bitmap, 1.0, -1.0);
    CGContextDrawImage(bitmap, CGRectMake(-width / 2, -height / 2, width, height), self.CGImage);
    
    ///生成图片
    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return newImage;
}

//裁剪图片
- (UIImage*)imageScaleToSize:(CGSize)size{
    UIGraphicsBeginImageContext(size);//size为CGSize类型，即你所需要的图片尺寸
    [self drawInRect:CGRectMake(0,0, size.width, size.height)];
    UIImage *scaledImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return scaledImage;
}

@end
