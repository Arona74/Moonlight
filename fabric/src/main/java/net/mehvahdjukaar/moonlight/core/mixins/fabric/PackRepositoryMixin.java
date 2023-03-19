package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.mehvahdjukaar.moonlight.api.platform.fabric.PlatHelperImpl;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;


@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {

    @Inject(method = "<init>(Lnet/minecraft/server/packs/PackType;[Lnet/minecraft/server/packs/repository/RepositorySource;)V",
            at = @At("TAIL"))
    private void init(PackType packType, RepositorySource[] repositorySources, CallbackInfo ci) {
        var list = PlatHelperImpl.getAdditionalPacks(packType);
        var newSources = new HashSet<>(((PackRepositoryAccessor) this).getSources());
        list.forEach(l -> {
            newSources.add((infoConsumer, b) -> infoConsumer.accept(l.get()));
        });
        ((PackRepositoryAccessor) this).setSources(newSources);
    }
}
