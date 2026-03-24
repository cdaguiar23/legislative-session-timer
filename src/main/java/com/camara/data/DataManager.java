package com.camara.data;

import com.camara.model.SessionConfig;
import com.camara.model.Vereador;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String DATA_FILE = "vereadores.json";
    private static final String CONFIG_FILE = "config.json";
    private final Gson gson;

    public DataManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void saveVereadores(List<Vereador> vereadores) {
        try (Writer writer = new FileWriter(DATA_FILE)) {
            gson.toJson(vereadores, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Vereador> loadVereadores() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Vereador>>(){}.getType();
            List<Vereador> list = gson.fromJson(reader, listType);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveConfig(SessionConfig config) {
        try (Writer writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SessionConfig loadConfig() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            return new SessionConfig(null);
        }
        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, SessionConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new SessionConfig(null);
        }
    }
}
