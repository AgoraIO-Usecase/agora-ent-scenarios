//
//  VLDebugView.m
//  AgoraEntScenarios
//
//  Created by CP on 2023/3/3.
//

#import "VLDebugView.h"
#import "KTVDebugInfo.h"
@import AgoraCommon;
@interface VLDebugView()
@property(nonatomic, weak) id <VLDebugViewDelegate>delegate;
@property (nonatomic,strong) UISwitch *dumpSwitch;
@property (nonatomic, assign) BOOL isDumpMode;
@property (nonatomic, strong) UITextField *keyTf;
@property (nonatomic, strong) UITextField *valueTf;
@property (nonatomic, strong) NSString *channelName;
@property (nonatomic, strong) NSString *sdkVer;
@end

@implementation VLDebugView

- (instancetype)initWithFrame:(CGRect)frame channelName:(NSString *)name sdkVer:(NSString *)ver isDumpMode:(BOOL)isDumpMode withDelegate:(id<VLDebugViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        self.isDumpMode = isDumpMode;
        self.channelName = name;
        self.sdkVer = ver;
        [self layoutUI];
    }
    return self;
}

-(void)layoutUI {
    
    UILabel *roomMsgLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, 50, 300, 20)];
    roomMsgLabel.text = [NSString stringWithFormat:@"channelName:%@, SDK:%@", self.channelName, self.sdkVer];
    roomMsgLabel.font = UIFontMake(18);
    roomMsgLabel.textAlignment = NSTextAlignmentCenter;
    roomMsgLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:roomMsgLabel];
    
    UILabel *titleLabel = [[UILabel alloc]initWithFrame:CGRectMake((SCREEN_WIDTH-200)*0.5, 20, 200, 22)];
    titleLabel.text = KTVLocalizedString(@"Debug Settings");
    titleLabel.font = UIFontMake(18);
    titleLabel.textAlignment = NSTextAlignmentCenter;
    titleLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:titleLabel];
    
    UILabel *headLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, 90, 150, 30)];
    headLabel.text = KTVLocalizedString(@"ktv_dump");
    headLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:headLabel];
    
    self.dumpSwitch = [[UISwitch alloc]initWithFrame:CGRectMake(SCREEN_WIDTH - 70, 90, 50, 30)];
    self.dumpSwitch.onTintColor = UIColorMakeWithHex(@"#099DFD");
    [self.dumpSwitch addTarget:self action:@selector(dumpModeChange:) forControlEvents:UIControlEventValueChanged];
    self.dumpSwitch.on = self.isDumpMode;
    [self addSubview:self.dumpSwitch];
    
    UIView *sepView3 = [[UIView alloc]initWithFrame:CGRectMake(20, 135, SCREEN_WIDTH - 40 , 1)];
    sepView3.backgroundColor = [UIColor colorWithRed:0.938 green:0.938 blue:0.938 alpha:0.08];
    [self addSubview:sepView3];
    
    UILabel *logLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, 153, 150, 30)];
    logLabel.text = KTVLocalizedString(@"ktv_export_log");
    logLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:logLabel];
    
    UIButton *exportLogBtn = [[UIButton alloc]initWithFrame:CGRectMake(190, 153, 150, 30)];
    [exportLogBtn setBackgroundColor:[UIColor blueColor]];
    [exportLogBtn setTitle:KTVLocalizedString(@"ktv_debug_confirm") forState:UIControlStateNormal];
    [exportLogBtn setFont:[UIFont systemFontOfSize:13]];
    [exportLogBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    exportLogBtn.layer.cornerRadius = 5;
    exportLogBtn.layer.masksToBounds = true;
    [exportLogBtn addTarget:self action:@selector(exportLog) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:exportLogBtn];
    
    UIView *sepView = [[UIView alloc]initWithFrame:CGRectMake(20, 198, SCREEN_WIDTH - 40 , 1)];
    sepView.backgroundColor = [UIColor colorWithRed:0.938 green:0.938 blue:0.938 alpha:0.08];
    [self addSubview:sepView];
    
    UILabel *clearDumpLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, 216, 150, 30)];
    clearDumpLabel.text = KTVLocalizedString(@"ktv_clear_dump");
    clearDumpLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:clearDumpLabel];
    
    UIButton *clearDumpBtn = [[UIButton alloc]initWithFrame:CGRectMake(190, 216, 150, 30)];
    [clearDumpBtn setBackgroundColor:[UIColor blueColor]];
    [clearDumpBtn setTitle:KTVLocalizedString(@"ktv_debug_confirm") forState:UIControlStateNormal];
    clearDumpBtn.layer.cornerRadius = 5;
    clearDumpBtn.layer.masksToBounds = true;
    [clearDumpBtn setFont:[UIFont systemFontOfSize:13]];
    [clearDumpBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [clearDumpBtn addTarget:self action:@selector(clearDump) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:clearDumpBtn];
    
    UIView *sepView2 = [[UIView alloc]initWithFrame:CGRectMake(20, 261, SCREEN_WIDTH - 40 , 1)];
    sepView2.backgroundColor = [UIColor colorWithRed:0.938 green:0.938 blue:0.938 alpha:0.08];
    [self addSubview:sepView2];
    
    UILabel *clearLogLabel = [[UILabel alloc]initWithFrame:CGRectMake(20, 279, 100, 30)];
    clearLogLabel.text = KTVLocalizedString(@"ktv_clear_log");
    clearLogLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
    [self addSubview:clearLogLabel];
    
    UIButton *clearLogBtn = [[UIButton alloc]initWithFrame:CGRectMake(190, 279, 150, 30)];
    [clearLogBtn setBackgroundColor:[UIColor blueColor]];
    [clearLogBtn setTitle:KTVLocalizedString(@"ktv_debug_confirm") forState:UIControlStateNormal];
    clearLogBtn.layer.cornerRadius = 5;
    clearLogBtn.layer.masksToBounds = true;
    [clearLogBtn setFont:[UIFont systemFontOfSize:13]];
    [clearLogBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [clearLogBtn addTarget:self action:@selector(clearLog) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:clearLogBtn];
    
    UIView *sepView4 = [[UIView alloc]initWithFrame:CGRectMake(20, 324, SCREEN_WIDTH - 40 , 1)];
    sepView4.backgroundColor = [UIColor colorWithRed:0.938 green:0.938 blue:0.938 alpha:0.08];
    [self addSubview:sepView4];
    
    NSDictionary *attributes = @{
        NSForegroundColorAttributeName: [UIColor redColor],
        NSFontAttributeName: [UIFont systemFontOfSize:14]
    };

    self.keyTf = [[UITextField alloc]initWithFrame:CGRectMake(20, 340, SCREEN_WIDTH - 40, 20)];
    NSAttributedString *keyPlaceholder = [[NSAttributedString alloc] initWithString:KTVLocalizedString(@"ktv_input_key") attributes:attributes];
    self.keyTf.attributedPlaceholder = keyPlaceholder;
    self.keyTf.textColor = [UIColor whiteColor];
    [self addSubview:self.keyTf];
    
    self.valueTf = [[UITextField alloc]initWithFrame:CGRectMake(20, 375, SCREEN_WIDTH - 40, 20)];
    NSAttributedString *valuePlaceholder = [[NSAttributedString alloc] initWithString:KTVLocalizedString(@"ktv_input_value") attributes:attributes];
    self.valueTf.attributedPlaceholder = valuePlaceholder;
    self.valueTf.textColor = [UIColor whiteColor];
    [self addSubview:self.valueTf];
    
    UIButton *setParamsBtn = [[UIButton alloc]initWithFrame:CGRectMake(20, 410, 200, 30)];
    [setParamsBtn setBackgroundColor:[UIColor blueColor]];
    [setParamsBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    setParamsBtn.layer.cornerRadius = 5;
    setParamsBtn.layer.masksToBounds = true;
    [setParamsBtn setFont:[UIFont systemFontOfSize:13]];
    [setParamsBtn setTitle:KTVLocalizedString(@"ktv_set_params") forState:UIControlStateNormal];
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

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [super touchesBegan:touches withEvent:event];
    [self endEditing:true];
}

@end
