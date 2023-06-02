//
//  VLVoiceShowView.m
//  AgoraEntScenarios
//
//  Created by CP on 2023/3/3.
//

#import "VLVoiceShowView.h"
#import "KTVMacro.h"
@interface VLVoiceShowView()
@property(nonatomic, weak) id <VLVoiceShowViewDelegate>delegate;
@property (nonatomic,strong) UIButton *selBtn;
@end

@implementation VLVoiceShowView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLVoiceShowViewDelegate>)delegate dataSource:(NSArray *)dataSource {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        [self layoutUIWithDataSource: dataSource];
    }
    return self;
}

-(void)setSelectedIndex:(NSInteger)index{
    self.selBtn.selected = false;
    self.selBtn.layer.borderWidth = 0;
    [self.selBtn setBackgroundImage:nil forState:UIControlStateNormal];
    UIButton *btn = [self viewWithTag:200 + index];
    btn.selected = true;
    btn.layer.borderColor = [UIColor blueColor].CGColor;
    btn.layer.borderWidth = 1;
    [btn setBackgroundImage:[UIImage sceneImageWithName:@"ktv_selIcon"] forState:UIControlStateNormal];
    self.selBtn = btn;
}

-(void)layoutUIWithDataSource:(NSArray *)dataSource {
    UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-200)*0.5, 20, 200, 22)];
    titleLabel.text = KTVLocalizedString(@"设置人声突出");
    titleLabel.font = UIFontMake(18);
    titleLabel.textAlignment = NSTextAlignmentCenter;
    titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:titleLabel];
    
    for(int i=0;i< dataSource.count; i++){
        UIButton *btn = [[UIButton alloc]init];
        [btn setTitle:dataSource[i] forState:UIControlStateNormal];
        [btn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [btn setTag:200 + i];
        [btn addTarget:self action:@selector(click:) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:btn];
    }
}

-(void)click:(UIButton *)btn {
    self.selBtn.selected = false;
    self.selBtn.layer.borderWidth = 0;
    [self.selBtn setBackgroundImage:nil forState:UIControlStateNormal];
    
    btn.selected = true;
    btn.layer.borderColor = [UIColor blueColor].CGColor;
    btn.layer.borderWidth = 1;
    [btn setBackgroundImage:[UIImage sceneImageWithName:@"ktv_selIcon"] forState:UIControlStateNormal];
    self.selBtn = btn;
    if([self.delegate respondsToSelector:@selector(voiceItemClickAction:)]){
        [self.delegate voiceItemClickAction:btn.tag - 200];
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
