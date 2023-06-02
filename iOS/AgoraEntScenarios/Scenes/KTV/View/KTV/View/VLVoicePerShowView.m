//
//  VLVoicePerShowView.m
//  AgoraEntScenarios
//
//  Created by CP on 2023/3/3.
//

#import "VLVoicePerShowView.h"
#import "KTVMacro.h"
#import "VLMacroDefine.h"
@interface VLVoicePerShowView()
@property(nonatomic, weak) id <VLVoicePerShowViewDelegate>delegate;
@property (nonatomic,strong) UISwitch *voiceSwitch;
@property (nonatomic,strong) UISwitch *delaySwitch;
@property (nonatomic, assign) NSInteger aecGrade;
@property (nonatomic, strong) UISegmentedControl *qualitySegment;
@property (nonatomic, strong) UILabel *qualityLabel;
@end

@implementation VLVoicePerShowView

- (instancetype)initWithFrame:(CGRect)frame aecGrade:(NSInteger)grade withDelegate:(id<VLVoicePerShowViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        self.aecGrade = grade;
        [self layoutUI];
    }
    return self;
}

-(void)setPerSelected:(BOOL)isSelected{
    self.voiceSwitch.on = isSelected;
}

-(void)setAECLevel:(NSInteger)level {
   // self.menu.selectedIndex = level;
}

-(void)layoutUI {
    UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-200)*0.5, 20, 200, 22)];
    titleLabel.text = KTVLocalizedString(@"配置");
    titleLabel.font = UIFontMake(18);
    titleLabel.textAlignment = NSTextAlignmentCenter;
    titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:titleLabel];
    
    UILabel *headLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, 60, 80, 30)];
    headLabel.text = @"专业模式";
    headLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:headLabel];
    
    self.voiceSwitch = [[UISwitch alloc]initWithFrame:CGRectMake(SCREEN_WIDTH - 70, 60, 50, 30)];
    self.voiceSwitch.onTintColor = UIColorMakeWithHex(@"#099DFD");
    [self.voiceSwitch addTarget:self action:@selector(perChange:) forControlEvents:UIControlEventValueChanged];
    [self addSubview:_voiceSwitch];
    
    UIView *sepView = [[UIView alloc]initWithFrame:CGRectMake(20, 105, SCREEN_WIDTH - 40 , 1)];
    sepView.backgroundColor = [UIColor colorWithRed:0.938 green:0.938 blue:0.938 alpha:0.08];
    [self addSubview:sepView];
    
    UILabel *qualityLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, 123, 80, 30)];
    qualityLabel.text = @"音质";
    qualityLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:qualityLabel];
    _qualityLabel = qualityLabel;
    
    [self initSegmentedControl];
    
    UIView *sepView2 = [[UIView alloc]initWithFrame:CGRectMake(20, 168, SCREEN_WIDTH - 40 , 1)];
    sepView2.backgroundColor = [UIColor colorWithRed:0.938 green:0.938 blue:0.938 alpha:0.08];
    [self addSubview:sepView2];
    
    UILabel *delayLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, 186, 100, 30)];
    delayLabel.text = @"低延时模式";
    delayLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:delayLabel];
    
    self.delaySwitch = [[UISwitch alloc]initWithFrame:CGRectMake(SCREEN_WIDTH - 70, 186, 50, 30)];
    self.delaySwitch.onTintColor = UIColorMakeWithHex(@"#099DFD");
    [self.delaySwitch addTarget:self action:@selector(delayChange:) forControlEvents:UIControlEventValueChanged];
    [self addSubview:_delaySwitch];

}

-(void)perChange:(UISwitch *)voiceSwitch {
    self.qualitySegment.enabled = voiceSwitch.on;
    self.qualitySegment.alpha = 0.8;
    
    if([self.delegate respondsToSelector:@selector(voicePerItemSelectedAction:)]){
        [self.delegate voicePerItemSelectedAction:voiceSwitch.isOn];
    }
}

-(void)delayChange:(UISwitch *)voiceSwitch {
    if([self.delegate respondsToSelector:@selector(voiceDelaySelectedAction:)]){
        [self.delegate voiceDelaySelectedAction:voiceSwitch.isOn];
    }
}

//初始化Segmented控件
- (void)initSegmentedControl
{
    NSArray *segmentedData = [[NSArray alloc]initWithObjects:@"低音质",@"中音质",@"高音质",nil];
    self.qualitySegment = [[UISegmentedControl alloc]initWithItems:segmentedData];
    self.qualitySegment.frame = CGRectMake(SCREEN_WIDTH - 209, 121, 189, 34);
    //这个是设置按下按钮时的颜色
    self.qualitySegment.selectedSegmentTintColor = [UIColor colorWithRed:1 green:1 blue:1 alpha:0.2];
    //默认选中的按钮索引
    self.qualitySegment.selectedSegmentIndex = self.aecGrade;
    self.qualitySegment.backgroundColor = [UIColor colorWithRed:0.938 green:0.938 blue:0.938 alpha:0.08];
    NSDictionary *attributes = [NSDictionary dictionaryWithObjectsAndKeys:[UIFont systemFontOfSize:14],NSFontAttributeName,[UIColor whiteColor], NSForegroundColorAttributeName, nil];
    [self.qualitySegment setTitleTextAttributes:attributes forState:UIControlStateNormal];
    NSDictionary *selectedAttributes = [NSDictionary dictionaryWithObject:[UIColor whiteColor] forKey:NSForegroundColorAttributeName];
    [self.qualitySegment setTitleTextAttributes:selectedAttributes forState:UIControlStateSelected];
    //设置分段控件点击相应事件
    [_qualitySegment addTarget:self action:@selector(segmentSelect:)forControlEvents:UIControlEventValueChanged];
    //添加到视图
    [self addSubview:self.qualitySegment];

}

-(void)segmentSelect:(UISegmentedControl *)seg{
    self.aecGrade = seg.selectedSegmentIndex;
    if([self.delegate respondsToSelector:@selector(didAIAECGradeChangedWithIndex:)]){
        [self.delegate didAIAECGradeChangedWithIndex:self.aecGrade];
    }
}

@end
