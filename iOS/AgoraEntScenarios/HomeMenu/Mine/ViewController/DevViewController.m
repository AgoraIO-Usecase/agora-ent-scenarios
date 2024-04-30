//
//  DevViewController.m
//  AgoraEntScenarios
//
//  Created by CP on 2024/4/8.
//

#import "DevViewController.h"
@import LEEAlert;
#import "MenuUtils.h"
#import "AgoraEntScenarios-Swift.h"
@interface DevViewController ()

@end

@implementation DevViewController


- (void)viewDidLoad {
    [super viewDidLoad];
    self.title = AGLocalizedString(@"app_debug_mode");
    self.view.backgroundColor = [UIColor whiteColor];
    [self layoutUI];
}

- (void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear: animated];
    self.navigationController.navigationBar.hidden = false;
}

- (void)layoutUI {
    // 创建一个包含 segment 和 button 的自定义视图
    UIView *customView = [[UIView alloc] initWithFrame:CGRectMake(20, 80, 300, 50)];
    
    NSInteger index = 0;
    NSNumber *toolboxEnvValue = [NSUserDefaults.standardUserDefaults objectForKey:@"TOOLBOXENV"];
    if (toolboxEnvValue && [toolboxEnvValue isKindOfClass:[NSNumber class]]) {
        index = toolboxEnvValue.integerValue;
    }
    
    // 创建 segment 控件
    UISegmentedControl *segment = [[UISegmentedControl alloc] initWithItems:@[@"release", @"debug"]];
    segment.frame = CGRectMake(40, 5, 150, 40);
    segment.selectedSegmentIndex = index;
    [segment addTarget:self action:@selector(segChanged:) forControlEvents:UIControlEventValueChanged];
    [customView addSubview:segment];
    
    // 创建 button 控件
    UIButton *button = [UIButton buttonWithType:UIButtonTypeSystem];
    [button setTitle:AGLocalizedString(@"confirm") forState:UIControlStateNormal];
    [button setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    button.frame = CGRectMake(200, 5, 60, 40);
    button.layer.cornerRadius = 5;
    button.layer.masksToBounds = true;
    button.backgroundColor = [UIColor blueColor];
    [button addTarget:self action:@selector(confirm) forControlEvents:UIControlEventTouchUpInside];
    [customView addSubview:button];
    
    [self.view addSubview:customView];
    
    UIButton *subBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    subBtn.frame = CGRectMake(40, UIScreen.mainScreen.bounds.size.height - 100, UIScreen.mainScreen.bounds.size.width - 80, 60);
    [subBtn setTitle:AGLocalizedString(@"exit_app_debug_mode") forState:UIControlStateNormal];
    subBtn.backgroundColor = [UIColor blueColor];
    subBtn.layer.cornerRadius = 10;
    subBtn.layer.masksToBounds = true;
    [subBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [subBtn addTarget:self action:@selector(leaveDev) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:subBtn];
}

- (void)segChanged:(UISegmentedControl *)seg {
    NSInteger index = seg.selectedSegmentIndex;
    [[NSUserDefaults standardUserDefaults] setValue:@(index) forKey:@"TOOLBOXENV"];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

- (void)confirm {
    exit(0);
}

- (void)leaveDev {
    [self closeOffDebugMode];
}

- (void)closeOffDebugMode {
    
    [LEEAlert alert].config
    .LeeAddTitle(^(UILabel *label) {
        label.text = AGLocalizedString(@"app_exit_debug");
        label.textColor = UIColorMakeWithHex(@"#040925");
        label.font = UIFontBoldMake(16);
    })
   // .LeeCustomView(customView)
    .LeeContent(AGLocalizedString(@"app_exit_debug_tip"))
    .LeeAddAction(^(LEEAction *action) {
        VL(weakSelf);
        action.type = LEEActionTypeCancel;
        action.title = AGLocalizedString(@"confirm");
        action.titleColor = UIColorMakeWithHex(@"#000000");
        action.backgroundColor = UIColorMakeWithHex(@"#EFF4FF");
        action.borderColor = UIColorMakeWithHex(@"#EFF4FF");
        action.cornerRadius = 20;
        action.height = 40;
        action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
        action.font = UIFontBoldMake(16);
        action.clickBlock = ^{
            [AppContext shared].isDebugMode = NO;
            dispatch_async(dispatch_get_main_queue(), ^{
                // 在主线程中执行UI刷新操作
                [weakSelf.navigationController popViewControllerAnimated:false];
            });
        };
    })
    .LeeAddAction(^(LEEAction *action) {
        action.type = LEEActionTypeCancel;
        action.title = AGLocalizedString(@"cancel");
        action.titleColor = UIColorMakeWithHex(@"#FFFFFF");
        action.backgroundColor = UIColorMakeWithHex(@"#2753FF");
        action.cornerRadius = 20;
        action.height = 40;
        action.font = UIFontBoldMake(16);
        action.insets = UIEdgeInsetsMake(10, 20, 20, 20);
        action.borderColor = UIColorMakeWithHex(@"#2753FF");
        action.clickBlock = ^{
            // 取消点击事件Block
        };
    })
    .LeeShow();
}

@end
