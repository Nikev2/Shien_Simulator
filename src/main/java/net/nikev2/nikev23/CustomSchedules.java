package net.nikev2.nikev23;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;


public class CustomSchedules {
    public static final Schedule VILLAGER_NITWIT=new ScheduleBuilder(new Schedule())

            .withActivity(6000, Activity.IDLE)
            .withActivity(18000, Activity.REST)

            .build();

    public static final Schedule VILLAGER_BABY_NITWIT=new ScheduleBuilder(new Schedule())

            .withActivity(6000, Activity.IDLE)
            .withActivity(10000,Activity.PLAY)
            .withActivity(12000,Activity.IDLE)
            .withActivity(16000,Activity.PLAY)
            .withActivity(18000, Activity.REST)
            .build();

   public static final Schedule VILLAGER_SLAVE=new ScheduleBuilder(new Schedule())


            .withActivity(20000,Activity.IDLE) // wake up 4 hours before the villagers wake up
            .withActivity(22000,Activity.WORK)
            .withActivity(14000,Activity.IDLE)
            .withActivity(15000,Activity.PLAY) // get 1 hour of free time
            .withActivity(16000,Activity.REST) // Get there nice 4-hour rest
            .build();
}
