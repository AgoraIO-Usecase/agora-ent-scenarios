package io.agora.scene.voice.model;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import io.agora.voice.baseui.general.net.Resource;
import io.agora.scene.voice.general.livedatas.SingleSourceLiveData;
import io.agora.scene.voice.general.repositories.ChatroomHandsRepository;
import io.agora.voice.network.tools.bean.VRoomUserBean;

public class RoomInviteViewModel extends AndroidViewModel {
   private ChatroomHandsRepository mRepository;
   private SingleSourceLiveData<Resource<VRoomUserBean>> inviteObservable;

   public RoomInviteViewModel(@NonNull Application application) {
      super(application);
      mRepository = new ChatroomHandsRepository();
      inviteObservable = new SingleSourceLiveData<>();
   }

   public LiveData<Resource<VRoomUserBean>> getInviteObservable(){
      return inviteObservable;
   }


   public void getInviteList(Context context,String roomId,int pageSize,String cursor){
      inviteObservable.setSource(mRepository.getInvitedList(context,roomId,pageSize,cursor));
   }

}
