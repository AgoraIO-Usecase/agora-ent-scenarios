//
//  VLVoicePerShowView.m
//  AgoraEntScenarios
//
//  Created by CP on 2023/3/3.
//

#import "VLSBGVoicePerShowView.h"
#import "SBGMacro.h"
@interface VLSBGVoicePerShowView()
@property(nonatomic, weak) id <VLSBGVoicePerShowViewDelegate>delegate;
@property (nonatomic,strong) UISwitch *voiceSwitch;
@end

@implementation VLSBGVoicePerShowView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGVoicePerShowViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        [self layoutUI];
    }
    return self;
}

-(void)setPerSelected:(BOOL)isSelected{
    self.voiceSwitch.on = isSelected;
}

-(void)layoutUI {
    UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-200)*0.5, 20, 200, 22)];
    titleLabel.text = SBGLocalizedString(@"专业主播");
    titleLabel.font = UIFontMake(18);
    titleLabel.textAlignment = NSTextAlignmentCenter;
    titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:titleLabel];
    
    UILabel *headLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, 60, 80, 30)];
    headLabel.text = @"专业模式";
    headLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:headLabel];
    
    self.voiceSwitch = [[UISwitch alloc]initWithFrame:CGRectMake(110, 60, 50, 30)];
    [self.voiceSwitch addTarget:self action:@selector(change:) forControlEvents:UIControlEventValueChanged];
    [self addSubview:_voiceSwitch];
}

-(void)change:(UISwitch *)voiceSwitch {
    if([self.delegate respondsToSelector:@selector(voicePerItemSelectedAction:)]){
        [self.delegate voicePerItemSelectedAction:voiceSwitch.isOn];
    }
}

@end
