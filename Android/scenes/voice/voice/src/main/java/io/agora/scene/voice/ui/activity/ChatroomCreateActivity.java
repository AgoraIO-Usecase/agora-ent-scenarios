package io.agora.scene.voice.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import io.agora.CallBack;
import io.agora.scene.voice.ui.widget.encryption.RoomEncryptionInputView;
import io.agora.scene.voice.ui.widget.titlebar.RoomTitleBar;
import io.agora.voice.baseui.BaseActivity;
import io.agora.voice.baseui.general.callback.OnResourceParseCallback;
import io.agora.voice.buddy.tool.MathTools;
import io.agora.voice.buddy.tool.ToastTools;
import io.agora.voice.buddy.config.RouterParams;
import io.agora.voice.buddy.config.RouterPath;
import io.agora.scene.voice.R;
import io.agora.scene.voice.bean.PageBean;
import io.agora.scene.voice.general.repositories.PageRepository;
import io.agora.scene.voice.general.repositories.ProfileManager;
import io.agora.scene.voice.model.RoomViewModel;
import io.agora.voice.imkit.manager.ChatroomHelper;
import io.agora.voice.network.tools.bean.VRUserBean;
import io.agora.voice.network.tools.bean.VRoomInfoBean;

@Route(path = RouterPath.ChatroomCreatePath)
public class ChatroomCreateActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, RoomTitleBar.OnBackPressListener, View.OnClickListener, RoomEncryptionInputView.OnTextChangeListener {

