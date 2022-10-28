package io.agora.scene.base.provider;

import androidx.annotation.NonNull;

import io.agora.scene.base.api.ApiManager;
import io.agora.scene.base.data.model.KTVBaseResponse;
import io.agora.scene.base.data.model.MusicModelBase;
import io.agora.scene.base.data.provider.DataRepositoryImpl;
import io.reactivex.Observable;

public class DataRepositoryImpl2 extends DataRepositoryImpl {
    @Override
    public Observable<KTVBaseResponse<MusicModelBase>> getMusic(@NonNull String musicId) {
        return ApiManager.getInstance().requestSongsDetail(musicId);
    }
}
