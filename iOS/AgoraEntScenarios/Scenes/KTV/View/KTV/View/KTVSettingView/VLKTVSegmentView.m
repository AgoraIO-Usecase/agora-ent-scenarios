//
//  VLKTVSwitcherView.m
//  VoiceOnLine
//

#import "VLKTVSegmentView.h"
#import "AgoraEntScenarios-Swift.h"
@import Masonry;
@import AgoraCommon;
@interface VLKTVSegmentView()

@property (nonatomic, strong) UILabel *subLabel;
@property (nonatomic, strong) UISegmentedControl *segmentControl;
@property (nonatomic, strong) UIView *lineView;
@end

@implementation VLKTVSegmentView


- (instancetype)init {
    if (self = [super init]) {
        [self initSubViews];
        [self addSubViewConstraints];
    }
    return self;
}

- (void)initSubViews {
    self.lineView = [UIView new];
    self.lineView.backgroundColor = [UIColor separatorColor];
    [self addSubview:self.lineView];
    
    [self addSubview:self.subLabel];
    self.subLabel.font = [UIFont systemFontOfSize:15];
    self.subLabel.textColor = [UIColor whiteColor];
    self.subLabel.numberOfLines = 0;
    [self addSubview:self.segmentControl];
}

- (void)addSubViewConstraints {
    
    [self.segmentControl mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.mas_equalTo(self).offset(-8);
        make.centerY.mas_equalTo(self);
        make.width.mas_equalTo(@(150));
        make.height.mas_equalTo(@(30));
    }];
    
    [self.subLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.mas_equalTo(self);
        make.height.mas_equalTo(@(40));
        make.left.mas_equalTo(self.mas_left).offset(20);
    }];
    
    [self.lineView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.bottom.equalTo(self);
        make.left.equalTo(self).offset(20);
        make.right.equalTo(self).offset(-10);
        make.height.mas_equalTo(@(1));
    }];
    
}

- (void)setSelectIndex:(NSInteger)selectIndex {
    _selectIndex = selectIndex;
    self.segmentControl.selectedSegmentIndex = selectIndex;
}

- (void)setSubText:(NSString *)subText attrText:(NSString *)attrText {
    if (!attrText || [attrText isEqualToString:@""]) {
        self.subLabel.text = subText;
    } else {
        NSString *fullText = [NSString stringWithFormat:@"%@\n%@", subText, attrText];
        NSMutableAttributedString *attributedText = [[NSMutableAttributedString alloc] initWithString:fullText];

        // 设置前面文字的样式
        NSDictionary *firstTextAttributes = @{NSFontAttributeName: [UIFont boldSystemFontOfSize:15], NSForegroundColorAttributeName: [UIColor whiteColor]};
        [attributedText setAttributes:firstTextAttributes range:NSMakeRange(0, subText.length + 1)]; // +1 是为了包含换行符

        // 设置后面文字的样式
        NSDictionary *secondTextAttributes = @{NSFontAttributeName: [UIFont italicSystemFontOfSize:12], NSForegroundColorAttributeName: [UIColor lightGrayColor]};
        [attributedText setAttributes:secondTextAttributes range:NSMakeRange(subText.length + 1, attrText.length)];

        self.subLabel.attributedText = attributedText;
    }
}

- (void)setType:(SegmentViewType)type {
    NSArray *segmentedData;
    switch (type) {
        case SegmentViewTypeScore:
            segmentedData = @[KTVLocalizedString(@"ktv_lrc_low"), KTVLocalizedString(@"ktv_lrc_mid"), KTVLocalizedString(@"ktv_lrc_high")];
            [self.segmentControl mas_updateConstraints:^(MASConstraintMaker *make) {
                make.width.mas_equalTo(@(200));
            }];
            break;
        case SegmentViewTypeVQS:
            segmentedData = @[KTVLocalizedString(@"ktv_normal_vol"), KTVLocalizedString(@"ktv_high_vol")];
            [self.segmentControl mas_updateConstraints:^(MASConstraintMaker *make) {
                make.width.mas_equalTo(@(140));
            }];
            break;
        case SegmentViewTypeAns:
            segmentedData = @[KTVLocalizedString(@"ktv_close_aec"), KTVLocalizedString(@"ktv_aec_mid"), KTVLocalizedString(@"ktv_aec_high")];
            [self.segmentControl mas_updateConstraints:^(MASConstraintMaker *make) {
                make.width.mas_equalTo(@(120));
            }];
            break;
        default:
            break;
    }

    for (NSString *title in segmentedData) {
        [self.segmentControl insertSegmentWithTitle:title atIndex:self.segmentControl.numberOfSegments animated:YES];
    }
}

- (UILabel *)subLabel {
    if (!_subLabel) {
        _subLabel = [[UILabel alloc] init];
        _subLabel.font = [UIFont systemFontOfSize:12];
        _subLabel.textColor = UIColorMakeWithHex(@"#6C7192");
    }
    return _subLabel;
}

-(UISegmentedControl *)segmentControl {
    if(!_segmentControl){
        //这个是设置按下按钮时的颜色
        self.segmentControl = [[UISegmentedControl alloc]init];
        self.segmentControl.selectedSegmentTintColor = [UIColor colorWithRed:1 green:1 blue:1 alpha:0.2];
        self.segmentControl.backgroundColor = [UIColor colorWithRed:0.938 green:0.938 blue:0.938 alpha:0.08];
        NSDictionary *attributes = [NSDictionary dictionaryWithObjectsAndKeys:[UIFont systemFontOfSize:13],NSFontAttributeName,[UIColor whiteColor], NSForegroundColorAttributeName, nil];
        [self.segmentControl setTitleTextAttributes:attributes forState:UIControlStateNormal];
        NSDictionary *selectedAttributes = [NSDictionary dictionaryWithObject:[UIColor whiteColor] forKey:NSForegroundColorAttributeName];
        [self.segmentControl setTitleTextAttributes:selectedAttributes forState:UIControlStateSelected];
        //设置分段控件点击相应事件
        [_segmentControl addTarget:self action:@selector(volSegmentSelect:)forControlEvents:UIControlEventValueChanged];
    }
    return _segmentControl;
}


-(void)volSegmentSelect:(UISegmentedControl *)control {
    self.selectIndex = control.selectedSegmentIndex;
    if([_delegate respondsToSelector:@selector(segmentView:DidSelectIndex:)]){
        [self.delegate segmentView:self DidSelectIndex:self.selectIndex];
    }
}

@end
