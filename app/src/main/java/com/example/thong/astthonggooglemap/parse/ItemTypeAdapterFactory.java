package com.example.thong.astthonggooglemap.parse;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ItemTypeAdapterFactory implements TypeAdapterFactory{

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        final TypeAdapter<JsonElement> elementAdapter = gson
                .getAdapter(JsonElement.class);

        return new TypeAdapter<T>() {

            public void write(JsonWriter out, T value) throws IOException {
                delegate.write(out, value);
            }

            public T read(JsonReader in) throws IOException {

                JsonElement jsonElement = elementAdapter.read(in);
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();

                    if(jsonObject != null && jsonObject.has("response")) {

                        JsonElement jsonElementResponse = jsonObject.get("response");

                        if (jsonElementResponse.isJsonObject()) {
                            JsonObject jsonObjectResponse = jsonElementResponse.getAsJsonObject();
                            if(jsonObjectResponse != null && jsonObjectResponse.has("venues")) {
                                jsonElement = jsonObjectResponse.get("venues");
                            }
                        }
                    }
                }
                return delegate.fromJsonTree(jsonElement);
            }
        }.nullSafe();
    }
}
