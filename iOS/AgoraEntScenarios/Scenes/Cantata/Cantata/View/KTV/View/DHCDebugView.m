//
//  VLDebugView.m
//  AgoraEntScenarios
//
//  Created by CP on 2023/3/3.
//

#import "DHCDebugView.h"
#import "AESMacro.h"
#import "VLMacroDefine.h"
#import "KTVDebugInfo.h"
@interface DHCDebugView()
@property(nonatomic, weak) id <DHCDebugViewDelegate>delegate;
@property (nonatomic,strong) UISwitch *dumpSwitch;
@property (nonatomic, assign) BOOL isDumpMode;
@property (nonatomic, strong) UITextField *keyTf;
@property (nonatomic, strong) UITextField *valueTf;
@end

@implementation DHCDebugView

- (instancetype)initWithFrame:(CGRect)frame isDumpMode:(BOOL)isDumpMode withDelegate:(id<DHCDebugViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        self.isDumpMode = isDumpMode;
        [self layoutUI];
    }
    return self;
}

-(void)layoutUI {
    UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-200)*0.5, 20, 200, 22)];
    titleLabel.text = DHCLocalizedString(@"Debug Settings");
    titleLabel.font = UIFontMake(18);
    titleLabel.textAlignment = NSTextAlignmentCenter;
    titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:titleLabel];
    
    UILabel *headLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, 60, 150, 30)];
    headLabel.text = @"ktv_dump".toSceneLocalization;
    headLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:headLabel];
    
    self.dumpSwitch = [[UISwitch alloc]initWithFrame:CGRectMake(SCREEN_WIDTH - 70, 60, 50, 30)];
    self.dumpSwitch.onTintColor = UIColorMakeWithHex(@"#099DFD");
    [self.dumpSwitch addTarget:self action:@selector(dumpModeChange:) forControlEvents:UIControlEventValueChanged];
    self.dumpSwitch.on = self.isDumpMode;
    [self addSubview:self.dumpSwitch];
    
    UIView *sepView3 = [[UIView alloc]initWithFrame:CGRectMake(20, 105, SCREEN_WIDTH - 40 , 1)];
    sepView3.backgroundColor = [UIColor colorWithRed:0.938 green:0.938 blue:0.938 alpha:0.08];
    [self addSubview:sepView3];
    
    UILabel *logLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, 123, 150, 30)];
    logLabel.text = @"ktv_export_log".toSceneLocalization;
    logLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:logLabel];
    
    UIButton *exportLogBtn = [[UIButton alloc]initWithFrame:CGRectMake(190, 123, 150, 30)];
    [exportLogBtn setBackgroundColor:[UIColor blueColor]];
    [exportLogBtn setTitle:@"ktv_debug_confirm".toSceneLocalization forState:UIControlStateNormal];
    [exportLogBtn setFont:[UIFont systemFontOfSize:13]];
    [exportLogBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    exportLogBtn.layer.cornerRadius = 5;
    exportLogBtn.layer.masksToBounds = true;
    [exportLogBtn addTarget:self action:@selector(exportLog) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:exportLogBtn];
    
    UIView *sepView = [[UIView alloc]initWithFrame:CGRectMake(20, 168, SCREEN_WIDTH - 40 , 1)];
    sepView.backgroundColor = [UIColor colorWithRed:0.938 green:0.938 blue:0.938 alpha:0.08];
    [self addSubview:sepView];
    
    UILabel *clearDumpLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, 186, 150, 30)];
    clearDumpLabel.text = @"ktv_clear_dump".toSceneLocalization;
    clearDumpLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:clearDumpLabel];
    
    UIButton *clearDumpBtn = [[UIButton alloc]initWithFrame:CGRectMake(190, 186, 150, 30)];
    [clearDumpBtn setBackgroundColor:[UIColor blueColor]];
    [clearDumpBtn setTitle:@"ktv_debug_confirm".toSceneLocalization forState:UIControlStateNormal];
    clearDumpBtn.layer.cornerRadius = 5;
    clearDumpBtn.layer.masksToBounds = true;
    [clearDumpBtn setFont:[UIFont systemFontOfSize:13]];
    [clearDumpBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [clearDumpBtn addTarget:self action:@selector(clearDump) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:clearDumpBtn];
    
    UIView *sepView2 = [[UIView alloc]initWithFrame:CGRectMake(20, 231, SCREEN_WIDTH - 40 , 1)];
    sepView2.backgroundColor = [UIColor colorWithRed:0.938 green:0.938 blue:0.938 alpha:0.08];
    [self addSubview:sepView2];
    
    UILabel *clearLogLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, 249, 100, 30)];
    clearLogLabel.text = @"ktv_clear_log".toSceneLocalization;
    clearLogLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:clearLogLabel];
    
    UIButton *clearLogBtn = [[UIButton alloc]initWithFrame:CGRectMake(190, 249, 150, 30)];
    [clearLogBtn setBackgroundColor:[UIColor blueColor]];
    [clearLogBtn setTitle:@"ktv_debug_confirm".toSceneLocalization forState:UIControlStateNormal];
    clearLogBtn.layer.cornerRadius = 5;
    clearLogBtn.layer.masksToBounds = true;
    [clearLogBtn setFont:[UIFont systemFontOfSize:13]];
    [clearLogBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [clearLogBtn addTarget:self action:@selector(clearLog) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:clearLogBtn];
    
    UIView *sepView4 = [[UIView alloc]initWithFrame:CGRectMake(20, 294, SCREEN_WIDTH - 40 , 1)];
    sepView4.backgroundColor = [UIColor colorWithRed:0.938 green:0.938 blue:0.938 alpha:0.08];
    [self addSubview:sepView4];
    
    NSDictionary *attributes = @{
        NSForegroundColorAttributeName: [UIColor redColor],
        NSFontAttributeName: [UIFont systemFontOfSize:14]
    };

    self.keyTf = [[UITextField alloc]initWithFrame:CGRectMake(20, 310, SCREEN_WIDTH - 40, 20)];
    NSAttributedString *keyPlaceholder = [[NSAttributedString alloc] initWithString:@"ktv_input_key".toSceneLocalization attributes:attributes];
    self.keyTf.attributedPlaceholder = keyPlaceholder;
    self.keyTf.textColor = [UIColor whiteColor];
    [self addSubview:self.keyTf];
    
    self.valueTf = [[UITextField alloc]initWithFrame:CGRectMake(20, 345, SCREEN_WIDTH - 40, 20)];
    NSAttributedString *valuePlaceholder = [[NSAttributedString alloc] initWithString:@"ktv_input_value".toSceneLocalization attributes:attributes];
    self.valueTf.attributedPlaceholder = valuePlaceholder;
    self.valueTf.textColor = [UIColor whiteColor];
    [self addSubview:self.valueTf];
    
    UIButton *setParamsBtn = [[UIButton alloc]initWithFrame:CGRectMake(20, 380, 200, 30)];
    [setParamsBtn setBackgroundColor:[UIColor blueColor]];
    [setParamsBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    setParamsBtn.layer.cornerRadius = 5;
    setParamsBtn.layer.masksToBounds = true;
    [setParamsBtn setFont:[UIFont systemFontOfSize:13]];
    [setParamsBtn setTitle:@"ktv_set_params".toSceneLocalization forState:UIControlStateNormal];
    [setParamsBtn addTarget:self action:@selector(setParams) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:setParamsBtn];
}

-(void)dumpModeChange:(UISwitch *)swich{
    if([_delegate respondsToSelector:@selector(didDumpModeChanged:)]){
        [self.delegate didDumpModeChanged:swich.isOn];
    }
}

-(void)exportLog{
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
  NSString *cachePath = [paths objectAtIndex:0];
  if([_delegate respondsToSelector:@selector(didExportLogWith:)]){
      [self.delegate didExportLogWith:cachePath];
  }
}

-(void)clearDump{
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
  NSString *cachePath = [paths objectAtIndex:0];
  NSFileManager *fileManager = [NSFileManager defaultManager];
  if ([fileManager fileExistsAtPath:cachePath]) {
      NSArray *childrenFiles = [fileManager subpathsAtPath:cachePath];
      for (NSString *fileName in childrenFiles) {
          if([fileName hasSuffix:@".pcm"] || [fileName hasSuffix:@".wav"]) {
              NSString *absolutePath = [cachePath stringByAppendingPathComponent:fileName];
              [fileManager removeItemAtPath:absolutePath error:nil];
              NSLog(@"remove dump path:%@", absolutePath);
          }
      }
  }
}

-(void)clearLog{
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
  NSString *cachePath = [paths objectAtIndex:0];
  NSFileManager *fileManager = [NSFileManager defaultManager];
  if ([fileManager fileExistsAtPath:cachePath]) {
      NSArray *childrenFiles = [fileManager subpathsAtPath:cachePath];
      for (NSString *fileName in childrenFiles) {
          if([fileName hasSuffix:@".log"]) {
              NSString *absolutePath = [cachePath stringByAppendingPathComponent:fileName];
              [fileManager removeItemAtPath:absolutePath error:nil];
              NSLog(@"remove log path:%@", absolutePath);
          }
      }
  }
}

-(void)setParams{
    if([_delegate respondsToSelector:@selector(didParamsSetWith:value:)]){
        [self.delegate didParamsSetWith:self.keyTf.text value:self.valueTf.text];
    }
}

@end
