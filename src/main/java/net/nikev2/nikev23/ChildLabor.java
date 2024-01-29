package net.nikev2.nikev23;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import org.apache.commons.lang3.Range;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
public class ChildLabor implements ModInitializer {
    boolean VillagerESPActive=false;
    boolean BabyVillagerESPActive=false;
    boolean NoJobVillagerESPActive=false;
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
            // Generate a random index within the range of the professions list size
            int randomIndex = random.nextInt(professions.size());

            // Get the profession at the random index
            chosenProfession = professions.get(randomIndex);

            // Repeat until the profession is not Nitwit or None
        } while (chosenProfession.equals(VillagerProfession.NITWIT) || chosenProfession.equals(VillagerProfession.NONE));

        return chosenProfession;
    }
    public void RefreshVillager(VillagerEntity villager){

        //////// Tries to ignore if the villager was given glowing from a different command
        if (villager.hasStatusEffect(StatusEffects.GLOWING)){
            boolean GlowingEffect=villager.hasStatusEffect(StatusEffects.GLOWING);
            if (GlowingEffect && Objects.requireNonNull(villager.getStatusEffect(StatusEffects.GLOWING)).getDuration()>3){
                return; /// If it was from a different command it stops the statement for the villager
            }
        }
        ///////
        StatusEffectInstance glowing=new StatusEffectInstance(StatusEffects.GLOWING,3,1,false,false);
        villager.addStatusEffect(glowing);
        if (VillagerESPActive)villager.addStatusEffect(glowing);else villager.removeStatusEffect(glowing.getEffectType());
        if (!VillagerESPActive && BabyVillagerESPActive && !NoJobVillagerESPActive){
            if (villager.isBaby()){
                villager.addStatusEffect(glowing);
            }else{
                villager.removeStatusEffect(glowing.getEffectType());
            }
        }
        if(!VillagerESPActive && !BabyVillagerESPActive && NoJobVillagerESPActive){
            if (Objects.requireNonNull(villager.getBrain().getOptionalMemory(MemoryModuleType.JOB_SITE)).isEmpty()&&!villager.isBaby()){
                villager.addStatusEffect(glowing);
            }else {
                villager.removeStatusEffect(glowing.getEffectType());
            }
        }

    }

    @Override
    public void onInitialize() {

        
        ServerEntityEvents.ENTITY_LOAD.register(((entity, world) -> {


            if (entity instanceof VillagerEntity villager){
                RefreshVillager(villager);
            }
        }));
        ServerTickEvents.START_WORLD_TICK.register((ServerWorld world)->{ /// Refreshes villager esp
            ClientPlayerEntity LocalPlayer= MinecraftClient.getInstance().player;
            if (LocalPlayer==null){
                return;
            }

            ServerPlayerEntity player=world.getServer().getPlayerManager().getPlayer(LocalPlayer.getUuid());
            assert player != null;
            List<VillagerEntity> villagers=world.getEntitiesByClass(VillagerEntity.class,new Box(player.getBlockPos()).expand(200), villager-> true);
            villagers.forEach(this::RefreshVillager);
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            dispatcher.register(CommandManager.literal("RefreshSchedules").executes(context -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                if (player == null) {
                    context.getSource().sendError(Text.of("Player is not available."));
                    return 0;
                }


                ServerWorld world = context.getSource().getServer().getWorld(player.getWorld().getRegistryKey());
                List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, new Box(player.getBlockPos()).expand(200), villager -> true);
                villagers.forEach(villager-> villager.reinitializeBrain(world));




                return 1;
            }));
            dispatcher.register(CommandManager.literal("BringBabyVillagers").executes(context -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                if (player==null)return 0;
                ServerWorld world=context.getSource().getServer().getWorld(player.getWorld().getRegistryKey());
                List<VillagerEntity> babyvillagers=world.getEntitiesByClass(VillagerEntity.class,new Box(player.getBlockPos()).expand(200), PassiveEntity::isBaby);
                babyvillagers.forEach(babyvillager->{
                    if (hastags(babyvillager)){
                        babyvillager.wakeUp();
                        babyvillager.setAiDisabled(true);
                        babyvillager.teleport(player.getX(),player.getY(),player.getZ());
                        babyvillager.setAiDisabled(false);
                        babyvillager.playAmbientSound();
                    }

                });
                return 1;
            }));
            dispatcher.register(CommandManager.literal("BringBabyVillagersWithFood").executes(context -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                if (player==null)return 0;
                ServerWorld world=context.getSource().getServer().getWorld(player.getWorld().getRegistryKey());
                List<VillagerEntity> babyvillagers=world.getEntitiesByClass(VillagerEntity.class,new Box(player.getBlockPos()).expand(200), PassiveEntity::isBaby);
                babyvillagers.forEach(babyvillager->{
                    if (hastags(babyvillager) && babyvillager.getInventory().containsAny(getAllFoodItems())){
                        babyvillager.wakeUp();
                        babyvillager.teleport(player.getX(),player.getY(),player.getZ());
                        babyvillager.playAmbientSound();

                    }
                });
                return 1;
            }));
            dispatcher.register(CommandManager.literal("RefreshNearestBabyVillager").executes(context -> {
                ServerWorld world=context.getSource().getWorld();
                Vec3d pos=context.getSource().getPosition();
                BlockPos blockpos=new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());

                VillagerEntity villager=findNearestVillager(world,blockpos,villager1 -> villager1.isBaby()&&villager1.getVillagerData().getProfession().equals(VillagerProfession.NONE));
                villager.setVillagerData(villager.getVillagerData().withProfession(getRandomProfession()));





                return 1;
            }));
            



            dispatcher.register(CommandManager.literal("tpvillagerstobed").executes(context -> {
                ServerWorld world=context.getSource().getWorld();
                Vec3d pos=context.getSource().getPosition();
                int TimeOfDay=(int)world.getTimeOfDay();

                BlockPos BlockPos=new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());

                List<VillagerEntity> villagers=world.getEntitiesByClass(VillagerEntity.class,new Box(BlockPos).expand(200), villager-> !villager.isSleeping());
                villagers.forEach(villager -> {

                    if (HasVillagerHomeMemory(villager) && villager.getBrain().getSchedule().getActivityForTime(TimeOfDay).equals(Activity.REST)){
                        BlockPos villagerhome= Objects.requireNonNull(villager.getBrain().getOptionalMemory(MemoryModuleType.HOME)).get().getPos();

                        villager.setInvulnerable(true);
                        villager.teleport(villagerhome.getX()+0.5,villagerhome.getY()+1,villagerhome.getZ()+0.5);
                        villager.setInvulnerable(false);
                        villager.playAmbientSound();
                        villager.sleep(villagerhome);

                    }
                });
                return 1;
            }));

            dispatcher.register(CommandManager.literal("villageresp").executes(context -> {

                ServerWorld world=context.getSource().getWorld();
                Vec3d pos=context.getSource().getPosition();

                BlockPos BlockPos=new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());

                List<VillagerEntity> villagers=world.getEntitiesByClass(VillagerEntity.class,new Box(BlockPos).expand(200), villager-> true);
                VillagerESPActive=!VillagerESPActive;
                BabyVillagerESPActive=false;
                NoJobVillagerESPActive=false;
                villagers.forEach(this::RefreshVillager);
                return 1;
            }));
            dispatcher.register(CommandManager.literal("babyvillageresp").executes(context -> {

                ServerWorld world=context.getSource().getWorld();
                Vec3d pos=context.getSource().getPosition();

                BlockPos BlockPos=new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());

                List<VillagerEntity> villagers=world.getEntitiesByClass(VillagerEntity.class,new Box(BlockPos).expand(200), PassiveEntity::isBaby);
                VillagerESPActive=false;
                BabyVillagerESPActive=!BabyVillagerESPActive;
                NoJobVillagerESPActive=false;
                villagers.forEach(this::RefreshVillager);
                return 1;
            }));
            dispatcher.register(CommandManager.literal("nojobvillageresp").executes(context -> {

                ServerWorld world=context.getSource().getWorld();
                Vec3d pos=context.getSource().getPosition();

                BlockPos BlockPos=new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());

                List<VillagerEntity> villagers=world.getEntitiesByClass(VillagerEntity.class,new Box(BlockPos).expand(200), villager-> !villager.isBaby() && Objects.requireNonNull(villager.getBrain().getOptionalMemory(MemoryModuleType.JOB_SITE)).isEmpty());
                VillagerESPActive=false;
                BabyVillagerESPActive=false;
                NoJobVillagerESPActive=!NoJobVillagerESPActive;
                villagers.forEach(this::RefreshVillager);
                return 1;
            }));
            dispatcher.register(CommandManager.literal("bringtheunemployed").executes(context -> {

                ServerWorld world=context.getSource().getWorld();
                Vec3d pos=context.getSource().getPosition();

                BlockPos BlockPos=new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());

                List<VillagerEntity> villagers=world.getEntitiesByClass(VillagerEntity.class,new Box(BlockPos).expand(200), villager-> !villager.isBaby() && Objects.requireNonNull(villager.getBrain().getOptionalMemory(MemoryModuleType.JOB_SITE)).isEmpty());

                villagers.forEach(villager -> {
                    villager.wakeUp();

                    if (world.getBlockEntity(BlockPos) instanceof CommandBlockBlockEntity){
                        villager.teleport(BlockPos.getX(),BlockPos.getY()+1,BlockPos.getZ());
                    }else {
                        villager.teleport(BlockPos.getX(),BlockPos.getY(),BlockPos.getZ());
                    }
                    villager.playAmbientSound();
                });

                return 1;
            }));
            dispatcher.register(CommandManager.literal("bringnewbornvillagers").executes(context -> {

                ServerWorld world=context.getSource().getWorld();
                Vec3d pos=context.getSource().getPosition();

                BlockPos BlockPos=new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());
                Range<Integer> AgeRange=Range.between(-24000,-20000);
                List<VillagerEntity> villagers=world.getEntitiesByClass(VillagerEntity.class,new Box(BlockPos).expand(200), villager-> AgeRange.contains(villager.getBreedingAge()));

                villagers.forEach(villager -> {
                    villager.wakeUp();
                    if (world.getBlockEntity(BlockPos) instanceof CommandBlockBlockEntity){
                        villager.teleport(BlockPos.getX(),BlockPos.getY()+1,BlockPos.getZ());
                    }else {
                        villager.teleport(BlockPos.getX(),BlockPos.getY(),BlockPos.getZ());
                    }
                    villager.playAmbientSound();
                });

                return 1;
            }));
            dispatcher.register(CommandManager.literal("tpawakevillagertobed").executes(context -> {
                ServerWorld world=context.getSource().getWorld();
                Vec3d pos=context.getSource().getPosition();
                int TimeOfDay=(int)world.getTimeOfDay();
                BlockPos BlockPos=new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());
                VillagerEntity villager=findNearestVillager(world,BlockPos,villager1 -> !villager1.isSleeping());
                if (HasVillagerHomeMemory(villager) && villager.getBrain().getSchedule().getActivityForTime(TimeOfDay).equals(Activity.REST)){
                    BlockPos villagerhome= Objects.requireNonNull(villager.getBrain().getOptionalMemory(MemoryModuleType.HOME)).get().getPos();

                    villager.setInvulnerable(true);
                    villager.teleport(villagerhome.getX()+0.5,villagerhome.getY()+1,villagerhome.getZ()+0.5);
                    villager.setInvulnerable(false);
                    villager.playAmbientSound();


                }


                return 1;
            }));
            dispatcher.register(CommandManager.literal("relTickets").then(CommandManager.argument("entity(s)",EntityArgumentType.entities()).executes(context -> {
                Collection<Entity> entities= (Collection<Entity>) EntityArgumentType.getEntities(context,"entity(s)");

                for (Entity entity : entities) {
                    if (!(entity instanceof VillagerEntity villager)){
                        context.getSource().sendError(Text.literal("Needs Villager Entity"));
                        return 0;
                    }


                    villager.releaseTicketFor(MemoryModuleType.HOME);
                    villager.releaseTicketFor(MemoryModuleType.JOB_SITE);
                    villager.releaseTicketFor(MemoryModuleType.POTENTIAL_JOB_SITE);
                    villager.releaseTicketFor(MemoryModuleType.MEETING_POINT);
                    villager.getBrain().forgetAll();

                }

                return 1;
            })));


        });


    }


    private boolean HasVillagerHomeMemory(VillagerEntity villager){
        return Objects.requireNonNull(villager.getBrain().getOptionalMemory(MemoryModuleType.HOME)).isPresent();
    }
    private boolean hastags(VillagerEntity villager){
        return (!villager.getCommandTags().contains("VillagerIgnore") && !villager.getCommandTags().contains("child"));
    }
    private static Set<Item> getAllFoodItems() {
        Set<Item> foodItems = new HashSet<>();

        for (Item item : Registries.ITEM) {
            if (item.isFood()) {
                foodItems.add(item);
            }
        }

        return foodItems;
    }

    private static VillagerEntity findNearestVillager(World world, BlockPos blockPos, Predicate<VillagerEntity> villagerPredicate) {
        double searchRadius = 30.0; // Define the search radius
        Box searchBox = new Box(blockPos).expand(searchRadius); // Define a box around the blockPos to search for villagers

        // Use the provided villagerPredicate, default to 'true' if it's null
        Predicate<VillagerEntity> effectivePredicate = villagerPredicate != null ? villagerPredicate : villager -> true;

        return world.getEntitiesByClass(VillagerEntity.class, searchBox, effectivePredicate)
                .stream()
                .min(Comparator.comparingDouble(v -> v.squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ())))
                .orElse(null);
    }
}