   private RadioGroup mRadioGroup;
   private RoomTitleBar mTitleBar;
   private TabLayout mTableLayout;
   private ViewPager2 mViewPager;
   private TextView mRandom;
   private TextView mNext;
   private EditText mEdRoomName;
   private boolean isPublic = true;
   private TextView mTip;
   private ArrayList<PageBean> data;
   private RoomEncryptionInputView mEditText;
   private ConstraintLayout baseLayout;
   private RoomViewModel chatroomViewModel;
   private ConstraintLayout tipLayout;
   private int roomType;
   private String encryption;
   private String roomName;
   private ConstraintLayout randomLayout;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
      layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS ;
      setNavAndStatusBarTransparent(this);
      super.onCreate(savedInstanceState);
   }

   @Override
   protected void initSystemFit() {
      setFitSystemForTheme(false, "#00000000");
      setStatusBarTextColor(false);
   }


   @Override
   protected int getLayoutId() {
      return R.layout.voice_activity_create_room_layout;
   }

   @Override
   protected void initView(Bundle savedInstanceState) {
      super.initView(savedInstanceState);
      mRadioGroup = findViewById(R.id.radioGroup_gender);
      mTitleBar = findViewById(R.id.title_bar);
      mTableLayout = findViewById(R.id.agora_tab_layout);
      mViewPager = findViewById(R.id.vp_fragment);
      mRandom = findViewById(R.id.random);
      mEdRoomName = findViewById(R.id.ed_room_name);
      mEditText = findViewById(R.id.ed_pwd);
      mTip = findViewById(R.id.input_tip);
      mNext = findViewById(R.id.bottom_next);
      baseLayout = findViewById(R.id.base_layout);
      randomLayout = findViewById(R.id.random_layout);
      tipLayout = findViewById(R.id.tips_layout);
      chickPrivate();
      data = PageRepository.getInstance().getDefaultPageData(this);
   }

   @Override
   protected void initListener() {
      super.initListener();
      mEditText.setOnTextChangeListener(this);
      mRadioGroup.setOnCheckedChangeListener(this);
      mTitleBar.setOnBackPressListener(this);
      mNext.setOnClickListener(this);
      randomLayout.setOnClickListener(this);
      mTableLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
         @Override
         public void onTabSelected(TabLayout.Tab tab) {
            if(tab.getCustomView() != null) {
               TextView title = tab.getCustomView().findViewById(R.id.tab_item_title);
               ViewGroup.LayoutParams layoutParams = title.getLayoutParams();
               layoutParams.height = (int)dip2px(mContext, 26);
               title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
               title.setGravity(Gravity.CENTER);
            }
         }

         @Override
         public void onTabUnselected(TabLayout.Tab tab) {
            if(tab.getCustomView() != null) {
               TextView title = tab.getCustomView().findViewById(R.id.tab_item_title);
               title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            }
         }

         @Override
         public void onTabReselected(TabLayout.Tab tab) {

         }
      });

      mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
         @Override
         public void onPageSelected(int position) {
            roomType = data.get(position).getRoom_type();
            if (roomType == 0){
               mNext.setText(getString(R.string.voice_room_create_next));
            }else {
               mNext.setText(getString(R.string.voice_room_create_go_live));
            }
            mEdRoomName.setText(randomName());
         }
      });

      baseLayout.setOnTouchListener(new View.OnTouchListener() {
         @Override
         public boolean onTouch(View v, MotionEvent event) {
            hideKeyboard();
            return false;
         }
      });
   }

   @Override
   protected void initData() {
      super.initData();
      chatroomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);
      chatroomViewModel.getCreateObservable().observe(this,response -> {
              parseResource(response, new OnResourceParseCallback<VRoomInfoBean>() {
                 @Override
                 public void onSuccess(@Nullable VRoomInfoBean data) {
                    if (null != data && null != data.getRoom()){
                       VRUserBean userinfo = ProfileManager.getInstance().getProfile();
                       Log.d("ChatroomCreateActivity","chat_uid: " + userinfo.getChat_uid());
                       Log.d("ChatroomCreateActivity","im_token: " + userinfo.getIm_token());
                       ChatroomHelper.getInstance().login(userinfo.getChat_uid(), userinfo.getIm_token(), new CallBack() {
                          @Override
                          public void onSuccess() {
                             joinRoom(data);
                          }

                          @Override
                          public void onError(int code, String desc) {
                             mNext.setEnabled(true);
                             dismissLoading();
                          }
                       });
                    }
                 }
              });
         });
      setupWithViewPager();
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      mEditText.rest();
   }

   @Override
   public void onCheckedChanged(RadioGroup group, int checkedId) {
      if (checkedId == R.id.radioButton_private) {
         isPublic = false;
      } else if (checkedId == R.id.radioButton_public) {
         isPublic = true;
      }
      chickPrivate();
   }

   private void setupWithViewPager() {
      mViewPager.setOffscreenPageLimit(1);
      View recyclerView = mViewPager.getChildAt(0);
      if(recyclerView instanceof RecyclerView){
         recyclerView.setPadding(MathTools.dp2px(this,30), 0, MathTools.dp2px(this,30), 0);
         ((RecyclerView) recyclerView).setClipToPadding(false);
      }
      CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
      compositePageTransformer.addTransformer(new MarginPageTransformer(MathTools.dp2px(this,16)));
      mViewPager.setPageTransformer(compositePageTransformer);
      // set adapter
      mViewPager.setAdapter(new RecyclerView.Adapter<ViewHolder>() {

         @NonNull
         @Override
         public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ChatroomCreateActivity.this).inflate(R.layout.voice_create_page_item_layout,parent, false);
            return new ViewHolder(view);
         }

         @Override
         public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
               if (data.get(position).getRoom_type() == 0){
                  holder.mLayout.setBackgroundResource(R.drawable.voice_icon_create_chat_room);
                  holder.mTitle.setText(getString(R.string.voice_room_create_chat_room));
                  holder.mContent.setText(getString(R.string.voice_room_create_chat_room_desc));
               }else if (data.get(position).getRoom_type() == 1){
                  holder.mLayout.setBackgroundResource(R.drawable.voice_icon_create_3d_room);
                  holder.mTitle.setText(getString(R.string.voice_room_create_3d_room));
                  holder.mContent.setText(getString(R.string.voice_room_create_3d_room_desc));
               }
         }

         @Override
         public int getItemCount() {
            return data.size();
         }

      });

      // set TabLayoutMediator
      TabLayoutMediator mediator = new TabLayoutMediator(mTableLayout, mViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
         @Override
         public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
            tab.setCustomView(R.layout.voice_create_tab_item_layout);
            TextView title = tab.getCustomView().findViewById(R.id.tab_item_title);
            title.setText(data.get(position).getTab_title());
         }
      });
      // setup with viewpager2
      mediator.attach();
   }

   public void joinRoom(VRoomInfoBean data){
      mNext.setEnabled(true);
      dismissLoading();
      Log.e("ChatroomCreateActivity","joinChatRoom onSuccess");
      ARouter.getInstance()
              .build(RouterPath.ChatroomPath)
              .withSerializable(RouterParams.KEY_CHATROOM_DETAILS_INFO, data)
              .navigation();
      finish();
   }

   private void chickPrivate(){
       if (isPublic){
          mEditText.setVisibility(View.GONE);
          baseLayout.requestFocus();
          hideKeyboard();
          mTip.setVisibility(View.GONE);
          tipLayout.setVisibility(View.VISIBLE);
       }else {
          mEditText.setVisibility(View.VISIBLE);
          mEditText.setFocusable(true);
          mEditText.setFocusableInTouchMode(true);
          mEditText.requestFocus();
          showKeyboard(mEditText);
          tipLayout.setVisibility(View.GONE);
       }
   }

   @Override
   public void onBackPress(View view) {
      onBackPressed();
   }

   @Override
   public void onClick(View v) {
      if (v.getId() == R.id.bottom_next){
         if (roomType == 1){
            mNext.setEnabled(false);
            showLoading(false);
         }
         encryption =  mEditText.getText().toString().trim();
         roomName = mEdRoomName.getText().toString().trim();
         check();
      }else if (v.getId() == R.id.random_layout){
         mEdRoomName.setText(randomName());
      }
   }

   private void check(){
      if (TextUtils.isEmpty(roomName)){
         ToastTools.show(this,getString(R.string.voice_room_create_empty_name),Toast.LENGTH_LONG);
         mNext.setEnabled(true);
         dismissLoading();
         return;
      }
      if (!isPublic && encryption.length() != 4){
         mTip.setVisibility(View.VISIBLE);
         ToastTools.show(this,getString(R.string.voice_room_create_tips),Toast.LENGTH_LONG);
         mNext.setEnabled(true);
         dismissLoading();
         return;
      }
      mTip.setVisibility(View.GONE);
      if(roomType == 0){
         Intent intent = new Intent(ChatroomCreateActivity.this,ChatroomSoundSelectionActivity.class);
         intent.putExtra(RouterParams.KEY_CHATROOM_CREATE_NAME,roomName);
         intent.putExtra(RouterParams.KEY_CHATROOM_CREATE_IS_PUBLIC,isPublic);
         if (!isPublic){
            intent.putExtra(RouterParams.KEY_CHATROOM_CREATE_ENCRYPTION,encryption);
         }
         intent.putExtra(RouterParams.KEY_CHATROOM_CREATE_ROOM_TYPE,roomType);
         startActivity(intent);
      }else if (roomType ==1){
         createSpatialRoom();
      }
   }

   public void createSpatialRoom(){
      if (isPublic){
         chatroomViewModel.createSpatial(this,roomName,false);
      }else {
         chatroomViewModel.createSpatial(this,roomName,true,encryption);
      }
   }

   public String randomName(){
      String roomName = "";
      SimpleDateFormat m = new SimpleDateFormat("MM");//获取月份
      SimpleDateFormat d = new SimpleDateFormat("dd");//获取分钟
      String month = m.format(new Date());
      String day = d.format(new Date());
      if (roomType == 0){
         roomName = getString(R.string.voice_room_create_chat_room)+"-"+month+day+"-"+Math.round((Math.random()*999)+1);
      }else {
         roomName = getString(R.string.voice_room_create_chat_3d_room)+"-"+month+day+"-"+Math.round((Math.random()*999)+1);
      }
      return roomName;
   }

   @Override
   public void onTextChange(String pwd) {
      if (pwd.length() >= 4){
         hideKeyboard();
      }
   }

   public static class ViewHolder extends RecyclerView.ViewHolder {

      private ConstraintLayout mLayout;
      private TextView mTitle;
      private TextView mContent;
      public ViewHolder(@NonNull View itemView) {
         super(itemView);
         mLayout =  itemView.findViewById(R.id.item_layout);
         mTitle =  itemView.findViewById(R.id.item_title);
         mContent = itemView.findViewById(R.id.item_text);
      }
   }
}
