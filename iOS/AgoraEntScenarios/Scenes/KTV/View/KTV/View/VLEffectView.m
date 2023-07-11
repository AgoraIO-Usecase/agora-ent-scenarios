//
//  VLEffectView.m
//  AgoraEntScenarios
//
//  Created by CP on 2023/3/3.
//

#import "VLEffectView.h"
#import "AESMacro.h"
@interface VLEffectView()
@property(nonatomic, weak) id <VLEffectViewDelegate>delegate;
@property (nonatomic,strong) UIButton *selBtn;
@end

@implementation VLEffectView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLEffectViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        [self layoutUI];
    }
    return self;
}

-(void)setSelectedIndex:(NSInteger)index{
    self.selBtn.selected = false;
    self.selBtn.layer.borderWidth = 0;
    UIButton *btn = [self viewWithTag:200 + index];
    btn.selected = true;
    btn.layer.borderColor = [UIColor blueColor].CGColor;
    btn.layer.borderWidth = 1;
    self.selBtn = btn;
}

-(void)layoutUI {
    UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-200)*0.5, 20, 200, 22)];
    titleLabel.text = KTVLocalizedString(@"音效");
    titleLabel.font = UIFontMake(18);
    titleLabel.textAlignment = NSTextAlignmentCenter;
    titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:titleLabel];
    
    NSArray *effects = @[@"原声", @"KTV",@"演唱会", @"录音棚", @"留声机", @"空旷", @"空灵", @"流行",@"R&B"];
    NSArray *imgs = @[@"ktv_console_setting1",@"ktv_console_setting2",@"ktv_console_setting3",@"ktv_console_setting4"];
    for(int i=0;i< effects.count; i++){
        UIButton *btn = [[UIButton alloc]init];
        [btn setTitle:effects[i] forState:UIControlStateNormal];
        [btn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [btn setBackgroundImage:[UIImage sceneImageWithName:imgs[i % 4]] forState:UIControlStateNormal];
        [btn setTag:200 + i];
        [btn addTarget:self action:@selector(click:) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:btn];
        if(i == 0){
            btn.selected = true;
            btn.layer.borderColor = [UIColor blueColor].CGColor;
            btn.layer.borderWidth = 1;
            self.selBtn = btn;
        }
    }
}

-(void)click:(UIButton *)btn {
    self.selBtn.selected = false;
    self.selBtn.layer.borderWidth = 0;
    
    btn.selected = true;
    btn.layer.borderColor = [UIColor blueColor].CGColor;
    btn.layer.borderWidth = 1;
    self.selBtn = btn;
    if([self.delegate respondsToSelector:@selector(effectItemClickAction:)]){
        [self.delegate effectItemClickAction:btn.tag - 200];
    }
}

-(void)layoutSubviews{
    [super layoutSubviews];
    CGFloat margin = 20;
    CGFloat width = (self.bounds.size.width - 4 * margin) / 3.0;
    CGFloat height = (self.bounds.size.height - 50 - 4 * margin) / 3.0;
    for(UIView *view in self.subviews){
        if([view isMemberOfClass:[UIButton class]]){
            UIButton *btn = (UIButton *)view;
            CGFloat x =  margin + (btn.tag - 200) % 3 * (width + margin);
            CGFloat y = margin + (btn.tag - 200) / 3 * (height + margin) + 42;
            btn.frame = CGRectMake(x, y, width, height);
            btn.layer.cornerRadius = 5;
            btn.layer.masksToBounds = true;
        }
    }
}

@end
