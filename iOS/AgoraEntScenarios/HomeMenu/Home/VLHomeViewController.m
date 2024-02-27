//
//  VLHomeViewController.m
//  VoiceOnLine
//

#import "VLHomeViewController.h"
#import "AESMacro.h"
#import <JXCategoryView/JXCategoryView.h>
#import "AgoraEntScenarios-Swift.h"

@import Pure1v1;
@import ShowTo1v1;

@interface VLHomeViewController ()<JXCategoryViewDelegate, JXCategoryListContainerViewDelegate>
@property (nonatomic, strong) NSArray *titles;
@property (nonatomic, strong) JXCategoryTitleView *myCategoryView;
@property (nonatomic, strong) JXCategoryListContainerView *listContainerView;
@property (nonatomic, strong) UIView *naviView;
@property (nonatomic, strong) NSArray<HomeModel *> *dataArray;

@end

@implementation VLHomeViewController
- (UIView *)naviView {
    if (_naviView == nil) {
        _naviView = [[UIView alloc] init];
        _naviView.translatesAutoresizingMaskIntoConstraints = NO;
    }
    return _naviView;
}
- (NSArray<HomeModel *> *)dataArray {
    if (_dataArray == nil) {
        _dataArray = [HomeModel createData];
        for (HomeModel *model in _dataArray) {
            @weakify(self);
            model.vc.changeToNavigationBarAlpha = ^(CGFloat alpha) {
                weak_self.naviView.backgroundColor = [[UIColor colorWithHexString:@"#F5F8FF"] colorWithAlphaComponent:alpha];
            };
        }
    }
    return _dataArray;
}

- (NSArray *)titles {
    if (_titles == nil) {
        NSMutableArray *tempArray = [NSMutableArray array];
        for (HomeModel *model in self.dataArray) {
            [tempArray appendObject:model.title];
        }
        _titles = [tempArray copy];
    }
    return _titles;
}

- (JXCategoryTitleView *)myCategoryView {
    if (_myCategoryView == nil) {
        _myCategoryView = [[JXCategoryTitleView alloc] init];
        _myCategoryView.titles = self.titles;
        _myCategoryView.titleColor = [UIColor colorWithHexString:@"#303553"];
        _myCategoryView.titleSelectedColor = [UIColor blackColor];
        _myCategoryView.titleFont = [UIFont systemFontOfSize:15];
        _myCategoryView.titleSelectedFont = [UIFont systemFontOfSize:17 weight:(UIFontWeightMedium)];
        _myCategoryView.cellSpacing = 16;
        _myCategoryView.delegate = self;
        _myCategoryView.listContainer = self.listContainerView;
        _myCategoryView.titleColorGradientEnabled = YES;
//        _myCategoryView.titleLabelZoomEnabled = YES;
        _myCategoryView.translatesAutoresizingMaskIntoConstraints = NO;
    }
    return _myCategoryView;
}

- (JXCategoryListContainerView *)listContainerView {
    if (_listContainerView == nil) {
        _listContainerView = [[JXCategoryListContainerView alloc] initWithType:(JXCategoryListContainerType_ScrollView) delegate:self];
        _listContainerView.translatesAutoresizingMaskIntoConstraints = NO;
        _listContainerView.backgroundColor = [UIColor clearColor];
        _listContainerView.listCellBackgroundColor = [UIColor clearColor];
        
    }
    return _listContainerView;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [[NetworkManager shared] reportDeviceInfoWithSceneName: @""];
    
    [self setUpUI];
    [self getSceneConfigs];
}

- (void)getSceneConfigs{
    [[VLSceneConfigsNetworkModel new] requestWithCompletion:^(NSError * _Nullable error, id _Nullable data) {
        if([data isKindOfClass:VLSceneConfigsModel.class]) {
            AppContext.shared.sceneConfig = data;
        }
    }];
}

- (void)setUpUI {
    [self.view addSubview:self.naviView];
    [self.naviView.leftAnchor constraintEqualToAnchor:self.view.leftAnchor].active = YES;
    [self.naviView.rightAnchor constraintEqualToAnchor:self.view.rightAnchor].active = YES;
    [self.naviView.topAnchor constraintEqualToAnchor:self.view.topAnchor].active = YES;
    UIEdgeInsets insets = [UIApplication sharedApplication].delegate.window.safeAreaInsets;
    CGFloat navigationBarHeight = self.navigationController.navigationBar.frame.size.height;
    CGFloat totalHeight = navigationBarHeight + insets.top;
    [self.naviView.heightAnchor constraintEqualToConstant: totalHeight].active = YES;
    
    [self.view addSubview:self.myCategoryView];
    [self.myCategoryView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:0].active = YES;
    [self.myCategoryView.bottomAnchor constraintEqualToAnchor:self.naviView.bottomAnchor].active = YES;
    [self.myCategoryView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:0].active = YES;
    [self.myCategoryView.heightAnchor constraintEqualToConstant:44].active = YES;
    
    [self.view addSubview:self.listContainerView];
    [self.listContainerView.bottomAnchor constraintEqualToAnchor:self.view.bottomAnchor].active = YES;
    [self.listContainerView.leftAnchor constraintEqualToAnchor:self.view.leftAnchor].active = YES;
    [self.listContainerView.rightAnchor constraintEqualToAnchor:self.view.rightAnchor].active = YES;
    [self.listContainerView.topAnchor constraintEqualToAnchor:self.naviView.bottomAnchor].active = YES;
}

// 返回列表的数量
- (NSInteger)numberOfListsInlistContainerView:(JXCategoryListContainerView *)listContainerView {
    return self.titles.count;
}
// 根据下标 index 返回对应遵守并实现 `JXCategoryListContentViewDelegate` 协议的列表实例
- (id<JXCategoryListContentViewDelegate>)listContainerView:(JXCategoryListContainerView *)listContainerView initListForIndex:(NSInteger)index {
    return self.dataArray[index].vc;
}

// 点击选中或者滚动选中都会调用该方法。适用于只关心选中事件，不关心具体是点击还是滚动选中的。
- (void)categoryView:(JXCategoryBaseView *)categoryView didSelectedItemAtIndex:(NSInteger)index {
    [self.dataArray[index].vc getScrollToPostion];
}

// 点击选中的情况才会调用该方法
- (void)categoryView:(JXCategoryBaseView *)categoryView didClickSelectedItemAtIndex:(NSInteger)index {
    
}

// 滚动选中的情况才会调用该方法
- (void)categoryView:(JXCategoryBaseView *)categoryView didScrollSelectedItemAtIndex:(NSInteger)index {
    
}

// 正在滚动中的回调
- (void)categoryView:(JXCategoryBaseView *)categoryView scrollingFromLeftIndex:(NSInteger)leftIndex toRightIndex:(NSInteger)rightIndex ratio:(CGFloat)ratio {
    
}

@end
