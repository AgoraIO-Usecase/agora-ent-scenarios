//
//  CantataPlugin.m
//  AFNetworking
//
//  Created by wushengtao on 2023/8/28.
//

#import "CantataPlugin.h"
#import "VLDHCOnLineListVC.h"

@implementation CantataPlugin
+(UIViewController *)getCantataRootViewController{
    VLDHCOnLineListVC *vc = [[VLDHCOnLineListVC alloc]init];
    return vc;
}

@end
