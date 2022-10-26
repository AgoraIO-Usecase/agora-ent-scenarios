//
//  VLDiscoveryView.m
//  VoiceOnLine
//

#import "VLDiscoveryView.h"
#import "VLAmusementTCell.h"
#import "VLDiscoveryItemTCell.h"
#import "VLMineTCell.h"

@interface VLDiscoveryView ()
@property (nonatomic, strong) UIImageView *bgImgView;
@property (nonatomic, strong) UILabel *titleLabel;
@property(nonatomic, weak) id <VLDiscoveryViewDelegate>delegate;


@end

@implementation VLDiscoveryView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLDiscoveryViewDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    [self addSubview:self.bgImgView];
    [self addSubview:self.titleLabel];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (section == 0) {
        return 1;
    }else{
        return 4;
    }
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *reuseCell = @"reuse";
    VLMineTCell *cell = [tableView dequeueReusableCellWithIdentifier:reuseCell];
    if (cell == nil) {
        cell = [[VLMineTCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseCell];
    }
//    cell.dict = self.itemsArray[indexPath.row];
    return cell;
}





@end
