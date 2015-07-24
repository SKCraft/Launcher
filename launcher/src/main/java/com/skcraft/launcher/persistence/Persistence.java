/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.persistence;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.Lf2SpacesIndenter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.Closer;
import com.google.common.io.Files;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.WeakHashMap;
import java.util.logging.Level;

/**
 * Simple persistence framework that can read an object from a file, bind
 * that object to that file, and allow any code having a reference to the
 * object make changes to the object and save those changes back to disk.
 * </p>
 * For example:
 * <pre>config = Persistence.load(file, Configuration.class);
 * config.changeSomething();
 * Persistence.commit(config);</pre>
 */
@Log
public final class Persistence {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final WeakHashMap<Object, ByteSink> bound = new WeakHashMap<Object, ByteSink>();
    public static final DefaultPrettyPrinter L2F_LIST_PRETTY_PRINTER;

    static {
        L2F_LIST_PRETTY_PRINTER = new DefaultPrettyPrinter();
        L2F_LIST_PRETTY_PRINTER.indentArraysWith(Lf2SpacesIndenter.instance);
    }

    private Persistence() {
    }

    /**
     * Bind an object to a path where the object will be saved.
     *
     * @param object the object
     * @param sink the byte sink
     */
    public static void bind(@NonNull Object object, @NonNull ByteSink sink) {
        synchronized (bound) {
            bound.put(object, sink);
        }
    }

    /**
     * Save an object to file.
     *
     * @param object the object
     * @throws java.io.IOException on save error
     */
    public static void commit(@NonNull Object object) throws IOException {
        ByteSink sink;
        synchronized (bound) {
            sink = bound.get(object);
            if (sink == null) {
                throw new IOException("Cannot persist unbound object: " + object);
            }
        }

        Closer closer = Closer.create();
        try {
            OutputStream os = closer.register(sink.openBufferedStream());
            mapper.writeValue(os, object);
        } finally {
            closer.close();
        }
    }

    /**
     * Save an object to file, and send all errors to the log.
     *
     * @param object the object
     */
    public static void commitAndForget(@NonNull Object object)  {
        try {
            commit(object);
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to save " + object.getClass() + ": " + object.toString(), e);
        }
    }

    /**
     * Read an object from a byte source, without binding it.
     *
     * @param source byte source
     * @param cls the class
     * @param returnNull true to return null if the object could not be loaded
     * @param <V> the type of class
     * @return an object
     */
    public static <V> V read(ByteSource source, Class<V> cls, boolean returnNull) {
        V object;
        Closer closer = Closer.create();

        try {
            object = mapper.readValue(closer.register(source.openBufferedStream()), cls);
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException)) {
                log.log(Level.INFO, "Failed to load" + cls.getCanonicalName(), e);
            }

            if (returnNull) {
                return null;
            }

            try {
                object = cls.newInstance();
            } catch (InstantiationException e1) {
                throw new RuntimeException(
                        "Failed to construct object with no-arg constructor", e1);
            } catch (IllegalAccessException e1) {
                throw new RuntimeException(
                        "Failed to construct object with no-arg constructor", e1);
            }
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
            }
        }

        return object;
    }

    /**
     * Read an object from file, without binding it.
     *
     * @param file the file
     * @param cls the class
     * @param returnNull true to return null if the object could not be loaded
     * @param <V> the type of class
     * @return an object
     */
    public static <V> V read(File file, Class<V> cls, boolean returnNull) {
        return read(Files.asByteSource(file), cls, returnNull);
    }


    /**
     * Read an object from file, without binding it.
     *
     * @param file the file
     * @param cls the class
     * @param <V> the type of class
     * @return an object
     */
    public static <V> V read(File file, Class<V> cls) {
        return read(file, cls, false);
    }

    /**
     * Read an object from file.
     *
     * @param file the file
     * @param cls the class
     * @param returnNull true to return null if the object could not be loaded
     * @param <V> the type of class
     * @return an object
     */
    public static <V> V load(File file, Class<V> cls, boolean returnNull) {
        ByteSource source = Files.asByteSource(file);
        ByteSink sink = new MkdirByteSink(Files.asByteSink(file), file.getParentFile());

        Scrambled scrambled = cls.getAnnotation(Scrambled.class);
        if (cls.getAnnotation(Scrambled.class) != null) {
            source = new ScramblingSourceFilter(source, scrambled.value());
            sink = new ScramblingSinkFilter(sink, scrambled.value());
        }

        V object = read(source, cls, returnNull);
        Persistence.bind(object, sink);
        return object;
    }

    /**
     * Read an object from file.
     *
     * <p>If the file does not exist or loading fails, construct a new instance of
     * the given class by using its no-arg constructor.</p>
     *
     * @param file the file
     * @param cls the class
     * @param <V> the type of class
     * @return an object
     */
    public static <V> V load(File file, Class<V> cls) {
        return load(file, cls, false);
    }

    /**
     * Write an object to file.
     *
     * @param file the file
     * @param object the object
     * @throws java.io.IOException on I/O error
     */
    public static void write(File file, Object object) throws IOException {
        write(file, object, null);
    }

    /**
     * Write an object to file.
     *
     * @param file the file
     * @param object the object
     * @param prettyPrinter a pretty printer to use, or null
     * @throws java.io.IOException on I/O error
     */
    public static void write(File file, Object object, PrettyPrinter prettyPrinter) throws IOException {
        file.getParentFile().mkdirs();
        if (prettyPrinter != null) {
            mapper.writer(prettyPrinter).writeValue(file, object);
        } else {
            mapper.writeValue(file, object);
        }
    }

    /**
     * Write an object to a string.
     *
     * @param object the object
     * @param prettyPrinter a pretty printer to use, or null
     * @throws java.io.IOException on I/O error
     */
    public static String writeValueAsString(Object object, PrettyPrinter prettyPrinter) throws IOException {
        if (prettyPrinter != null) {
            return mapper.writer(prettyPrinter).writeValueAsString(object);
        } else {
            return mapper.writeValueAsString(object);
        }
    }

}
