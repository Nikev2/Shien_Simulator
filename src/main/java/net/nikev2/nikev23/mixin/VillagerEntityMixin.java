package net.nikev2.nikev23.mixin;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.poi.PointOfInterestTypes;
import net.nikev2.nikev23.Config;
import net.nikev2.nikev23.CustomSchedules;
import net.nikev2.nikev23.DelayUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;


@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin {




    @Shadow public abstract VillagerData getVillagerData();


    @Shadow public abstract boolean isClient();

    @Unique
    private boolean isParentNitwit(VillagerEntity parentEntity){

        return parentEntity.getVillagerData().getProfession() == VillagerProfession.NITWIT;
    }
    @Unique
    private Object GetParentProfession(VillagerEntity Parent){
        return Parent.getVillagerData().getProfession();
    }
    @Unique
    private double increaseNitwitificationProbability(VillagerEntity parentEntity)
    {
        if(isParentNitwit(parentEntity))
            return 0.4;
        return 0;
    }

    /// For some reason i just copied everything from the source file to make it work for baby villagers since the voids couldn't be accessed with thousand errors
    @Unique
    private ActionResult StartTrading(PlayerEntity player){
        VillagerEntity villager =(VillagerEntity) (Object) this;
        int i = villager.getReputation(player);
        if (i != 0) {
            for (TradeOffer tradeOffer : villager.getOffers()) {
                tradeOffer.increaseSpecialPrice(-MathHelper.floor((float)i * tradeOffer.getPriceMultiplier()));
            }
        }
        if (player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {
            StatusEffectInstance statusEffectInstance = player.getStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE);
            assert statusEffectInstance != null;
            int j = statusEffectInstance.getAmplifier();
            for (TradeOffer tradeOffer2 : villager.getOffers()) {
                double d = 0.3 + 0.0625 * (double)j;
                int k = (int)Math.floor(d * (double)tradeOffer2.getOriginalFirstBuyItem().getCount());
                tradeOffer2.increaseSpecialPrice(-Math.max(k, 1));
            }
        }
        villager.setCustomer(player);
        villager.sendOffers(player, villager.getDisplayName(), villager.getVillagerData().getLevel());
        return ActionResult.success(villager.isClient());
    }
    @Unique
    private ActionResult success = ActionResult.success(this.isClient());

    @Unique
    public VillagerProfession getRandomProfession() {
        List<VillagerProfession> professions = new ArrayList<>();

        // Use reflection to get all fields (professions) from VillagerProfession record
        Field[] fields = VillagerProfession.class.getFields();
        for (Field field : fields) {
            try {
                // Check if the field is of type VillagerProfession
                if (field.getType().equals(VillagerProfession.class)) {
                    professions.add((VillagerProfession) field.get(null));

                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        Random random = new Random();
        VillagerProfession chosenProfession;

        do {
            System.out.println("Attempting to random employ");

            // Generate a random index within the range of the professions list size
            int randomIndex = random.nextInt(professions.size());

            // Get the profession at the random index
            chosenProfession = professions.get(randomIndex);

            // Repeat until the profession is not Nitwit or None
        } while (chosenProfession.equals(VillagerProfession.NITWIT) || chosenProfession.equals(VillagerProfession.NONE));

        return chosenProfession;
    }

    @Unique
    private void makeNitwit(VillagerEntity villagerEntity, double probability){
        double random = Math.random();

        if(random <= probability)
            villagerEntity.setVillagerData(villagerEntity.getVillagerData().withProfession(VillagerProfession.NITWIT));

    }

    @Inject(method = "interactMob",at = @At(value = "HEAD"), cancellable = true)
    public void OnInteraction(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir){

        if ((Object)this instanceof VillagerEntity villager){

            ServerWorld world=MinecraftClient.getInstance().getServer().getWorld(player.getWorld().getRegistryKey());
            if (hand.equals(Hand.MAIN_HAND)){
                boolean HasChain=player.getMainHandStack().getItem().equals(Items.CHAIN);

                if (HasChain && villager.isBaby() && !villager.isSleeping()){
                    if (!villager.getCommandTags().contains("laborer")){
                        villager.playSound(SoundEvents.BLOCK_CHAIN_PLACE,1f,1f);
                        villager.playSound(SoundEvents.ENTITY_VILLAGER_NO,1f,villager.getSoundPitch());
                        villager.setHeadRollingTimeLeft(50);
                        villager.addCommandTag("laborer");
                        world.sendEntityStatus(villager, EntityStatuses.ADD_VILLAGER_ANGRY_PARTICLES);
                        cir.setReturnValue(this.success);

                    }else {
                        villager.playSound(SoundEvents.ENTITY_VILLAGER_CELEBRATE,1f,villager.getSoundPitch());
                        villager.playSound(SoundEvents.BLOCK_CHAIN_BREAK,1f,1f);
                        villager.getCommandTags().remove("laborer");
                        world.sendEntityStatus(villager, EntityStatuses.ADD_VILLAGER_HEART_PARTICLES);
                        cir.setReturnValue(this.success);
                    }
                    villager.reinitializeBrain(world);
                    cir.setReturnValue(this.success);


                }else  if (villager.isBaby() && !villager.isSleeping() && !(villager.getVillagerData().getProfession().equals(VillagerProfession.NONE)||villager.getVillagerData().getProfession().equals(VillagerProfession.NITWIT)))cir.setReturnValue(StartTrading(player));
            }



        }


    }


    @Inject(method = "playWorkSound",at=@At(value = "HEAD"),cancellable = true)
    void onworksound(CallbackInfo ci){
        VillagerEntity villager = (VillagerEntity) (Object) this;
        villager.playSound(villager.getVillagerData().getProfession().workSound(),1f,1f);
        ci.cancel();
    }

    /// If a baby villager does not have the laborer tag than it won't make it look for a job site
    @Unique
    public ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> GetLaborerCoreData(VillagerEntity villager){
        float speed=0.5f;
        boolean BabySlave= villager.getCommandTags().contains("laborer");
        VillagerProfession profession = villager.getVillagerData().getProfession();
        BabySlave=!BabySlave; /// For some reason it needs the inversed beacuse of the only run if child boolean
        return ImmutableList.of(Pair.of(0, new StayAboveWaterTask(0.8f)), Pair.of(0, OpenDoorsTask.create()), Pair.of(0, new LookAroundTask(45, 90)), Pair.of(0, new PanicTask()), Pair.of(0, WakeUpTask.create()), Pair.of(0, HideWhenBellRingsTask.create()), Pair.of(0, StartRaidTask.create()), Pair.of(0, ForgetCompletedPointOfInterestTask.create(profession.heldWorkstation(), MemoryModuleType.JOB_SITE)), Pair.of(0, ForgetCompletedPointOfInterestTask.create(profession.acquirableWorkstation(), MemoryModuleType.POTENTIAL_JOB_SITE)), Pair.of(1, new WanderAroundTask()), Pair.of(2, WorkStationCompetitionTask.create()), Pair.of(3, new FollowCustomerTask(speed)), new Pair[]{Pair.of(5, WalkToNearestVisibleWantedItemTask.create(speed, false, 4)), Pair.of(6, FindPointOfInterestTask.create(profession.acquirableWorkstation(), MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, BabySlave, Optional.empty())), Pair.of(7, new WalkTowardJobSiteTask(speed)), Pair.of(8, TakeJobSiteTask.create(speed)), Pair.of(10, FindPointOfInterestTask.create(poiType -> poiType.matchesKey(PointOfInterestTypes.HOME), MemoryModuleType.HOME, false, Optional.of((byte)14))), Pair.of(10, FindPointOfInterestTask.create(poiType -> poiType.matchesKey(PointOfInterestTypes.MEETING), MemoryModuleType.MEETING_POINT, false, Optional.of((byte)14))), Pair.of(10, GoToWorkTask.create()), Pair.of(10, LoseJobOnSiteLossTask.create())});
    }
    @Inject(method = "initBrain",at=@At(value = "HEAD"),cancellable = true)
    void onBrainInit(Brain<VillagerEntity> brain, CallbackInfo ci){

        VillagerEntity villager=(VillagerEntity) (Object) this;

        VillagerProfession villagerProfession=villager.getVillagerData().getProfession();

        ///Schedule Logic

        if (villager.isBaby()){

            if (villager.getCommandTags().contains("laborer")){
                brain.setSchedule(CustomSchedules.VILLAGER_SLAVE);

            }else if (villager.getVillagerData().getProfession().equals(VillagerProfession.NITWIT)){
                brain.setSchedule(CustomSchedules.VILLAGER_BABY_NITWIT);


            }else brain.setSchedule(Schedule.VILLAGER_BABY);

        }else {
            if (villager.getVillagerData().getProfession().equals(VillagerProfession.NITWIT)){
                brain.setSchedule(CustomSchedules.VILLAGER_NITWIT);
            }else {
                brain.setSchedule(Schedule.VILLAGER_DEFAULT);
            }
        }
        ///


        // overides villager activity logic.
        brain.setTaskList(Activity.MEET, VillagerTaskListProvider.createMeetTasks(villagerProfession, 0.5f), ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryModuleState.VALUE_PRESENT)));
        brain.setTaskList(Activity.REST, VillagerTaskListProvider.createRestTasks(villagerProfession, 0.5f));
        brain.setTaskList(Activity.PLAY, VillagerTaskListProvider.createPlayTasks(0.5f));
        brain.setTaskList(Activity.IDLE, VillagerTaskListProvider.createIdleTasks(villagerProfession, 0.5f));
        brain.setTaskList(Activity.PANIC, VillagerTaskListProvider.createPanicTasks(villagerProfession, 0.5f));
        brain.setTaskList(Activity.PRE_RAID, VillagerTaskListProvider.createPreRaidTasks(villagerProfession, 0.5f));
        brain.setTaskList(Activity.RAID, VillagerTaskListProvider.createRaidTasks(villagerProfession, 0.5f));
        brain.setTaskList(Activity.HIDE, VillagerTaskListProvider.createHideTasks(villagerProfession, 0.5f));
        brain.setTaskList(Activity.WORK, VillagerTaskListProvider.createWorkTasks(getVillagerData().getProfession(), 0.5f), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryModuleState.VALUE_PRESENT)));
        brain.setTaskList(Activity.CORE,GetLaborerCoreData(villager));
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.doExclusively(Activity.IDLE);
        brain.refreshActivities(villager.getWorld().getTimeOfDay(), villager.getWorld().getTime());
        ci.cancel();

    }
    @Inject(method = "onInteractionWith",at=@At("HEAD"),cancellable = true) ///Prevents negative gossips or iron golems attacking
    void onDeath(EntityInteraction interaction, Entity entity, CallbackInfo ci){
        if (!Config.NoNegativeRepuation) return;
        if(interaction.equals(EntityInteraction.VILLAGER_KILLED)||interaction.equals(EntityInteraction.VILLAGER_HURT)){
            if (entity instanceof PlayerEntity)ci.cancel();

        }
    }


    @Inject(method = "createChild(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/PassiveEntity;)Lnet/minecraft/entity/passive/VillagerEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;initialize(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/world/LocalDifficulty;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/entity/EntityData;Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/entity/EntityData;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD,cancellable = true)
    void createChild(ServerWorld serverWorld, PassiveEntity passiveEntity, CallbackInfoReturnable<VillagerEntity> cir, VillagerType villagerType, double d, VillagerEntity Child) {
        if(passiveEntity instanceof VillagerEntity) {

            DelayUtil.wait(1/10, Child::playAmbientSound);


            if(!Config.InheritanceSystem)return;

            // Nitwit Logic
            double nitwitProbability = 0.1;
            nitwitProbability += increaseNitwitificationProbability((VillagerEntity)(Object)this);
            nitwitProbability += increaseNitwitificationProbability((VillagerEntity)passiveEntity);
            makeNitwit(Child, nitwitProbability);
            Child.method_35200();
            // End of nitwit logic
            if (Child.getVillagerData().getProfession()!=VillagerProfession.NITWIT){
                double ChanceToInheritFromParent=0.5;
                double ChanceForDiffrentProfession=0.9;
                VillagerEntity Parent=(VillagerEntity)passiveEntity;

                VillagerProfession ParentProfession=(VillagerProfession) GetParentProfession(Parent);
                Random random = new Random();
                double Chance = random.nextDouble();

                if (Parent.getCommandTags().contains("NoInherit")){ // If the villager child has the special tag
                    Child.setVillagerData(Child.getVillagerData().withProfession(VillagerProfession.NONE));
                    Child.addCommandTag("FromNoInherit");
                }else {
                    Child.setExperience(1); // Baby villagers can't get job sites so this locks their profession
                    if (Chance<ChanceToInheritFromParent){
                        do { // Sometimes the Villager doesn't get the profession from there parent this makes sure to keep doing it until it gets the profession
                            if (Parent.getVillagerData().getProfession().equals(VillagerProfession.NONE)) {
                                Child.addCommandTag("AlreadyNone");
                                break;
                            } // If the parents profession was already none then break the loop
                            Child.setVillagerData(Child.getVillagerData().withProfession(ParentProfession));


                        }while (Child.getVillagerData().getProfession()==VillagerProfession.NONE); // Repeat until Villagers profession is not none
                    }
                    else if (Chance<ChanceForDiffrentProfession){
                    Child.setExperience(2);

                    Child.setVillagerData(Child.getVillagerData().withProfession(getRandomProfession())); // the method alredy has a loop built in


                    }

                }



            }
            cir.setReturnValue(Child);
        }


    }



}
