// Generated by view binder compiler. Do not edit!
package io.agora.scene.voice.spatial.databinding;

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
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.textview.MaterialTextView;
import io.agora.scene.voice.spatial.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class VoiceSpatialItemRoomSoundSelectionBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final AppCompatImageView ivSoundSelected;

  @NonNull
  public final AppCompatImageView ivSoundSelectionToggle;

  @NonNull
  public final LinearLayout llSoundCustomerUsage;

  @NonNull
  public final LinearLayout llSoundSelectionTips;

  @NonNull
  public final MaterialCardView mcvSoundSelectionContent;

  @NonNull
  public final MaterialDivider mdSoundSelectionDivider;

  @NonNull
  public final MaterialTextView mtSoundSelectionContent;

  @NonNull
  public final MaterialTextView mtSoundSelectionCurrentName;

  @NonNull
  public final MaterialTextView mtSoundSelectionCustomerUsage;

  @NonNull
  public final MaterialTextView mtSoundSelectionName;

  private VoiceSpatialItemRoomSoundSelectionBinding(@NonNull ConstraintLayout rootView,
      @NonNull AppCompatImageView ivSoundSelected,
      @NonNull AppCompatImageView ivSoundSelectionToggle,
      @NonNull LinearLayout llSoundCustomerUsage, @NonNull LinearLayout llSoundSelectionTips,
      @NonNull MaterialCardView mcvSoundSelectionContent,
      @NonNull MaterialDivider mdSoundSelectionDivider,
      @NonNull MaterialTextView mtSoundSelectionContent,
      @NonNull MaterialTextView mtSoundSelectionCurrentName,
      @NonNull MaterialTextView mtSoundSelectionCustomerUsage,
      @NonNull MaterialTextView mtSoundSelectionName) {
    this.rootView = rootView;
    this.ivSoundSelected = ivSoundSelected;
    this.ivSoundSelectionToggle = ivSoundSelectionToggle;
    this.llSoundCustomerUsage = llSoundCustomerUsage;
    this.llSoundSelectionTips = llSoundSelectionTips;
    this.mcvSoundSelectionContent = mcvSoundSelectionContent;
    this.mdSoundSelectionDivider = mdSoundSelectionDivider;
    this.mtSoundSelectionContent = mtSoundSelectionContent;
    this.mtSoundSelectionCurrentName = mtSoundSelectionCurrentName;
    this.mtSoundSelectionCustomerUsage = mtSoundSelectionCustomerUsage;
    this.mtSoundSelectionName = mtSoundSelectionName;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static VoiceSpatialItemRoomSoundSelectionBinding inflate(
      @NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static VoiceSpatialItemRoomSoundSelectionBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.voice_spatial_item_room_sound_selection, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static VoiceSpatialItemRoomSoundSelectionBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.ivSoundSelected;
      AppCompatImageView ivSoundSelected = ViewBindings.findChildViewById(rootView, id);
      if (ivSoundSelected == null) {
        break missingId;
      }

      id = R.id.ivSoundSelectionToggle;
      AppCompatImageView ivSoundSelectionToggle = ViewBindings.findChildViewById(rootView, id);
      if (ivSoundSelectionToggle == null) {
        break missingId;
      }

      id = R.id.llSoundCustomerUsage;
      LinearLayout llSoundCustomerUsage = ViewBindings.findChildViewById(rootView, id);
      if (llSoundCustomerUsage == null) {
        break missingId;
      }

      id = R.id.llSoundSelectionTips;
      LinearLayout llSoundSelectionTips = ViewBindings.findChildViewById(rootView, id);
      if (llSoundSelectionTips == null) {
        break missingId;
      }

      id = R.id.mcvSoundSelectionContent;
      MaterialCardView mcvSoundSelectionContent = ViewBindings.findChildViewById(rootView, id);
      if (mcvSoundSelectionContent == null) {
        break missingId;
      }

      id = R.id.mdSoundSelectionDivider;
      MaterialDivider mdSoundSelectionDivider = ViewBindings.findChildViewById(rootView, id);
      if (mdSoundSelectionDivider == null) {
        break missingId;
      }

      id = R.id.mtSoundSelectionContent;
      MaterialTextView mtSoundSelectionContent = ViewBindings.findChildViewById(rootView, id);
      if (mtSoundSelectionContent == null) {
        break missingId;
      }

      id = R.id.mtSoundSelectionCurrentName;
      MaterialTextView mtSoundSelectionCurrentName = ViewBindings.findChildViewById(rootView, id);
      if (mtSoundSelectionCurrentName == null) {
        break missingId;
      }

      id = R.id.mtSoundSelectionCustomerUsage;
      MaterialTextView mtSoundSelectionCustomerUsage = ViewBindings.findChildViewById(rootView, id);
      if (mtSoundSelectionCustomerUsage == null) {
        break missingId;
      }

      id = R.id.mtSoundSelectionName;
      MaterialTextView mtSoundSelectionName = ViewBindings.findChildViewById(rootView, id);
      if (mtSoundSelectionName == null) {
        break missingId;
      }

      return new VoiceSpatialItemRoomSoundSelectionBinding((ConstraintLayout) rootView,
          ivSoundSelected, ivSoundSelectionToggle, llSoundCustomerUsage, llSoundSelectionTips,
          mcvSoundSelectionContent, mdSoundSelectionDivider, mtSoundSelectionContent,
          mtSoundSelectionCurrentName, mtSoundSelectionCustomerUsage, mtSoundSelectionName);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
