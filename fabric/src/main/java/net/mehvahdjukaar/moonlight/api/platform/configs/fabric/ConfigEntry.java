package net.mehvahdjukaar.moonlight.api.platform.configs.fabric;

import com.google.gson.JsonObject;

public abstract class ConfigEntry{

    protected final String name;

    protected ConfigEntry(String name) {
        this.name = name;
    }

    public abstract void loadFromJson(JsonObject object);

    public abstract void saveToJson(JsonObject object);

    public String getName() {
        return name;
    }
}