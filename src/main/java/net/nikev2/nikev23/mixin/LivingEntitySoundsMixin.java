package net.nikev2.nikev23.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.nikev2.nikev23.AgeCalcualtor;
import net.nikev2.nikev23.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySoundsMixin {


    @Inject(method = "getSoundPitch",at=@At("HEAD"), cancellable = true)
    void getSoundPitch(CallbackInfoReturnable<Float> cir){
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof VillagerEntity villager){
            if (villager.isBaby() && villager.getBreedingAge()>=VillagerEntity.BABY_AGE && Config.FixedSoundPitch){
                cir.setReturnValue(AgeCalcualtor.calculatePitchBasedOnAge(villager));
            }
        }
    }
}
