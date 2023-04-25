package me.blutkrone.rpgcore.hud.editor.index;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EditorIndex<K, Q extends IEditorRoot<K>> {
    // directory prefix to index from
    private final String directory;
    // an index of everything we keep loaded
    private final Map<String, K> indexed = new HashMap<>();
    // classifier of the editor instance
    private final Class<Q> editor_class;
    // a factory accepting a new ID
    private final Supplier<Q> editor_factory;
    // cooldown on running another index attempt
    private long next_index_timestamp;
    // version is incremented whenever something is loaded/updated
    private int version;

    public EditorIndex(String directory, Class<Q> editor_class, Supplier<Q> editor_factory) {
        this.directory = "editor" + File.separator + directory;
        this.editor_class = editor_class;
        this.editor_factory = editor_factory;

        File parent = FileUtil.directory(this.directory);
        if (!parent.exists()) {
            parent.mkdirs();
        }

        Bukkit.getScheduler().runTask(RPGCore.inst(), this::getAll);
    }

    /**
     * A soft get, meaning that we get an object by its ID as an
     * attachment which can be updated in case it was changed in
     * the editor.
     *
     * @param id Identifier to grab
     * @return Attachment wrapping object
     */
    public IndexAttachment<?, K> getSoft(String id) {
        return createAttachment(index -> index.get(id));
    }

    /**
     * Create an attachment which will keep a value cached until
     * the index updates..
     *
     * @param factorize rebuild cached value.
     * @param <B>       type of cached value
     * @return attachment to use
     */
    public <B> IndexAttachment<K, B> createAttachment(Function<EditorIndex<K, ?>, B> factorize) {
        return new IndexAttachment<K, B>(this) {
            @Override
            protected B compute() {
                return factorize.apply(getIndex());
            }
        };
    }

    /**
     * Create an attachment which will keep a value cached until
     * the index updates..
     *
     * @param filter if true will retain value
     * @return attachment to use
     */
    public IndexAttachment<K, List<K>> createFiltered(Predicate<K> filter) {
        return new IndexAttachment<K, List<K>>(this) {
            @Override
            protected List<K> compute() {
                List<K> results = new ArrayList<>();
                for (K value : getIndex().getAll()) {
                    if (filter.test(value)) {
                        results.add(value);
                    }
                }
                return results;
            }
        };
    }

    /**
     * Retrieve the factory which can provide us with clean instances
     * to operate with.
     *
     * @return a factor to provide clean instances
     */
    public Supplier<Q> getEditorFactory() {
        return editor_factory;
    }

    /**
     * Retrieve the directory of the index.
     *
     * @return which directory is the index under.
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Drop all instances and load them again.
     */
    public void reload() {
        // drop all tracked instances
        this.indexed.clear();
        // load them again from the disk
        try {
            for (File item_file : FileUtil.buildAllFiles(FileUtil.directory(this.directory))) {
                String name = item_file.getName();
                if (name.endsWith(".rpgcore")) {
                    get(name.replace(".rpgcore", ""));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // ensure that we incremented version by at least one
        this.version += 1;
    }

    /**
     * Retrieve all elements available, do note that newly added files will
     * be regularly re-indexed - hence caution should be exerted when we do
     * call this method.
     *
     * @return a collection of all elements on the disk.
     */
    public Collection<K> getAll() {
        // search the disk for new items being registered to it
        if (this.next_index_timestamp < System.currentTimeMillis()) {
            try {
                for (File item_file : FileUtil.buildAllFiles(FileUtil.directory(this.directory))) {
                    String name = item_file.getName();
                    if (name.endsWith(".rpgcore")) {
                        get(name.replace(".rpgcore", ""));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.next_index_timestamp = System.currentTimeMillis() + 20000;
        }

        // offer up the items registered to the core
        return this.indexed.values();
    }

    /**
     * Retrieve all elements available, do note that newly added files will
     * be regularly re-indexed - hence caution should be exerted when we do
     * call this method.
     *
     * @return a collection of all elements on the disk.
     */
    public Collection<String> getKeys() {
        // search the disk for new items being registered to it
        if (this.next_index_timestamp < System.currentTimeMillis()) {
            try {
                for (File item_file : FileUtil.buildAllFiles(FileUtil.directory(this.directory))) {
                    String name = item_file.getName();
                    if (name.endsWith(".rpgcore")) {
                        get(name.replace(".rpgcore", ""));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.next_index_timestamp = System.currentTimeMillis() + 20000;
        }

        // offer up the items registered to the core
        return this.indexed.keySet();
    }

    /**
     * Check if a certain object actually is available, do note that if it isn't
     * loaded but the file for it exists we can attempt to create it.
     *
     * @param id which ID to check.
     * @return true if loaded, or a file exists.
     */
    public boolean has(String id) {
        if (this.indexed.containsKey(id)) {
            return true;
        }

        File config_file = FileUtil.file(this.directory, id.toLowerCase() + ".rpgcore");
        return config_file.exists();
    }

    /**
     * Load the given id into an editor object, but do not register it. Update the
     * runtime instance with a call to {@link #update(String, Object)}
     *
     * @param id which ID to load
     * @return an editor object
     */
    public Q edit(String id) {
        // retrieve the editor object associated with the given key
        try {
            File file = FileUtil.file(this.getDirectory(), id + ".rpgcore");

            if (file.exists()) {
                Reader reader = Files.newBufferedReader(file.toPath());
                Q editor = RPGCore.inst().getGsonPretty().fromJson(reader, this.editor_class);
                reader.close();

                editor.setFile(file);
                return editor;
            } else {
                throw new IllegalArgumentException("File " + file.getPath() + " does not exist!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The difference between a create call and a get call is that the
     * create call will allow us to run a consumer on the editor object
     * to manipulate it before compiling it into a runtime core object.
     *
     * @param id        the id to initialize
     * @param initiator the initiator used
     * @return nothing if the id already exists
     */
    public K create(String id, Consumer<Q> initiator) {
        // make sure we can use the given ID
        if (id == null || this.indexed.containsKey(id)) {
            return null;
        }
        // initialize in an appropriate context
        try {
            // retrieve the editor object associated with the given key
            Q editor;
            File config_file = FileUtil.file(this.directory, id.toLowerCase() + ".rpgcore");
            if (config_file.exists()) {
                throw new IllegalArgumentException("ID " + id + " already has been created!");
            } else {
                editor = this.editor_factory.get();
                initiator.accept(editor);
                if (!config_file.getParentFile().exists()) {
                    config_file.getParentFile().mkdirs();
                }
                editor.setFile(config_file);
                editor.save();
            }
            // create and link the editor
            K build = editor.build(id);
            this.indexed.put(id, build);
            return build;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve a certain object bound to a certain ID, do note that
     * if the object is absent we will create it.
     *
     * @param id which ID to allocate.
     * @return the runtime instance of the object.
     */
    public K get(String id) {
        if (id == null) {
            return null;
        }

        return this.indexed.computeIfAbsent(id, (key -> {
            try {
                // retrieve the editor object associated with the given key
                Q editor = null;
                File config_file = FileUtil.file(this.directory, key.toLowerCase() + ".rpgcore");
                if (config_file.exists()) {
                    try {
                        Reader reader = Files.newBufferedReader(config_file.toPath());
                        editor = RPGCore.inst().getGsonPretty().fromJson(reader, this.editor_class);
                        reader.close();
                    } catch (Throwable e) {
                        Bukkit.getLogger().severe("Corrupted file: " + config_file.getPath() + "");
                        e.printStackTrace();
                        editor = editor_factory.get();
                    }
                    editor.setFile(config_file);
                } else {
                    editor = this.editor_factory.get();
                    if (!config_file.getParentFile().exists()) {
                        config_file.getParentFile().mkdirs();
                    }
                    editor.setFile(config_file);
                    editor.save();
                }
                // update our versioning
                this.version += 1;
                // create an actual instance from the editor object
                return editor.build(key);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    /**
     * Base class shared by this index.
     *
     * @return base editor interface.
     */
    public Class<Q> getEditorClass() {
        return editor_class;
    }

    /**
     * Bind a given key to a new value.
     *
     * @param id    the ID to index under.
     * @param value what value to index.
     */
    public void update(String id, K value) {
        this.version += 1;
        this.indexed.put(id, value);
    }

    /**
     * The version is updated whenever the underlying structure
     * has been updated.
     *
     * @return the current version we got.
     */
    public int getVersion() {
        return this.version;
    }
}
