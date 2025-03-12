//
//  VLChoosedSongView.m
//  VoiceOnLine
//

#import "VLSBGSongList.h"
#import "VLSBGSongListCell.h"
#import "VLSBGRoomSelSongModel.h"
#import "VLUserCenter.h"
#import "VLMacroDefine.h"
#import "VLURLPathConfig.h"
#import "AppContext+SBG.h"
#import "SBGMacro.h"

@interface VLSBGSongList ()<UITableViewDataSource,UITableViewDelegate>

@property(nonatomic, weak) id <VLSBGSongListDelegate>delegate;

@property (nonatomic, strong) UITableView  *tableView;
@end

@implementation VLSBGSongList

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGSongListDelegate>)delegate {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    self.tableView = [[UITableView alloc]initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, self.height)];
    self.tableView.dataSource = self;
    self.tableView.delegate = self;
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    self.tableView.backgroundColor = UIColorMakeWithHex(@"#152164");
    [self addSubview:self.tableView];
    
}

#pragma mark -- UITableViewDataSource UITableViewDelegate
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.selSongsArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    VL(weakSelf);
    static NSString *reuseCell = @"reuse";
    VLSBGSongListCell *cell = [tableView dequeueReusableCellWithIdentifier:reuseCell];
    if (cell == nil) {
        cell = [[VLSBGSongListCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseCell];
    }
    cell.selSongModel = self.selSongsArray[indexPath.row];
    cell.numberLabel.text = [NSString stringWithFormat:@"%d",(int)(indexPath.row+1)];
    cell.sortBtnClickBlock = ^(VLSBGRoomSelSongModel * _Nonnull model) {
        if (VLUserCenter.user.ifMaster) {
            [weakSelf sortSongEvent:model];
        }
    };
    cell.deleteBtnClickBlock = ^(VLSBGRoomSelSongModel * _Nonnull model) {
        if (model.status == VLSBGSongPlayStatusPlaying) {
            return;
        }
        if (VLUserCenter.user.ifMaster || [VLUserCenter.user.id isEqualToString:cell.selSongModel.userNo]) {
            [weakSelf deleteSongEvent:model];
        }
        
    };
    
    if(VLUserCenter.user.ifMaster) {
        if(indexPath.row == 0) {
            cell.sortBtn.hidden = YES;
        }
        else {
            cell.sortBtn.hidden = NO;
        }
    }
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 76;
}


- (void)sortSongEvent:(VLSBGRoomSelSongModel *)model {
    
    SBGMakeSongTopInputModel* inputModel = [SBGMakeSongTopInputModel new];
    inputModel.songNo = model.songNo;
    inputModel.objectId = model.objectId;
    [[AppContext sbgServiceImp] pinSongWithInput:inputModel
                                      completion:^(NSError * error) {
    }];
}

- (void)deleteSongEvent:(VLSBGRoomSelSongModel *)model {
    SBGRemoveSongInputModel* inputModel = [SBGRemoveSongInputModel new];
    inputModel.songNo = model.songNo;
    inputModel.objectId = model.objectId;
    [[AppContext sbgServiceImp] removeSongWithInput:inputModel
                                         completion:^(NSError * error) {
        if (error != nil) {
            return;
        }
        
    }];
}

- (void)setSelSongsArray:(NSArray *)selSongsArray {
    _selSongsArray = selSongsArray;
    [self.tableView reloadData];
}

@end
