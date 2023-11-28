//
//  KTVDebugViewController.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/17.
//

#import "KTVDebugViewController.h"
#import "KTVDebugInfo.h"
#import "KTVDebugManager.h"
#import "AESMacro.h"
@import AgoraRtcKit;

@interface KTVDebugViewController () <UITableViewDelegate, UITableViewDataSource>

@end


@implementation KTVDebugViewController

- (NSArray*)dataArray {
    return [KTVDebugInfo debugDataArray];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    UIButton* backBtn = [UIButton buttonWithType:UIButtonTypeClose];
    [backBtn addTarget:self action:@selector(backAction:) forControlEvents:(UIControlEventTouchUpInside)];
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:backBtn];
    
    UIButton* cleanDumpBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [cleanDumpBtn setTitle:KTVLocalizedString(@"ktv_clear_dump") forState:UIControlStateNormal];
    [cleanDumpBtn setTitleColor:[UIColor redColor] forState:UIControlStateNormal];
    [cleanDumpBtn addTarget:self action:@selector(onClickClearDump) forControlEvents:(UIControlEventTouchUpInside)];
    
    UIButton* cleanBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [cleanBtn setTitle:KTVLocalizedString(@"ktv_clear_log") forState:UIControlStateNormal];
    [cleanBtn setTitleColor:[UIColor redColor] forState:UIControlStateNormal];
    [cleanBtn addTarget:self action:@selector(onClickClearLog) forControlEvents:(UIControlEventTouchUpInside)];
    
    UIButton* exportBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [exportBtn setTitle:KTVLocalizedString(@"ktv_export_log") forState:UIControlStateNormal];
    [exportBtn setTitleColor:[UIColor redColor] forState:UIControlStateNormal];
    [exportBtn addTarget:self action:@selector(onClickExport) forControlEvents:(UIControlEventTouchUpInside)];
    self.navigationItem.rightBarButtonItems = @[
        [[UIBarButtonItem alloc] initWithCustomView:cleanDumpBtn],
        [[UIBarButtonItem alloc] initWithCustomView:cleanBtn],
        [[UIBarButtonItem alloc] initWithCustomView:exportBtn]
    ];
}

- (void)backAction:(id)sender {
    UIResponder* responder = self;
    while (responder.nextResponder != nil) {
        responder = responder.nextResponder;
        if ([responder isKindOfClass:[UINavigationController class]]) {
            break;
        }
    }
    UINavigationController* parentVC = (UINavigationController*)responder;
    if (parentVC.viewControllers.firstObject != self) {
        return;
    }
    [parentVC.view removeFromSuperview];
    [parentVC removeFromParentViewController];
}

- (void)onClickExport {
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
  NSString *cachePath = [paths objectAtIndex:0];
  UIActivityViewController *activityController = [[UIActivityViewController alloc] initWithActivityItems:@[[NSURL fileURLWithPath:cachePath isDirectory:YES]] applicationActivities:nil];
  activityController.modalPresentationStyle = UIModalPresentationFullScreen;
  [self presentViewController:activityController animated:YES completion:nil];
}

- (void)onClickClearLog {
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

- (void)onClickClearDump {
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

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.dataArray count];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 50;
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *reuseCell = @"tablecell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:reuseCell];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseCell];
    }
    
    cell.textLabel.text = [self.dataArray objectAtIndex:indexPath.row][kTitleKey];
    cell.accessoryType = [KTVDebugInfo getSelectedStatusForKey:cell.textLabel.text] ? UITableViewCellAccessoryCheckmark : UITableViewCellAccessoryNone;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    NSDictionary* rowDic = [self.dataArray objectAtIndex:indexPath.row];
    NSString* key = rowDic[kTitleKey];
    BOOL status = ![KTVDebugInfo getSelectedStatusForKey:key];
    [KTVDebugInfo setSelectedStatus:status forKey:key];
    [tableView reloadRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    
    [KTVDebugManager reLoadParamAll];
}

@end
