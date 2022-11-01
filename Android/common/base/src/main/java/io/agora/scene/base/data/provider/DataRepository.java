package io.agora.scene.base.data.provider;

import java.io.File;

import io.agora.scene.base.data.model.KTVBaseResponse;
import io.agora.scene.base.data.model.MusicModelBase;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

public class DataRepository implements IDataRepository {
    private volatile static DataRepository instance;

    private IDataRepository mIDataRepository;

    private DataRepository() {
    }

    public void setDataRepositoryImpl(DataRepositoryImpl aDataRepositoryImpl) {
        mIDataRepository = aDataRepositoryImpl;
    }

    public static synchronized DataRepository Instance() {
        if (instance == null) {
            synchronized (DataRepository.class) {
                if (instance == null)
                    instance = new DataRepository();
            }
        }
        return instance;
    }

    @Override
    public Observable<KTVBaseResponse<MusicModelBase>> getMusic(@NonNull String musicId) {
        return mIDataRepository.getMusic(musicId);
    }

    @Override
    public Completable download(@NonNull File file, @NonNull String url) {
        return mIDataRepository.download(file, url);
    }
}
