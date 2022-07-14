package net.mehvahdjukaar.cagerium.mixins;

import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Fox.class)
public interface FoxInvoker {

    @Invoker("setSleeping")
    public void invokeSetSleeping(boolean sleeping);
}
