//
//  AEACategoryTitleCell.m
//  AgoraEditAvatar
//
//  Created by FanPengpeng on 2022/9/20.
//

#import "AEACategoryTitleCell.h"


@interface AEACategoryTitleCell ()

@property (strong, nonatomic) UILabel *titleLabel;

@end

@implementation AEACategoryTitleCell

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self setupUI];
    }
    return self;
}

- (void)setupUI {
    _titleLabel = [UILabel new];
    _titleLabel.textColor = [UIColor colorWithRed:0 green:0 blue:0 alpha: 0.5];
    _titleLabel.font = [UIFont systemFontOfSize:16 weight:UIFontWeightMedium];
    _titleLabel.textAlignment = NSTextAlignmentCenter;
    [self.contentView addSubview:_titleLabel];
    
    _titleLabel.translatesAutoresizingMaskIntoConstraints = NO;
    
    [[_titleLabel.leftAnchor constraintEqualToAnchor:self.contentView.leftAnchor  constant:5] setActive:YES];
    [[_titleLabel.rightAnchor constraintEqualToAnchor:self.contentView.rightAnchor constant:-5] setActive:YES];
    [[_titleLabel.topAnchor constraintEqualToAnchor:self.contentView.topAnchor constant:5] setActive:YES];
    [[_titleLabel.bottomAnchor constraintEqualToAnchor:self.contentView.bottomAnchor constant:-5] setActive:YES];
}

- (void)setTitle:(NSString *)title {
    _title = title;
    self.titleLabel.text = title;
    _titleLabel.textColor = self.isSelected ? _titleSelectedColor : _titleColor;
    _titleLabel.font = self.isSelected ? _titleSelectedFont : _titleFont;
}

- (void)setSelected:(BOOL)selected {
    [super setSelected:selected];
    _titleLabel.textColor = selected ? _titleSelectedColor : _titleColor;
    _titleLabel.font = selected ? _titleSelectedFont : _titleFont;
}


@end
