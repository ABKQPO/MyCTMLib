package com.github.wohaopa.MyCTMLib.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 记录资源重载全流程（正常与异常），供 dump_debug_load 命令写入 config/ctmlib_debug_load.json。
 */
@SideOnly(Side.CLIENT)
public class ResourceLoadTrace {

    private static final ResourceLoadTrace INSTANCE = new ResourceLoadTrace();

    private final List<LoadTraceEntry> entries = new ArrayList<>();

    public static ResourceLoadTrace getInstance() {
        return INSTANCE;
    }

    public static final class LoadTraceEntry {
        public final String phase;
        public final String location;
        public final String path;
        public final boolean success;
        public final String message;
        public final String exceptionClass;
        public final String exceptionMessage;
        public final JsonArray stackTrace;

        public LoadTraceEntry(String phase, String location, String path, boolean success,
            String message, String exceptionClass, String exceptionMessage, JsonArray stackTrace) {
            this.phase = phase;
            this.location = location;
            this.path = path;
            this.success = success;
            this.message = message;
            this.exceptionClass = exceptionClass;
            this.exceptionMessage = exceptionMessage;
            this.stackTrace = stackTrace;
        }
    }

    public synchronized void add(String phase, String location, String path, boolean success) {
        add(phase, location, path, success, null, null);
    }

    public synchronized void add(String phase, String location, String path, boolean success, String message) {
        add(phase, location, path, success, message, null);
    }

    public synchronized void add(String phase, String location, String path, boolean success, String message,
        Throwable t) {
        String excClass = null;
        String excMsg = null;
        JsonArray stack = null;
        if (t != null) {
            excClass = t.getClass().getName();
            excMsg = t.getMessage() != null ? t.getMessage() : "";
            stack = new JsonArray();
            for (StackTraceElement el : t.getStackTrace()) {
                stack.add(new JsonPrimitive(el.toString()));
            }
        }
        entries.add(new LoadTraceEntry(phase, location, path, success, message, excClass, excMsg, stack));
    }

    public synchronized void clear() {
        entries.clear();
    }

    public synchronized void flushToFile(File file) {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("timestamp", Instant.now().toString());
            JsonArray arr = new JsonArray();
            for (LoadTraceEntry e : entries) {
                JsonObject obj = new JsonObject();
                obj.addProperty("phase", e.phase);
                obj.addProperty("location", e.location);
                if (e.path != null && !e.path.isEmpty()) obj.addProperty("path", e.path);
                obj.addProperty("success", e.success);
                if (e.message != null && !e.message.isEmpty()) obj.addProperty("message", e.message);
                if (e.exceptionClass != null) {
                    JsonObject exc = new JsonObject();
                    exc.addProperty("exceptionClass", e.exceptionClass);
                    exc.addProperty("message", e.exceptionMessage != null ? e.exceptionMessage : "");
                    if (e.stackTrace != null) exc.add("stackTrace", e.stackTrace);
                    obj.add("exception", exc);
                }
                arr.add(obj);
            }
            root.add("entries", arr);
            file.getParentFile().mkdirs();
            try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root, w);
            }
        } catch (Exception e) {
            com.github.wohaopa.MyCTMLib.MyCTMLib.LOG.warn("ResourceLoadTrace flush failed", e);
        }
    }
}
