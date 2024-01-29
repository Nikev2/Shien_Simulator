package net.nikev2.nikev23;

public class Config {
    public static boolean InheritanceSystem=true; // a slightly tweaked pre village and pillage mechanic: baby villagers will inherit their profession based on their parents. The chance list is on the bottom of the config NOTE: the chances are not configurable.
    public static boolean FixedSoundPitch=true; // Baby villagers sound pitch will slowly drop from a high 2 to 1.3 as they get older instead of using a random pitch
    public static boolean IronGolemsAttackNitwits=true; // make those iron robots attack those dumb lazy fucks. There's also a bug were iron golems might attack each other.
    public static boolean NoNegativeRepuation=true; // Gives you immunity to the consequences of killing or hurting villagers. This means iron golems don't attack you

    /* MORE INFO:
    Chance List:
        10 percent chance of being a dumb-ass (NITWIT)
        40 percent of choosing a random profession (After the nitwit chance logic happens)
        50 percent chance of it taking the profession of its parent

        NOTE: this does NOT require workstations to work. ALSO, SOME OF THE VILLAGERS MIGHT BE UNEMPLOYED I CAN'T FIX THIS
   */

}
