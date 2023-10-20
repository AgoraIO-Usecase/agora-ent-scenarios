//
//  ChooseSongInputModel.m
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/26.
//

#import "ChooseSongInputModel.h"

@implementation ChooseSongInputModel
-(instancetype)initWithTitle:(NSString *)title id:(NSString *)id artist:(NSString *)artist imageUrl:(NSString *)imageUrl playCounts:(NSArray<NSNumber *> *)playCounts {
    self = [super init];
    if (self) {
        _title = title;
        _id = id;
        _artist = artist;
        _imageUrl = imageUrl;
        _playCounts = playCounts;
    }
    return self;
}
@end

@implementation SongModel

static NSArray<ChooseSongInputModel *> *songList;

-(void)initialize {
    songList = @[ [[ChooseSongInputModel alloc] initWithTitle:@"勇气大爆发" id:@"6805795303139450" artist:@"贝乐虎；土豆王国小乐队；奶糖乐团" imageUrl:@"https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/CASW1078064.jpg" playCounts:@[@32000, @47000, @81433, @142000, @176000]], [[ChooseSongInputModel alloc] initWithTitle:@"美人鱼" id:@"6625526604232820" artist:@"林俊杰" imageUrl:@"https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/661208.jpg" playCounts:@[@55000, @97000, @150000, @190000, @243000]],
                  [[ChooseSongInputModel alloc] initWithTitle:@"天外来物" id:@"6388433023669520" artist:@"薛之谦" imageUrl:@"https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/CJ1420004109.jpg" playCounts:@[@91000, @129000, @173000, @212000, @251000]],
                  [[ChooseSongInputModel alloc] initWithTitle:@"凄美地" id:@"6625526611288130" artist:@"郭顶" imageUrl:@"https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/126936.jpg" playCounts:@[@44000, @89000, @132000, @192000, @244000]],
                  [[ChooseSongInputModel alloc] initWithTitle:@"一直很安静" id:@"6654550232746660" artist:@"阿桑" imageUrl:@"https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/961853.jpg" playCounts:@[@57000, @76000, @130000, @148000, @210000]],
                  [[ChooseSongInputModel alloc] initWithTitle:@"他不懂" id:@"6625526604594370" artist:@"张杰" imageUrl:@"https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/792885.jpg" playCounts:@[@46000, @81000, @124000, @159000, @207000]],
                  [[ChooseSongInputModel alloc] initWithTitle:@"一路向北" id:@"6357555536291690" artist:@"周杰伦" imageUrl:@"https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/961979.jpg" playCounts:@[@90000, @118000, @194000, @222000, @262000]],
                  [[ChooseSongInputModel alloc] initWithTitle:@"天黑黑" id:@"6246262727285990" artist:@"孙燕姿" imageUrl:@"https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/147907.jpg" playCounts:@[@51000, @85000, @122000, @176000, @223000]],
                  [[ChooseSongInputModel alloc] initWithTitle:@"起风了" id:@"6625526603305730" artist:@"买辣椒也用券" imageUrl:@"https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/385062.jpg" playCounts:@[@63000, @109000, @154000, @194000, @274000]],
                  [[ChooseSongInputModel alloc] initWithTitle:@"这世界那么多人" id:@"6375711121105330" artist:@"莫文蔚" imageUrl:@"https://accpic.sd-rtn.com/pic/release/jpg/3/640_640/CJ1420010039.jpg" playCounts:@[@91000, @147000, @191000, @235000, @295000]] ];
}

-(NSArray<ChooseSongInputModel *> *)songList {
    return songList;
}

-(ChooseSongInputModel *)getRandomGameSong {
    NSInteger randomIndex = arc4random_uniform((uint32_t)songList.count);
    return songList[randomIndex];
}

@end
