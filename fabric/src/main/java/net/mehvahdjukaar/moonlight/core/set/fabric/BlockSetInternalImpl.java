package net.mehvahdjukaar.moonlight.core.set.fabric;

import net.mehvahdjukaar.moonlight.api.platform.fabric.RegHelperImpl;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.minecraft.core.Registry;

import java.util.*;

public class BlockSetInternalImpl {

    private static boolean hasFilledBlockSets = false;

    public static boolean hasFilledBlockSets() {
        return hasFilledBlockSets;
    }

    public static final Map<Registry<?>,
            Map<Class<? extends BlockType>, LateRegQueue<?, ?>>> QUEUES = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends BlockType, E> void addDynamicRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<E, T> registrationFunction, Class<T> blockType,
            Registry<E> registry) {
        LateRegQueue<T, E> r = (LateRegQueue<T, E>) QUEUES.computeIfAbsent(registry, b -> new LinkedHashMap<>())
                .computeIfAbsent(blockType, b -> new LateRegQueue<>(blockType, registry));
        r.add(registrationFunction);
    }

    public static void registerEntries() {
        BlockSetInternal.initializeBlockSets();
        //init items immediately as this happens after all registries have fired
        BlockSetInternal.getRegistries().forEach(BlockTypeRegistry::onItemInit);


        List<Registry<?>> priority = new ArrayList<>(QUEUES.keySet().stream().toList());
        priority.sort(Comparator.comparingInt(
                RegHelperImpl.REG_PRIORITY::indexOf));
        for(var r : priority) {
            var blockQueue = QUEUES.get(r);
            if (blockQueue != null) {
                for (var e : blockQueue.entrySet()) {
                    e.getValue().registerEntries();
                }
                QUEUES.remove(r);
            }
        }
        hasFilledBlockSets =true;
    }

    private static class LateRegQueue<T extends BlockType, E> {
        final Class<T> blockType;
        final Queue<BlockSetAPI.BlockTypeRegistryCallback<E, T>> queue = new ArrayDeque<>();
        final Registry<E> registry;

        public LateRegQueue(Class<T> blockType, Registry<E> registry) {
            this.blockType = blockType;
            this.registry = registry;
        }

        public void add(BlockSetAPI.BlockTypeRegistryCallback<E, T> callback) {
            queue.add(callback);
        }

        public void registerEntries(){
            queue.forEach(a -> a.accept((n, i) ->
                    Registry.register(registry, n, i), BlockSetAPI.getBlockSet(blockType).getValues()));
        }
    }

}
