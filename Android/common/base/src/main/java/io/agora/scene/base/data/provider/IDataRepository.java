package io.agora.scene.base.data.provider;


import java.io.File;

import io.agora.scene.base.data.model.KTVBaseResponse;
import io.agora.scene.base.data.model.MusicModelBase;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

/**
 * 数据仓库接口
 *
 * @author chenhengfei(Aslanchen)
 */
public interface IDataRepository {

    Observable<KTVBaseResponse<MusicModelBase>> getMusic(@NonNull String musicId);

    Completable download(@NonNull File file, @NonNull String url);
}
