//
//  KTVSkipView.m
//  AgoraEntScenarios
//
//  Created by CP on 2023/1/29.
//

#import "KTVSkipView.h"
@interface KTVSkipView ()
@property (nonatomic, strong) UIView *bgView;
@property (nonatomic, strong) UIButton *skipBtn;
@property (nonatomic, strong) UIButton *canCelBtn;
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
        
        self.skipBtn = [[UIButton alloc]init];
        self.skipBtn.titleLabel.font = [UIFont systemFontOfSize:15];
        [self.skipBtn setTitle:@"  跳过前奏" forState:UIControlStateNormal];
        [self.skipBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [self.skipBtn addTarget:self action:@selector(skip:) forControlEvents:UIControlEventTouchUpInside];
        self.skipBtn.tag = 200;
        [self.bgView addSubview:self.skipBtn];
        
        self.canCelBtn = [[UIButton alloc]init];
        self.canCelBtn.titleLabel.font = [UIFont systemFontOfSize:13];
        [self.canCelBtn setImage:[UIImage sceneImageWithName:@"x"] forState:UIControlStateNormal];
        self.canCelBtn.tag = 201;
        [self.canCelBtn addTarget:self action:@selector(skip:) forControlEvents:UIControlEventTouchUpInside];
        [self.canCelBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [self.bgView addSubview:self.canCelBtn];
    }
    return self;
}

-(void)setSkipType:(SkipType)type{
    [self.skipBtn setTitle:[NSString stringWithFormat:@"跳过%@奏", type == SkipTypePrelude ? @"前" : @"尾"] forState:UIControlStateNormal];
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
    self.skipBtn.frame = CGRectMake(10, 0, self.bounds.size.width / 3.0 * 2 , self.bounds.size.height);
    self.canCelBtn.frame = CGRectMake(self.bounds.size.width / 3.0 * 2, 0, self.bounds.size.width / 3.0 , self.bounds.size.height);
}

@end
