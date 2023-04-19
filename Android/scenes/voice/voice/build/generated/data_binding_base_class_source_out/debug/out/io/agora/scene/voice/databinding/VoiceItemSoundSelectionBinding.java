// Generated by view binder compiler. Do not edit!
package io.agora.scene.voice.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import io.agora.scene.voice.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class VoiceItemSoundSelectionBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ConstraintLayout item;

  @NonNull
  public final AppCompatImageView ivSoundSelected;

  @NonNull
  public final LinearLayout llSoundCustomerUsage;

  @NonNull
  public final MaterialCardView mcvSoundSelectionContent;

  @NonNull
  public final MaterialTextView soundDesc;

  @NonNull
  public final MaterialTextView soundName;

  @NonNull
  public final MaterialTextView usage;

  private VoiceItemSoundSelectionBinding(@NonNull ConstraintLayout rootView,
      @NonNull ConstraintLayout item, @NonNull AppCompatImageView ivSoundSelected,
      @NonNull LinearLayout llSoundCustomerUsage,
      @NonNull MaterialCardView mcvSoundSelectionContent, @NonNull MaterialTextView soundDesc,
      @NonNull MaterialTextView soundName, @NonNull MaterialTextView usage) {
    this.rootView = rootView;
    this.item = item;
    this.ivSoundSelected = ivSoundSelected;
    this.llSoundCustomerUsage = llSoundCustomerUsage;
    this.mcvSoundSelectionContent = mcvSoundSelectionContent;
    this.soundDesc = soundDesc;
    this.soundName = soundName;
    this.usage = usage;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static VoiceItemSoundSelectionBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static VoiceItemSoundSelectionBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.voice_item_sound_selection, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static VoiceItemSoundSelectionBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.item;
      ConstraintLayout item = ViewBindings.findChildViewById(rootView, id);
      if (item == null) {
        break missingId;
      }

      id = R.id.ivSoundSelected;
      AppCompatImageView ivSoundSelected = ViewBindings.findChildViewById(rootView, id);
      if (ivSoundSelected == null) {
        break missingId;
      }

      id = R.id.llSoundCustomerUsage;
      LinearLayout llSoundCustomerUsage = ViewBindings.findChildViewById(rootView, id);
      if (llSoundCustomerUsage == null) {
        break missingId;
      }

      id = R.id.mcvSoundSelectionContent;
      MaterialCardView mcvSoundSelectionContent = ViewBindings.findChildViewById(rootView, id);
      if (mcvSoundSelectionContent == null) {
        break missingId;
      }

      id = R.id.sound_desc;
      MaterialTextView soundDesc = ViewBindings.findChildViewById(rootView, id);
      if (soundDesc == null) {
        break missingId;
      }

      id = R.id.sound_name;
      MaterialTextView soundName = ViewBindings.findChildViewById(rootView, id);
      if (soundName == null) {
        break missingId;
      }

      id = R.id.usage;
      MaterialTextView usage = ViewBindings.findChildViewById(rootView, id);
      if (usage == null) {
        break missingId;
      }

      return new VoiceItemSoundSelectionBinding((ConstraintLayout) rootView, item, ivSoundSelected,
          llSoundCustomerUsage, mcvSoundSelectionContent, soundDesc, soundName, usage);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
