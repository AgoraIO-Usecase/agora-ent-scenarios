//
//  VLRoomPersonView.m
//  VoiceOnLine
//

#import "VLRoomPersonView.h"
#import "VLRoomPersonIteimCCell.h"
#import "VLRoomSeatModel.h"
#import "VLRoomSelSongModel.h"
#import "VLMacroDefine.h"
#import "VLUserCenter.h"
#import "KTVMacro.h"
@import YYCategories;
@import SDWebImage;

@interface VLRoomPersonView ()<UICollectionViewDataSource,UICollectionViewDelegate>

@property(nonatomic, weak) id <VLRoomPersonViewDelegate>delegate;

@property (nonatomic, strong) UICollectionView *personCollectionView;
@end

@implementation VLRoomPersonView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLRoomPersonViewDelegate>)delegate withRTCkit:(AgoraRtcEngineKit *)RTCkit{
    if (self = [super initWithFrame:frame]) {
        self.delegate = delegate;
        self.roomSeatsArray = [[NSArray alloc]init];
        [self setupView];
    }
    return self;
}

- (void)setupView {
    
    UICollectionViewFlowLayout *flowLayOut = [[UICollectionViewFlowLayout alloc]init];
    flowLayOut.scrollDirection = UICollectionViewScrollDirectionVertical;
    
    CGFloat itemW = VLREALVALUE_WIDTH(54);
    CGFloat middleMargin = (SCREEN_WIDTH-2*27-4*itemW)/3.0;
    CGFloat itemH = VLREALVALUE_WIDTH(54)+33;
    flowLayOut.itemSize = CGSizeMake(itemW, itemH);
    flowLayOut.minimumInteritemSpacing = middleMargin;
    flowLayOut.minimumLineSpacing = 15;
    
    self.personCollectionView = [[UICollectionView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, itemH*2+15) collectionViewLayout:flowLayOut];
    self.personCollectionView.dataSource = self;
    self.personCollectionView.delegate = self;
    self.personCollectionView.alwaysBounceVertical = true;
    self.personCollectionView.showsHorizontalScrollIndicator = false;
    self.personCollectionView.showsVerticalScrollIndicator = false;
    self.personCollectionView.backgroundColor = UIColorClear;
    self.personCollectionView.scrollEnabled = NO;
    self.personCollectionView.contentInset = UIEdgeInsetsMake(0, 27, 0, 27);
    if (@available(iOS 11, *)) {
        self.personCollectionView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
    }
    [self.personCollectionView registerClass:[VLRoomPersonIteimCCell class] forCellWithReuseIdentifier:[VLRoomPersonIteimCCell className]];
    [self addSubview:self.personCollectionView];
    
}

- (void)setRoomSeatsArray:(NSArray *)roomSeatsArray {
    _roomSeatsArray = [[NSArray alloc]initWithArray:roomSeatsArray];
    [self.personCollectionView reloadData];
}

- (void)updateIfNeeded
{
    [self.personCollectionView reloadData];
}

