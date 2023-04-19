// Generated by view binder compiler. Do not edit!
package io.agora.scene.voice.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import io.agora.scene.voice.R;
import io.agora.scene.voice.ui.widget.encryption.RoomEncryptionInputView;
import io.agora.scene.voice.ui.widget.titlebar.RoomTitleBar;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class VoiceActivityCreateRoomLayoutBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final TabLayout agoraTabLayout;

  @NonNull
  public final ConstraintLayout baseLayout;

  @NonNull
  public final TextView bottomNext;

  @NonNull
  public final ConstraintLayout edArea;

  @NonNull
  public final RoomEncryptionInputView edPwd;

  @NonNull
  public final EditText edRoomName;

  @NonNull
  public final TextView encryption;

  @NonNull
  public final Guideline glHorizontal44;

  @NonNull
  public final ConstraintLayout groupLayout;

  @NonNull
  public final TextView inputTip;

  @NonNull
  public final AppCompatRadioButton radioButtonPrivate;

  @NonNull
  public final AppCompatRadioButton radioButtonPublic;

  @NonNull
  public final RadioGroup radioGroupGender;

  @NonNull
  public final TextView random;

  @NonNull
  public final ConstraintLayout randomLayout;

  @NonNull
  public final TextView tipBottom;

  @NonNull
  public final ImageView tipIcon;

  @NonNull
  public final TextView tipTop;

  @NonNull
  public final ConstraintLayout tipsLayout;

  @NonNull
  public final RoomTitleBar titleBar;

  @NonNull
  public final ViewPager2 vpFragment;

  private VoiceActivityCreateRoomLayoutBinding(@NonNull ConstraintLayout rootView,
      @NonNull TabLayout agoraTabLayout, @NonNull ConstraintLayout baseLayout,
      @NonNull TextView bottomNext, @NonNull ConstraintLayout edArea,
      @NonNull RoomEncryptionInputView edPwd, @NonNull EditText edRoomName,
      @NonNull TextView encryption, @NonNull Guideline glHorizontal44,
      @NonNull ConstraintLayout groupLayout, @NonNull TextView inputTip,
      @NonNull AppCompatRadioButton radioButtonPrivate,
      @NonNull AppCompatRadioButton radioButtonPublic, @NonNull RadioGroup radioGroupGender,
      @NonNull TextView random, @NonNull ConstraintLayout randomLayout, @NonNull TextView tipBottom,
      @NonNull ImageView tipIcon, @NonNull TextView tipTop, @NonNull ConstraintLayout tipsLayout,
      @NonNull RoomTitleBar titleBar, @NonNull ViewPager2 vpFragment) {
    this.rootView = rootView;
    this.agoraTabLayout = agoraTabLayout;
    this.baseLayout = baseLayout;
    this.bottomNext = bottomNext;
    this.edArea = edArea;
    this.edPwd = edPwd;
    this.edRoomName = edRoomName;
    this.encryption = encryption;
    this.glHorizontal44 = glHorizontal44;
    this.groupLayout = groupLayout;
    this.inputTip = inputTip;
    this.radioButtonPrivate = radioButtonPrivate;
    this.radioButtonPublic = radioButtonPublic;
    this.radioGroupGender = radioGroupGender;
    this.random = random;
    this.randomLayout = randomLayout;
    this.tipBottom = tipBottom;
    this.tipIcon = tipIcon;
    this.tipTop = tipTop;
    this.tipsLayout = tipsLayout;
    this.titleBar = titleBar;
    this.vpFragment = vpFragment;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static VoiceActivityCreateRoomLayoutBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static VoiceActivityCreateRoomLayoutBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.voice_activity_create_room_layout, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static VoiceActivityCreateRoomLayoutBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.agora_tab_layout;
      TabLayout agoraTabLayout = ViewBindings.findChildViewById(rootView, id);
      if (agoraTabLayout == null) {
        break missingId;
      }

      ConstraintLayout baseLayout = (ConstraintLayout) rootView;

      id = R.id.bottom_next;
      TextView bottomNext = ViewBindings.findChildViewById(rootView, id);
      if (bottomNext == null) {
        break missingId;
      }

      id = R.id.ed_area;
      ConstraintLayout edArea = ViewBindings.findChildViewById(rootView, id);
      if (edArea == null) {
        break missingId;
      }

      id = R.id.ed_pwd;
      RoomEncryptionInputView edPwd = ViewBindings.findChildViewById(rootView, id);
      if (edPwd == null) {
        break missingId;
      }

      id = R.id.ed_room_name;
      EditText edRoomName = ViewBindings.findChildViewById(rootView, id);
      if (edRoomName == null) {
        break missingId;
      }

      id = R.id.encryption;
      TextView encryption = ViewBindings.findChildViewById(rootView, id);
      if (encryption == null) {
        break missingId;
      }

      id = R.id.glHorizontal44;
      Guideline glHorizontal44 = ViewBindings.findChildViewById(rootView, id);
      if (glHorizontal44 == null) {
        break missingId;
      }

      id = R.id.group_layout;
      ConstraintLayout groupLayout = ViewBindings.findChildViewById(rootView, id);
      if (groupLayout == null) {
        break missingId;
      }

      id = R.id.input_tip;
      TextView inputTip = ViewBindings.findChildViewById(rootView, id);
      if (inputTip == null) {
        break missingId;
      }

      id = R.id.radioButton_private;
      AppCompatRadioButton radioButtonPrivate = ViewBindings.findChildViewById(rootView, id);
      if (radioButtonPrivate == null) {
        break missingId;
      }

      id = R.id.radioButton_public;
      AppCompatRadioButton radioButtonPublic = ViewBindings.findChildViewById(rootView, id);
      if (radioButtonPublic == null) {
        break missingId;
      }

      id = R.id.radioGroup_gender;
      RadioGroup radioGroupGender = ViewBindings.findChildViewById(rootView, id);
      if (radioGroupGender == null) {
        break missingId;
      }

      id = R.id.random;
      TextView random = ViewBindings.findChildViewById(rootView, id);
      if (random == null) {
        break missingId;
      }

      id = R.id.random_layout;
      ConstraintLayout randomLayout = ViewBindings.findChildViewById(rootView, id);
      if (randomLayout == null) {
        break missingId;
      }

      id = R.id.tip_bottom;
      TextView tipBottom = ViewBindings.findChildViewById(rootView, id);
      if (tipBottom == null) {
        break missingId;
      }

      id = R.id.tip_icon;
      ImageView tipIcon = ViewBindings.findChildViewById(rootView, id);
      if (tipIcon == null) {
        break missingId;
      }

      id = R.id.tip_top;
      TextView tipTop = ViewBindings.findChildViewById(rootView, id);
      if (tipTop == null) {
        break missingId;
      }

      id = R.id.tips_layout;
      ConstraintLayout tipsLayout = ViewBindings.findChildViewById(rootView, id);
      if (tipsLayout == null) {
        break missingId;
      }

      id = R.id.title_bar;
      RoomTitleBar titleBar = ViewBindings.findChildViewById(rootView, id);
      if (titleBar == null) {
        break missingId;
      }

      id = R.id.vp_fragment;
      ViewPager2 vpFragment = ViewBindings.findChildViewById(rootView, id);
      if (vpFragment == null) {
        break missingId;
      }

      return new VoiceActivityCreateRoomLayoutBinding((ConstraintLayout) rootView, agoraTabLayout,
          baseLayout, bottomNext, edArea, edPwd, edRoomName, encryption, glHorizontal44,
          groupLayout, inputTip, radioButtonPrivate, radioButtonPublic, radioGroupGender, random,
          randomLayout, tipBottom, tipIcon, tipTop, tipsLayout, titleBar, vpFragment);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
