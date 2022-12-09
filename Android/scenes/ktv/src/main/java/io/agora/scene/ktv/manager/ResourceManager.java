package io.agora.scene.ktv.manager;

import android.content.Context;

import androidx.annotation.NonNull;

import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.agora.scene.base.bean.MemberMusicModel;
import io.agora.scene.base.data.model.KTVBaseResponse;
import io.agora.scene.base.data.model.MusicModelBase;
import io.agora.scene.base.data.provider.DataRepository;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Music Resource
 *
 * @author chenhengfei(Aslanchen)
 * @date 2021/06/01
 */
public final class ResourceManager {
    private Logger.Builder mLogger = XLog.tag("MusicRes");

    private volatile static ResourceManager instance;

    private String resourceRoot;

    public static volatile boolean isPreparing = false;

    private ResourceManager(Context mContext) {
        resourceRoot = mContext.getExternalCacheDir().getPath();
    }

    public static ResourceManager Instance(Context mContext) {
        if (instance == null) {
            synchronized (ResourceManager.class) {
                if (instance == null)
                    instance = new ResourceManager(mContext.getApplicationContext());
            }
        }
        return instance;
    }

    public Single<MemberMusicModel> download(final MemberMusicModel musicModel, boolean onlyLrc) {
        return DataRepository.Instance()
                .getMusic(musicModel.songNo)
                .firstOrError()
                .retry(3)
                .flatMap(new Function<KTVBaseResponse<MusicModelBase>, SingleSource<MemberMusicModel>>() {
                    @Override
                    public SingleSource<MemberMusicModel> apply(@NonNull KTVBaseResponse<MusicModelBase> model) throws Exception {
                        musicModel.songUrl = (model.getData().data.playUrl);
                        musicModel.lyric = model.getData().data.lyric;

                        File fileMusic = new File(resourceRoot, musicModel.songNo);
                        File fileLrc;

                        if (model.getData().data.lyric.endsWith("zip")) {
                            fileLrc = new File(resourceRoot, musicModel.songNo + ".zip");
                        } else if (model.getData().data.lyric.endsWith("xml")) {
                            fileLrc = new File(resourceRoot, musicModel.songNo + ".xml");
                        } else if (model.getData().data.lyric.endsWith("lrc")) {
                            fileLrc = new File(resourceRoot, musicModel.songNo + ".lrc");
                        } else {
                            return Single.error(new Throwable("未知歌词格式"));
                        }

                        musicModel.fileMusic = (fileMusic);
                        musicModel.fileLrc = (fileLrc);

                        mLogger.i("prepareMusic down %s", musicModel);
                        if (onlyLrc) {
                            Completable mCompletable = DataRepository.Instance().download(fileLrc, musicModel.lyric);
                            if (model.getData().data.lyric.endsWith("zip")) {
                                mCompletable = mCompletable.andThen(Completable.create(new CompletableOnSubscribe() {
                                    @Override
                                    public void subscribe(@NonNull CompletableEmitter emitter) throws Exception {
                                        File fileLrcNew = new File(resourceRoot, musicModel.songNo + ".xml");
                                        unzipLrc(fileLrc, fileLrcNew);
                                        musicModel.fileLrc = (fileLrcNew);
                                        emitter.onComplete();
                                    }
                                }));
                            }

                            return mCompletable.andThen(Single.just(musicModel));
                        } else {
                            Completable mCompletable = DataRepository.Instance().download(fileLrc, musicModel.lyric);
                            if (model.getData().data.lyric.endsWith("zip")) {
                                mCompletable = mCompletable.andThen(Completable.create(new CompletableOnSubscribe() {
                                    @Override
                                    public void subscribe(@NonNull CompletableEmitter emitter) throws Exception {
                                        File fileLrcNew = new File(resourceRoot, musicModel.songNo + ".xml");
                                        unzipLrc(fileLrc, fileLrcNew);
                                        musicModel.fileLrc = (fileLrcNew);
                                        emitter.onComplete();
                                    }
                                }));
                            }

                            return Completable.mergeArray(
                                    DataRepository.Instance().download(fileMusic, musicModel.songUrl),
                                    mCompletable)
                                    .toSingle(new Callable<MemberMusicModel>() {
                                        @Override
                                        public MemberMusicModel call() throws Exception {
                                            return musicModel;
                                        }
                                    });
                        }
                    }
                }).doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        isPreparing = true;
                    }
                }).doOnSuccess(new Consumer<MemberMusicModel>() {
                    @Override
                    public void accept(MemberMusicModel musicModel) throws Exception {
                        isPreparing = false;
                    }
                }).doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        isPreparing = false;
                        mLogger.e("prepareMusic error", throwable);
                    }
                });
    }

    private void unzipLrc(File src, File des) throws Exception {
        mLogger.i("prepareMusic unzipLrc %s", des);

        ZipInputStream inZip = new ZipInputStream(new FileInputStream(src));
        ZipEntry zipEntry;
        String szName = null;

        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                continue;
            } else {
                des.createNewFile();
                FileOutputStream out = new FileOutputStream(des);
                int len;
                byte[] buffer = new byte[1024];
                while ((len = inZip.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        inZip.close();
    }
}
