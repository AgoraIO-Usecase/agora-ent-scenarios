package io.agora.scene.joy.network;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DynamicModelRuntimeTypeAdapterFactory<T extends DynamicModel> implements
        TypeAdapterFactory {
    private final Class<?> baseType;
    private final String typeFieldName;
    private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap<>();
    private final Map<Class<?>, String> subtypeToLabel = new LinkedHashMap<>();

    private DynamicModelRuntimeTypeAdapterFactory(Class<?> baseType, String typeFieldName) {
        if (typeFieldName == null || baseType == null) {
            throw new NullPointerException();
        }
        this.baseType = baseType;
        this.typeFieldName = typeFieldName;
    }

    public static <T extends DynamicModel> DynamicModelRuntimeTypeAdapterFactory<?> of(
            Class<T> baseType, String typeFieldName) {
        return new DynamicModelRuntimeTypeAdapterFactory<T>(baseType, typeFieldName);
    }

    public static <T extends DynamicModel> DynamicModelRuntimeTypeAdapterFactory<?> of(
            Class<T> baseType) {
        return new DynamicModelRuntimeTypeAdapterFactory<T>(baseType, "type");
    }

    public DynamicModelRuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type,
                                                                    String code) {
        if (type == null || TextUtils.isEmpty(code)) {
            throw new NullPointerException();
        }
        if (subtypeToLabel.containsKey(type) || labelToSubtype.containsKey(code)) {
            throw new IllegalArgumentException("types and labels must be unique");
        }
        labelToSubtype.put(code, type);
        subtypeToLabel.put(type, code);
        return this;
    }

    public DynamicModelRuntimeTypeAdapterFactory<T> registerSubtype(String code,
                                                                    Class<? extends T> type) {
        return registerSubtype(type, code);
    }

    public DynamicModelRuntimeTypeAdapterFactory<T> registerSubtypeMap(
            Map<String, Class<? extends T>> map) {
        for (Map.Entry<String, Class<? extends T>> entry : map.entrySet()) {
            if (entry.getValue() == null || TextUtils.isEmpty(entry.getKey())) {
                throw new NullPointerException();
            }
            if (subtypeToLabel.containsKey(entry.getKey()) || labelToSubtype
                    .containsKey(entry.getValue())) {
                throw new IllegalArgumentException("types and labels must be unique");
            }
            labelToSubtype.put(entry.getKey(), entry.getValue());
            subtypeToLabel.put(entry.getValue(), entry.getKey());
        }
        return this;
    }

    public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
        if (type.getRawType() != baseType) {
            return null;
        }

        final Map<String, TypeAdapter<?>> labelToDelegate
                = new LinkedHashMap<>();
        final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate
                = new LinkedHashMap<>();
        for (Map.Entry<String, Class<?>> entry : labelToSubtype.entrySet()) {
            TypeAdapter<?> delegate =
                    gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
            labelToDelegate.put(entry.getKey(), delegate);
            subtypeToDelegate.put(entry.getValue(), delegate);
        }

        return new TypeAdapter<R>() {
            @Override
            public R read(JsonReader in) {
                JsonElement jsonElement = Streams.parse(in);
                JsonElement labelJsonElement = jsonElement.getAsJsonObject().get(typeFieldName);
                if (labelJsonElement == null) {
                    throw new JsonParseException("cannot deserialize " + baseType
                            + " because it does not define a field named " + typeFieldName);
                }
                String label = labelJsonElement.getAsString();
                @SuppressWarnings("unchecked") // registration requires that subtype extends T
                TypeAdapter<R> delegate = (TypeAdapter<R>) labelToDelegate.get(label);
                if (delegate == null) {
                    throw new JsonParseException(
                            "cannot deserialize " + baseType + " subtype named "
                                    + label + "; did you forget to register a subtype?");
                }
                return delegate.fromJsonTree(jsonElement);
            }

            @Override
            public void write(JsonWriter out, R value) throws IOException {
                Class<?> srcType = value.getClass();
                String label = subtypeToLabel.get(srcType);
                @SuppressWarnings("unchecked") // registration requires that subtype extends T
                TypeAdapter<R> delegate = (TypeAdapter<R>) subtypeToDelegate.get(srcType);
                if (delegate == null) {
                    throw new JsonParseException("cannot serialize " + srcType.getName()
                            + "; did you forget to register a subtype?");
                }
                JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();

                JsonObject clone = new JsonObject();
                clone.add(typeFieldName, new JsonPrimitive(label));
                for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                    clone.add(e.getKey(), e.getValue());
                }
                Streams.write(clone, out);
            }
        }.nullSafe();
    }

}
