//
//  VLKTVKindsCell.m
//  VoiceOnLine
//

#import "VLKTVKindsCell.h"
#import "AgoraEntScenarios-Swift.h"
@import Masonry;
@interface VLKTVKindsCell()

@property (nonatomic, strong) UIImageView *cImageView;
@property (nonatomic, strong) UILabel *cLabel;

@end

@implementation VLKTVKindsCell

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self.contentView addSubview:self.cImageView];
        [self.contentView addSubview:self.cLabel];
        self.contentView.layer.cornerRadius = 5;
        self.contentView.layer.borderWidth = 2;
        self.contentView.layer.borderColor = [UIColor clearColor].CGColor;
        
        [self.cImageView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.mas_equalTo(self.contentView);
        }];
        
        [self.cLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.centerY.mas_equalTo(self.contentView);
        }];
    }
    return self;
}

- (void)setModel:(VLKTVKindsModel *)model {
    _model = model;
    _cLabel.text = _model.title;
    _cImageView.image = [UIImage ktv_sceneImageWithName:_model.imageName ];
    if (_model.selected) {
        self.contentView.layer.borderColor = [UIColor blueColor].CGColor;
    } else {
        self.contentView.layer.borderColor = [UIColor clearColor].CGColor;
    }
}


- (UILabel *)cLabel {
    if (!_cLabel) {
        _cLabel = [[UILabel alloc] init];
        _cLabel.font = [UIFont systemFontOfSize:13];
        _cLabel.textColor = [UIColor whiteColor];
    }
    return _cLabel;
}

- (UIImageView *)cImageView {
    if (!_cImageView) {
        _cImageView = [[UIImageView alloc] init];
        _cImageView.layer.cornerRadius = 5;
    }
    return _cImageView;
}

@end
