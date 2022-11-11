//
//  VLsoundEffectView.m
//  VoiceOnLine
//

#import "VLsoundEffectView.h"
#import "ZQTCustomSwitch.h"
#import "VLHotSpotBtn.h"
#import "KTVMacro.h"
@import QMUIKit;
@import YYCategories;
@import ChameleonFramework;

@interface VLsoundEffectView ()

@property(nonatomic, weak) id <VLsoundEffectViewDelegate>delegate;
@property (nonatomic, strong) ZQTCustomSwitch *openSwitch;
@property (nonatomic, strong) UIImageView *rotateImgView;
@property (nonatomic, strong) UILabel *heFengLabel;
@property (nonatomic, strong) UIView  *heFengView;
@property (nonatomic, strong) UILabel *xiaoDiaoLabel;
@property (nonatomic, strong) UIView  *xiaoDiaoView;
@property (nonatomic, strong) UILabel *daDiaoLabel;
@property (nonatomic, strong) UIView  *daDiaoView;

@property (nonatomic, assign) CGFloat rotateValue;
@property (nonatomic, assign) CGFloat beforeValue;

@property (nonatomic, assign) VLKTVSoundEffectType typeValue;

@end


@implementation VLsoundEffectView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLsoundEffectViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        self.typeValue = -1;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    self.beforeValue = 0;
    self.rotateValue = 0;
    VLHotSpotBtn *backBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(20, 20, 20, 20)];
    [backBtn setImage:[UIImage sceneImageWithName:@"ktv_back_whiteIcon"] forState:UIControlStateNormal];
    [backBtn addTarget:self action:@selector(backBtnClickEvent) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:backBtn];
    
    UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-200)*0.5, 20, 200, 22)];
    titleLabel.text = KTVLocalizedString(@"音效");
    titleLabel.font = UIFontMake(16);
    titleLabel.textAlignment = NSTextAlignmentCenter;
    titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:titleLabel];
    
    UILabel *electronicLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, titleLabel.bottom+25, 55, 17)];
    electronicLabel.text = KTVLocalizedString(@"启用电音");
    electronicLabel.font = UIFontMake(13);
    electronicLabel.textColor = UIColorMakeWithHex(@"#C6C4DE");
    [self addSubview:electronicLabel];
    
    self.openSwitch = [[ZQTCustomSwitch alloc]initWithFrame:CGRectMake(electronicLabel.right+20,electronicLabel.centerY-13, 46, 26) onColor:UIColorMakeWithHex(@"#219BFF") offColor:HexColor(@"#DDDDDD") font:[UIFont systemFontOfSize:9] ballSize:22];
    [self.openSwitch addTarget:self action:@selector(switchPressed:) forControlEvents:UIControlEventValueChanged];
    [self addSubview:self.openSwitch];
    
    UILabel *typeLabel = [[UILabel alloc]initWithFrame:CGRectMake(electronicLabel.left, electronicLabel.bottom+24, 55, 17)];
    typeLabel.text = KTVLocalizedString(@"选择调式");
    typeLabel.font = UIFontMake(13);
    typeLabel.textColor = UIColorMakeWithHex(@"#C6C4DE");
    [self addSubview:typeLabel];
    
    UIView *bgView = [[UIView alloc]initWithFrame:CGRectMake((self.width-220)*0.5, typeLabel.bottom+2, 220, 220)];
    bgView.layer.cornerRadius = 110;
    bgView.layer.masksToBounds = YES;
    bgView.backgroundColor = UIColorMakeWithRGBA(4, 9, 37, 0.35);
    [self addSubview:bgView];
    
    UIImageView *circleImgView = [[UIImageView alloc]initWithFrame:CGRectMake(55, 55, 110, 110)];
    circleImgView.image = [UIImage sceneImageWithName:@"ktv_circle_bgIcon"];
    circleImgView.userInteractionEnabled = YES;
    [bgView addSubview:circleImgView];
    
    self.rotateImgView = [[UIImageView alloc]initWithFrame:CGRectMake(62.5, 62.5, 95, 95)];
    self.rotateImgView.image = [UIImage sceneImageWithName:@"ktv_soundEffert_icon"];
    self.rotateImgView.userInteractionEnabled = YES;
