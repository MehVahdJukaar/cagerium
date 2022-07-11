package net.mehvahdjukaar.cagerium.mixins;

import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Slime.class)
public interface SlimeInvoker {

    @Invoker("setSize")
    public void invokeSetSize(int size, boolean health);
}
