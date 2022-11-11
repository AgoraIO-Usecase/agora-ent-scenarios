package io.agora.scene.voice.model;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import io.agora.voice.baseui.general.net.Resource;
import io.agora.scene.voice.general.livedatas.SingleSourceLiveData;
import io.agora.scene.voice.general.repositories.ChatroomHandsRepository;
import io.agora.voice.network.tools.bean.VRMicListBean;

public class RoomRaisedViewModel extends AndroidViewModel {
   private ChatroomHandsRepository mRepository;
   private SingleSourceLiveData<Resource<VRMicListBean>> raisedObservable;

   public RoomRaisedViewModel(@NonNull Application application) {
      super(application);
      mRepository = new ChatroomHandsRepository();
      raisedObservable = new SingleSourceLiveData<>();
   }

   public LiveData<Resource<VRMicListBean>> getRaisedObservable(){
      return raisedObservable;
   }

   public void getRaisedList(Context context,String roomId,int pageSize,String cursor){
      raisedObservable.setSource(mRepository.getRaisedList(context,roomId,pageSize,cursor));
   }
}
