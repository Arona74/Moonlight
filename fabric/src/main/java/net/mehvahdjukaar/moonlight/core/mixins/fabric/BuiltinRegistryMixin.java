package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.mehvahdjukaar.moonlight.api.fluids.fabric.SoftFluidRegistryImpl;
import net.mehvahdjukaar.moonlight.core.map.fabric.MapDataInternalImpl;
import net.minecraft.data.BuiltinRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltinRegistries.class)
public abstract class BuiltinRegistryMixin {

    @Inject(method = "<clinit>", at = @At(value = "INVOKE_ASSIGN", target ="Lnet/minecraft/data/BuiltinRegistries;registerSimple(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/data/BuiltinRegistries$RegistryBootstrap;)Lnet/minecraft/core/Registry;",
    ordinal = 0))
    private static void registerAdditional(CallbackInfo ci){
        SoftFluidRegistryImpl.REG = BuiltinRegistries
                .registerSimple(SoftFluidRegistryImpl.KEY,SoftFluidRegistryImpl::getDefaultValue);

        MapDataInternalImpl.REG = BuiltinRegistries
                .registerSimple(MapDataInternalImpl.KEY, MapDataInternalImpl::getDefaultValue);
    }
}
