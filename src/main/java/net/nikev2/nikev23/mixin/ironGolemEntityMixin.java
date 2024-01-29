package net.nikev2.nikev23.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import net.nikev2.nikev23.Config;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(IronGolemEntity.class)
public abstract class ironGolemEntityMixin extends GolemEntity implements Angerable {

    @Shadow public abstract @Nullable UUID getAngryAt();

    protected ironGolemEntityMixin(EntityType<? extends GolemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    public boolean IsNitwitVillager(Entity entity){
        boolean val=false;

        if (entity instanceof VillagerEntity villager){
         val=villager.getVillagerData().getProfession().equals(VillagerProfession.NITWIT);
        }

        return val;
    }

    @Inject(method = "initGoals",at=@At(value = "HEAD"))

    ///Makes iron golems attack nitwits

    void OnInit(CallbackInfo ci){

        IronGolemEntity ironGolem=(IronGolemEntity) (Object) this;
        targetSelector.add(3, new ActiveTargetGoal<>(ironGolem, MobEntity.class, 5, false, false, entity -> {
            boolean val = false;
            if (entity instanceof Monster && !(entity instanceof CreeperEntity)) {
                val = true;
            } else if (IsNitwitVillager(entity)&&Config.IronGolemsAttackNitwits) {
                val = true;

            }

            return val;
        }));
    }


}
