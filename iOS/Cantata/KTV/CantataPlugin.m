//
//  CantataPlugin.m
//  AFNetworking
//
//  Created by wushengtao on 2023/8/28.
//

#import "CantataPlugin.h"
#import "VLOnLineListVC.h"

@implementation CantataPlugin
+(UIViewController *)getCantataRootViewCOntroller{
    VLOnLineListVC *vc = [[VLOnLineListVC alloc]init];
    return vc;
}
@end
