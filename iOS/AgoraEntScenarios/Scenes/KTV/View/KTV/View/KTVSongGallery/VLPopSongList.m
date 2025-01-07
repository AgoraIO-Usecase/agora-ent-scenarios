//
//  VLPopChooseSongView.m
//  VoiceOnLine
//

#import "VLPopSongList.h"
#import "VLSelectSongTableItemView.h"
#import "VLSongList.h"
#import "VLHotSpotBtn.h"
@import AgoraCommon;
@interface VLPopSongList ()

@property(nonatomic, weak) id <VLPopSongListDelegate>delegate;

@property (nonatomic, strong) VLHotSpotBtn *dianGeBtn;
@property (nonatomic, strong) VLHotSpotBtn *choosedBtn;
@property (nonatomic, strong) UILabel      *choosedCountLabel;
@property (nonatomic, strong) VLSelectSongTableItemView *selsectSongView;
@property (nonatomic, strong) VLSongList *choosedSongView;

@property (nonatomic, copy) NSString *roomNo;

@property (nonatomic, assign) BOOL ifChorus;
@property (nonatomic, strong) NSArray *selSongsArray;

@property (nonatomic, assign) BOOL isOwner;

@end

@implementation VLPopSongList

- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<VLPopSongListDelegate>)delegate
                   withRoomNo:(NSString *)roomNo
                     ifChorus:(BOOL)ifChorus{
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = UIColorMakeWithHex(@"#152164");
        self.delegate = delegate;
        self.roomNo = roomNo;
        self.ifChorus = ifChorus;
        [self setupView];
    }
    return self;
}

- (void)setupView {
    [self addSubview:self.dianGeBtn];
    [self addSubview:self.choosedBtn];
    [self addSubview:self.choosedCountLabel];
    [self addSubview:self.selsectSongView];
    [self addSubview:self.choosedSongView];
}

#pragma mark --Event
- (void)itemBtnClickEvent:(VLHotSpotBtn *)sender {
    if (sender.tag == 0) {
        self.dianGeBtn.titleLabel.font = UIFontBoldMake(16);
        [self.dianGeBtn setTitleColor:UIColorMakeWithHex(@"#FFFFFF") forState:UIControlStateNormal];
        self.choosedBtn.titleLabel.font = UIFontMake(14);
        [self.choosedBtn setTitleColor:UIColorMakeWithHex(@"#979CBB") forState:UIControlStateNormal];
        self.selsectSongView.hidden = NO;
        self.choosedSongView.hidden = YES;
        [self.dianGeBtn sizeToFit];
    }else{
        self.choosedBtn.titleLabel.font = UIFontBoldMake(16);
        [self.choosedBtn setTitleColor:UIColorMakeWithHex(@"#FFFFFF") forState:UIControlStateNormal];
        self.dianGeBtn.titleLabel.font = UIFontMake(14);
        [self.dianGeBtn setTitleColor:UIColorMakeWithHex(@"#979CBB") forState:UIControlStateNormal];
        self.selsectSongView.hidden = YES;
        self.choosedSongView.hidden = NO;
        [self.choosedBtn sizeToFit];
    }
    
    if ([self.delegate respondsToSelector:@selector(chooseSongView:tabbarDidClick:)]) {
        [self.delegate chooseSongView:self tabbarDidClick:sender.tag];
    }
}

#pragma mark --setter,getter
- (void)setSelSongsArray:(NSArray *)selSongsArray isOwner:(BOOL)isOwner {
    self.selSongsArray = selSongsArray;
    self.isOwner = isOwner;
    if (selSongsArray.count > 0) {
        self.choosedCountLabel.hidden = NO;
    }else{
        self.choosedCountLabel.hidden = YES;
    }

    self.selsectSongView.selSongsArray = selSongsArray;
    [self.choosedSongView setSelSongsArray:selSongsArray isOwner:isOwner];

    self.choosedCountLabel.text = [NSString stringWithFormat:@"%d",(int)selSongsArray.count];
}

- (void)refreshSounds {
    [self.selsectSongView loadDatasWithIfRefresh:false];
}

- (VLHotSpotBtn *)dianGeBtn {
    if (!_dianGeBtn) {
        _dianGeBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(30, 20, 34, 22)];
        [_dianGeBtn setTitle:KTVLocalizedString(@"ktv_order_song") forState:UIControlStateNormal];
        _dianGeBtn.titleLabel.font = UIFontBoldMake(16);
        [_dianGeBtn addTarget:self action:@selector(itemBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
        _dianGeBtn.tag = 0;
        [_dianGeBtn setTitleColor:UIColorMakeWithHex(@"#FFFFFF") forState:UIControlStateNormal];
        [_dianGeBtn sizeToFit];
    }
    return _dianGeBtn;
}

- (VLHotSpotBtn *)choosedBtn {
    if (!_choosedBtn) {
        _choosedBtn = [[VLHotSpotBtn alloc]initWithFrame:CGRectMake(_dianGeBtn.right+28, 20, 34, 22)];
        [_choosedBtn setTitle:KTVLocalizedString(@"ktv_room_chosen_song_list") forState:UIControlStateNormal];
        _choosedBtn.titleLabel.font = UIFontMake(14);
        [_choosedBtn addTarget:self action:@selector(itemBtnClickEvent:) forControlEvents:UIControlEventTouchUpInside];
        _choosedBtn.tag = 1;
        [_choosedBtn setTitleColor:UIColorMakeWithHex(@"#979CBB") forState:UIControlStateNormal];
        [_choosedBtn sizeToFit];
    }
    return _choosedBtn;
}

- (UILabel *)choosedCountLabel {
    if (!_choosedCountLabel) {
        _choosedCountLabel = [[UILabel alloc]initWithFrame:CGRectMake(_choosedBtn.right+3, _choosedBtn.centerY-8, 18, 18)];
        _choosedCountLabel.textColor = UIColorMakeWithHex(@"#EFF4FF");
        _choosedCountLabel.font = UIFontMake(11);
        _choosedCountLabel.layer.cornerRadius = 9;
        _choosedCountLabel.layer.masksToBounds = YES;
        _choosedCountLabel.textAlignment = NSTextAlignmentCenter;
        _choosedCountLabel.backgroundColor = UIColorMakeWithHex(@"#156EF3");
    }
    return _choosedCountLabel;
}

- (VLSelectSongTableItemView *)selsectSongView {
    if (!_selsectSongView) {
        _selsectSongView = [[VLSelectSongTableItemView alloc] initWithFrame:CGRectMake(0, _dianGeBtn.bottom+20, SCREEN_WIDTH, self.height-20-22-20)
                                                                  withRooNo:self.roomNo];
    }
    return _selsectSongView;
}

- (VLSongList *)choosedSongView {
    if (!_choosedSongView) {
        _choosedSongView = [[VLSongList alloc]initWithFrame:CGRectMake(0, _dianGeBtn.bottom+20, SCREEN_WIDTH, self.height-20-22-20)];
        _choosedSongView.hidden = YES;
    }
    return _choosedSongView;
}

@end
