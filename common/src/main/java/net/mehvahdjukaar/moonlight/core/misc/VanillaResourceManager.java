package net.mehvahdjukaar.moonlight.core.misc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.Closeable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

//resource manager that only contains vanilla stuff
public class VanillaResourceManager implements ResourceManager, Closeable {

    private final PackType TYPE = PackType.CLIENT_RESOURCES;
    private final Map<String, PackResources> packs = new HashMap<>();

    public VanillaResourceManager(PackRepository repository) {
        var v = repository.getPack("vanilla");
        if (v != null) packs.put("vanilla", v.open());
        var m = repository.getPack("mod_resources");
        if (m != null) packs.put("mod_resources", m.open());
    }

    @Override
    public Set<String> getNamespaces() {
        return packs.keySet();
    }

    @Override
    public Stream<PackResources> listPacks() {
        return this.packs.values().stream();
    }

    @Override
    public Optional<Resource> getResource(ResourceLocation location) {
        for (var p : packs.values()) {
            var res = p.getResource(TYPE, location);
            if (res != null) return Optional.of(new Resource(p, res));
        }
        return Optional.empty();
    }

    @Override
    public void close() {
        this.packs.values().forEach(PackResources::close);
    }


    //old getResources
    @Override
    public List<Resource> getResourceStack(ResourceLocation resourceLocation) {
        return List.of();
    }

    @Override
    public Map<ResourceLocation, Resource> listResources(String path, Predicate<ResourceLocation> filter) {
        return Collections.emptyMap();
    }

    //old listResources
    @Override
    public Map<ResourceLocation, List<Resource>> listResourceStacks(String string, Predicate<ResourceLocation> filter) {
        return Collections.emptyMap();
    }
}