//    UITapGestureRecognizer *tapGes = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(tapGesEvent:)];
//    [self.rotateImgView addGestureRecognizer:tapGes];
    [bgView addSubview:self.rotateImgView];
    
    self.heFengLabel = [[UILabel alloc]initWithFrame:CGRectMake(10, self.rotateImgView.centerY-12, 26, 17)];
    self.heFengLabel.text = KTVLocalizedString(@"和风");
    self.heFengLabel.font = UIFontBoldMake(12);
    self.heFengLabel.textAlignment = NSTextAlignmentCenter;
    self.heFengLabel.textColor = UIColorMakeWithHex(@"#C6C4DE");
    [bgView addSubview:self.heFengLabel];
    
    self.heFengView = [[UIView alloc]initWithFrame:CGRectMake(self.heFengLabel.right+5, self.heFengLabel.centerY-3.5, 7, 7)];
    self.heFengView.layer.cornerRadius = 3.5;
    self.heFengView.layer.masksToBounds = YES;
    self.heFengView.backgroundColor = UIColorMakeWithHex(@"#009FFF");
    [bgView addSubview:self.heFengView];
    
    VLHotSpotBtn *heFengBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(0, self.rotateImgView.centerY-20,50, 40)];
    heFengBtn.tag = 0;
    [heFengBtn addTarget:self action:@selector(itemBtnClick:) forControlEvents:UIControlEventTouchUpInside];
    [bgView addSubview:heFengBtn];
    
    self.xiaoDiaoLabel = [[UILabel alloc]initWithFrame:CGRectMake((220-26)*0.5, 15, 26, 17)];
    self.xiaoDiaoLabel.text = KTVLocalizedString(@"小调");
    self.xiaoDiaoLabel.font = UIFontBoldMake(12);
    self.xiaoDiaoLabel.textAlignment = NSTextAlignmentCenter;
    self.xiaoDiaoLabel.textColor = UIColorMakeWithHex(@"#979CBB");
    [bgView addSubview:self.xiaoDiaoLabel];
    
    self.xiaoDiaoView = [[UIView alloc]initWithFrame:CGRectMake(self.xiaoDiaoLabel.centerX-3.5, self.xiaoDiaoLabel.bottom+5, 7, 7)];
    self.xiaoDiaoView.layer.cornerRadius = 3.5;
    self.xiaoDiaoView.layer.masksToBounds = YES;
    self.xiaoDiaoView.backgroundColor = UIColorMakeWithHex(@"#3C4267");
    [bgView addSubview:self.xiaoDiaoView];
    
    VLHotSpotBtn *xiaoDiaoBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake((220-50)*0.5, 5,50, 50)];
    xiaoDiaoBtn.tag = 1;
    [xiaoDiaoBtn addTarget:self action:@selector(itemBtnClick:) forControlEvents:UIControlEventTouchUpInside];
    [bgView addSubview:xiaoDiaoBtn];
    
    self.daDiaoView = [[UIView alloc]initWithFrame:CGRectMake(circleImgView.right+5, self.heFengView.top, 7, 7)];
    self.daDiaoView.layer.cornerRadius = 3.5;
    self.daDiaoView.layer.masksToBounds = YES;
    self.daDiaoView.backgroundColor = UIColorMakeWithHex(@"#3C4267");
    [bgView addSubview:self.daDiaoView];
    
    self.daDiaoLabel = [[UILabel alloc]initWithFrame:CGRectMake(self.daDiaoView.right+5, self.heFengLabel.top, 26, 17)];
    self.daDiaoLabel.text = KTVLocalizedString(@"大调");
    self.daDiaoLabel.font = UIFontBoldMake(12);
    self.daDiaoLabel.textAlignment = NSTextAlignmentCenter;
    self.daDiaoLabel.textColor = UIColorMakeWithHex(@"#979CBB");
    [bgView addSubview:self.daDiaoLabel];
    
    VLHotSpotBtn *daDiaoBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(circleImgView.right+5, heFengBtn.top,50, 40)];
    daDiaoBtn.tag = 2;
    [daDiaoBtn addTarget:self action:@selector(itemBtnClick:) forControlEvents:UIControlEventTouchUpInside];
    [bgView addSubview:daDiaoBtn];
    
}


- (void)switchPressed:(ZQTCustomSwitch *)sender {
    if(!self.openSwitch.isOn) {
        [self closeEffect];
    }
    else {
        [self validateEffect];
    }
}

//- (void)tapGesEvent:(UITapGestureRecognizer *)tapGes {
//    if (self.openSwitch.on) {
//        CGPoint point = [tapGes locationInView:self.rotateImgView];
//
//        if (point.x>=0 && point.x<=55 && point.y>=22.5 && point.y<=77.5) {

//        }else if (point.x>=22.5 && point.x<=77.5 && point.y>=0 && point.y<=55) {
//
//        }else if (point.x>=55 && point.x<=110 && point.y>=22.5 && point.y<=77.5){
//
//        }
//    }
//}

- (void)closeEffect {
    if (!self.openSwitch.isOn) {
        if (self.delegate && [self.delegate respondsToSelector:@selector(soundEffectItemClickAction:)]) {
            [self.delegate soundEffectItemClickAction:VLKTVSoundEffectTypeNone];
        }
    }
}

