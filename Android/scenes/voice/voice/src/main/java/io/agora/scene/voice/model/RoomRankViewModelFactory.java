package io.agora.scene.voice.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class RoomRankViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        try {
            Constructor<T> constructor = modelClass.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Cannot create an instance of " + modelClass, e);
        }
    }
}