#pragma mark - UITableViewDelegate,UITableViewDataSource
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.roomSeatsArray.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    VLRoomPersonIteimCCell *cell = nil;
    cell = [collectionView dequeueReusableCellWithReuseIdentifier:[VLRoomPersonIteimCCell className] forIndexPath:indexPath];
    
    for (UIView *view in cell.videoView.subviews) {
        if (view.tag > viewTag) {
            [view removeFromSuperview];
        }
    }
    VLRoomSeatModel *seatModel = self.roomSeatsArray[indexPath.row];
    
    if (seatModel.name.length > 0) {
        cell.nickNameLabel.text = seatModel.name;
    }else{
        cell.nickNameLabel.text = [NSString stringWithFormat:KTVLocalizedString(@"%d号麦"), (int)indexPath.row + 1];
    }
    if (seatModel.isMaster) {
        cell.avatarImgView.layer.borderWidth = 2.0;
        cell.avatarImgView.layer.borderColor = UIColorMakeWithHex(@"#75ADFF").CGColor;
        cell.roomerImgView.hidden = cell.roomerLabel.hidden = NO;
        cell.nickNameLabel.textColor = UIColorMakeWithHex(@"#DBDAE9");
    }else{
        cell.roomerImgView.hidden = cell.roomerLabel.hidden = YES;
        cell.nickNameLabel.textColor = UIColorMakeWithHex(@"#AEABD0");
        cell.avatarImgView.layer.borderColor = UIColorClear.CGColor;
    }
    cell.roomerLabel.text = KTVLocalizedString(@"房主");
    if (seatModel.headUrl.length > 0) {
        [cell.avatarImgView sd_setImageWithURL:[NSURL URLWithString:seatModel.headUrl]];
    }else{
        cell.avatarImgView.image = [UIImage sceneImageWithName:@"ktv_emptySeat_icon"];
    }
    cell.singingBtn.hidden = !seatModel.isOwner;
    
    cell.muteImgView.hidden = !seatModel.isAudioMuted;
    
    if(seatModel.isJoinedChorus)
        cell.joinChorusBtn.hidden = NO;
    else
        cell.joinChorusBtn.hidden = YES;
    
    if (seatModel.rtcUid == nil) {
        cell.muteImgView.hidden = YES;
        cell.singingBtn.hidden = YES;
        cell.joinChorusBtn.hidden = YES;
    }
    
    //only display when rtcUid exists (on mic seat), and video is not muted
    cell.videoView.hidden = !(seatModel.rtcUid != nil && !seatModel.isVideoMuted);
    //avatar or camera will only be displayed 1 at atime
    cell.avatarImgView.hidden = !cell.videoView.isHidden;
    if (!seatModel.isVideoMuted && seatModel.rtcUid != nil) { //开启了视频
        [self.delegate onVLRoomPersonView:self onRenderVideo:seatModel inView:cell.videoView atIndex:indexPath.row];
    }
    
    return cell;
}


//- (AgoraRtcChannelMediaOptions *)mediaOption {
//    if (!_mediaOption) {
//        _mediaOption = [[AgoraRtcChannelMediaOptions alloc] init];
//        _mediaOption.autoSubscribeAudio = [AgoraRtcBoolOptional of:YES];
//        _mediaOption.autoSubscribeVideo = [AgoraRtcBoolOptional of:YES];
//    }
//    return _mediaOption;
//}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    VLRoomSeatModel *roomSeatModel = self.roomSeatsArray[indexPath.row];
    if (self.delegate && [self.delegate respondsToSelector:@selector(onVLRoomPersonView:seatItemTappedWithModel:atIndex:)]) {
        [self.delegate onVLRoomPersonView:self seatItemTappedWithModel:roomSeatModel atIndex:indexPath.row];
    }
}

- (void)updateSingBtnWithChoosedSongArray:(NSArray *)choosedSongArray {
    BOOL hasChanged = NO;
    if (choosedSongArray.count > 0) {
        VLRoomSelSongModel *songModel = choosedSongArray.firstObject;
        for (VLRoomSeatModel *seatModel in self.roomSeatsArray) {
            BOOL isOwner = [seatModel.userNo isEqualToString:songModel.userNo];
            if (isOwner != seatModel.isOwner) {
                seatModel.isOwner = isOwner;
                hasChanged = YES;
            }
            if (seatModel.isJoinedChorus) {
                seatModel.isJoinedChorus = NO;
                hasChanged = YES;
            }
        }
    }else{
        for (VLRoomSeatModel *seatModel in self.roomSeatsArray) {
            if (seatModel.isOwner) {
                seatModel.isOwner = NO;
                hasChanged = YES;
            }
            if (seatModel.isJoinedChorus) {
                seatModel.isJoinedChorus = NO;
                hasChanged = YES;
            }
        }
    }
    
    if (!hasChanged) {
        return;
    }
    [self.personCollectionView reloadData];
}


@end
