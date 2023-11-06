//
//  ChooseSongInputModel.h
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/26.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ChooseSongInputModel : NSObject
@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *id;
@property (nonatomic, copy) NSString *artist;
@property (nonatomic, copy) NSString *imageUrl;
@property (nonatomic, strong) NSArray<NSNumber *> *playCounts;
-(instancetype)initWithTitle:(NSString *)title id:(NSString *)id artist:(NSString *)artist imageUrl:(NSString *)imageUrl playCounts:(NSArray<NSNumber *> *)playCounts;
@end

@interface SongModel : NSObject

@property (class, nonatomic, readonly) NSArray<ChooseSongInputModel *> *songList;

-(ChooseSongInputModel *)getRandomGameSong;
@end

NS_ASSUME_NONNULL_END
