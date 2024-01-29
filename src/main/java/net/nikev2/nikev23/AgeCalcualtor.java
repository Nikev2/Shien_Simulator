package net.nikev2.nikev23;

import net.minecraft.entity.passive.VillagerEntity;

public class AgeCalcualtor {

    /////Sound Pitch not postional pitch
    public static float calculatePitchBasedOnAge(VillagerEntity villager) {
        // Define the age range and pitch range
        int minAge = VillagerEntity.BABY_AGE;
        int maxAge = -1;
        int age=villager.getBreedingAge();
        float maxPitch = 2.0f;
        float minPitch = 1.3f;



        // Ensure the age is within the expected range
        age = Math.max(minAge, Math.min(maxAge, age));

        // Normalize the age value to a range of 0 to 1
        float normalizedAge = (float)(age - minAge) / (maxAge - minAge);

        // Calculate the pitch based on the normalized age
        // The pitch decreases as the age increases

        return maxPitch - normalizedAge * (maxPitch - minPitch);
    }
}
