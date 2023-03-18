//
//  KTVSkipView.m
//  AgoraEntScenarios
//
//  Created by CP on 2023/1/29.
//

#import "KTVSkipView.h"
#import "AgoraEntScenarios-Swift.h"
@interface KTVSkipView ()
@property (nonatomic, strong) UIView *bgView;
@property (nonatomic, strong) UIButton *SkipBtn;
@property (nonatomic, strong) UIButton *CanCelBtn;
@property (nonatomic, copy) OnSkipCallback completion;
@end

@implementation KTVSkipView

-(instancetype)initWithFrame:(CGRect)frame completion:(OnSkipCallback _Nullable)completion {
    if(self = [super initWithFrame:frame]){
        self.completion = completion;
        self.bgView = [[UIView alloc]init];
        self.bgView.backgroundColor = [UIColor colorWithRed:63/255.0 green:64/255.0 blue:93/255.0 alpha:1];
        //self.bgView.layer.backgroundColor = [UIColor colorWithRed:0.027 green:0.063 blue:0.192 alpha:0.6].CGColor;
        self.bgView.layer.borderColor = [UIColor colorWithRed:1 green:1 blue:1 alpha:0.6].CGColor;
        [self addSubview:self.bgView];
        
        self.SkipBtn = [[UIButton alloc]init];
        [self.SkipBtn setFont:[UIFont systemFontOfSize:15]];
        [self.SkipBtn setTitle:@"  跳过前奏" forState:UIControlStateNormal];
        [self.SkipBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [self.SkipBtn addTarget:self action:@selector(skip:) forControlEvents:UIControlEventTouchUpInside];
        self.SkipBtn.tag = 200;
        [self.bgView addSubview:self.SkipBtn];
        
        self.CanCelBtn = [[UIButton alloc]init];
        [self.CanCelBtn setFont:[UIFont systemFontOfSize:13]];
        [self.CanCelBtn setImage:[UIImage sceneImageWithName:@"x"] forState:UIControlStateNormal];
        self.CanCelBtn.tag = 201;
        [self.CanCelBtn addTarget:self action:@selector(skip:) forControlEvents:UIControlEventTouchUpInside];
        [self.CanCelBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [self.bgView addSubview:self.CanCelBtn];
    }
    return self;
}

-(void)setSkipType:(SkipType)type{
    [self.SkipBtn setTitle:[NSString stringWithFormat:@"跳过%@奏", type == SkipTypePrelude ? @"前" : @"尾"] forState:UIControlStateNormal];
}

-(void)skip:(UIButton *)btn{
    self.completion(btn.tag == 200 ? SkipActionTypeDown : SkipActionTypeCancel);
}

- (void)layoutSubviews{
    [super layoutSubviews];
    self.bgView.frame = self.bounds;
    self.bgView.layer.cornerRadius = self.bounds.size.height / 2.0;
    self.bgView.layer.masksToBounds = true;
    self.bgView.layer.borderWidth = 1;
    self.SkipBtn.frame = CGRectMake(10, 0, self.bounds.size.width / 3.0 * 2 , self.bounds.size.height);
    self.CanCelBtn.frame = CGRectMake(self.bounds.size.width / 3.0 * 2, 0, self.bounds.size.width / 3.0 , self.bounds.size.height);
}

@end
