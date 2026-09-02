package com.flansmod.utils;

import java.util.*;

public final class ConfigMap extends HashMap<String, ArrayList<String>> {
    private final HashMap<String, ArrayList<String>> defaultMap = new HashMap<>();

    public String packName = "";
    public String fileName = "";

    public void setIdentifiers(String pack, String file) {
        packName = pack;
        fileName = file;
    }

    public boolean containsKey(String key) {
        return defaultMap.containsKey(key.toLowerCase());
    }

    public String get(String key) {
        // Get last item instead of first, like Flans did before..
        ArrayList<String> configList = defaultMap.get(key.toLowerCase());
        if (configList != null && !configList.isEmpty()) {
            return configList.get(configList.size() - 1);
        }

        return null;
    }

    public ArrayList<String> getAll(String key) {
        if (defaultMap.containsKey(key.toLowerCase())) {
            return defaultMap.get(key.toLowerCase());
        }

        return new ArrayList<String>();
    }

    public ArrayList<String> put(String key, String value) {
        return putValues(key, Collections.singletonList(value));
    }

    public ArrayList<String> put(String key, ArrayList<String> inValues) {
        return putValues(key, inValues);
    }

    private ArrayList<String> putValues(String key, List<String> valuesToAdd) {
        key = key.toLowerCase();
        ArrayList<String> values;
        if (defaultMap.containsKey(key)) {
            values = defaultMap.get(key);
        } else {
            values = new ArrayList<>();
        }
        values.addAll(valuesToAdd);
        return defaultMap.put(key, values);
    }

    public int size() {
        return defaultMap.size();
    }

    public Set<Map.Entry<String,ArrayList<String>>> entrySet() {
        return defaultMap.entrySet();
    }

    public Collection<ArrayList<String>> values() {
        return defaultMap.values();
    }

    public int hashCode() {
        return defaultMap.hashCode();
    }

    public boolean isEmpty() {
        return defaultMap.isEmpty();
    }

    public boolean equals(Object o) {
        return defaultMap.equals(o);
    }
}