- (void)validateEffect
{
    if(self.typeValue == -1) {
        self.typeValue = VLKTVSoundEffectTypeHeFeng;
    }
    
    if (self.delegate && [self.delegate respondsToSelector:@selector(soundEffectItemClickAction:)]) {
        [self.delegate soundEffectItemClickAction:self.typeValue];
    }
}

- (void)itemBtnClick:(VLHotSpotBtn *)sender {
    if (!self.openSwitch.isOn) {
        [self closeEffect];
        return;
    }
    if (sender.tag == 0) {
        self.rotateValue = 0;
        [self rotateAnimationFrom:self.beforeValue toValue:self.rotateValue];
        self.typeValue = VLKTVSoundEffectTypeHeFeng;
        
        self.heFengLabel.font = UIFontBoldMake(12);
        self.heFengLabel.textColor = UIColorMakeWithHex(@"#C6C4DE");
        self.heFengView.backgroundColor = UIColorMakeWithHex(@"#009FFF");
        self.xiaoDiaoLabel.textColor = UIColorMakeWithHex(@"#979CBB");
        self.xiaoDiaoLabel.font = UIFontMake(12);
        self.xiaoDiaoView.backgroundColor = UIColorMakeWithHex(@"#3C4267");
        self.daDiaoView.backgroundColor = UIColorMakeWithHex(@"#3C4267");
        self.daDiaoLabel.textColor = UIColorMakeWithHex(@"#979CBB");
        self.daDiaoLabel.font = UIFontMake(12);
    }else if (sender.tag == 1){
        self.rotateValue = M_PI_2;
        [self rotateAnimationFrom:self.beforeValue toValue:self.rotateValue];
        self.typeValue = VLKTVSoundEffectTypeXiaoDiao;
        
        self.heFengLabel.font = UIFontMake(12);
        self.heFengLabel.textColor = UIColorMakeWithHex(@"#979CBB");
        self.heFengView.backgroundColor = UIColorMakeWithHex(@"#3C4267");
        self.xiaoDiaoLabel.textColor = UIColorMakeWithHex(@"#C6C4DE");
        self.xiaoDiaoLabel.font = UIFontBoldMake(12);
        self.xiaoDiaoView.backgroundColor = UIColorMakeWithHex(@"#009FFF");
        self.daDiaoView.backgroundColor = UIColorMakeWithHex(@"#3C4267");
        self.daDiaoLabel.textColor = UIColorMakeWithHex(@"#979CBB");
        self.daDiaoLabel.font = UIFontMake(12);
    }else if (sender.tag == 2){
        self.rotateValue = M_PI;
        [self rotateAnimationFrom:self.beforeValue toValue:self.rotateValue];
        self.typeValue = VLKTVSoundEffectTypeDaDiao;
        
        self.heFengLabel.font = UIFontMake(12);
        self.heFengLabel.textColor = UIColorMakeWithHex(@"#979CBB");
        self.heFengView.backgroundColor = UIColorMakeWithHex(@"#3C4267");
        self.xiaoDiaoLabel.textColor = UIColorMakeWithHex(@"#3C4267");
        self.xiaoDiaoLabel.font = UIFontMake(12);
        self.xiaoDiaoView.backgroundColor = UIColorMakeWithHex(@"#979CBB");
        
        self.daDiaoLabel.textColor = UIColorMakeWithHex(@"#C6C4DE");
        self.daDiaoLabel.font = UIFontBoldMake(12);
        self.daDiaoView.backgroundColor = UIColorMakeWithHex(@"#009FFF");
    }
    [self validateEffect];
}

- (void)rotateAnimationFrom:(CGFloat)fromValue toValue:(CGFloat)toValue {
    CABasicAnimation *basicAnimation = [CABasicAnimation animationWithKeyPath:@"transform.rotation.z"];
    basicAnimation.fromValue = @(fromValue);
    basicAnimation.toValue = @(toValue);
    basicAnimation.repeatCount = 1;
    basicAnimation.duration = 0.25;
    //若要将removedOnCompletion设置为NO,fillMode必须设置为kCAFillModeForwards
    basicAnimation.fillMode = kCAFillModeForwards;
    //若要将removedOnCompletion设置为NO表示动画结束后图形不会回到初始状态
    basicAnimation.removedOnCompletion = NO;
    [self.rotateImgView.layer addAnimation:basicAnimation forKey:nil];
    self.beforeValue = toValue;
}

- (void)backBtnClickEvent {
    if (self.delegate && [self.delegate respondsToSelector:@selector(soundEffectViewBackBtnAction)]) {
        [self.delegate soundEffectViewBackBtnAction];
    }
}

@end
