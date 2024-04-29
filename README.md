### I was rejected by modrinth sand curseforge so theres no gallary
### Note this is fabric only

### Main Description

Do you hate baby villagers jumping on beds, chasing each other and not working? Do you wish that you could see a villagers trades at birth? Do you wish that a libarian villager could breed more librians instead of unemployed children? ~~Are you a cheap labor ancap?~~ Or you can't stop being the pysco you are in minecraft? Well you came to the right place let me introduce you to the CHILD LABOR UPDATE!

Keys:
*=Configurable via file

Some notes:

More info on confifuring files at the bottom


The mechanics of this mod includes:

- Custom Schedules
-*Iron Golems will kill those dumbheaded nitwits*
- *No Negative Reputation aka: kill as many villagers as you want and never face the justice system
- Baby villagers with professions are tradable
- *Villager Inheritance System baby villagers will be assigned professions at birth using a random chance probability system
- *Modified baby villager pitches. a baby villagers pitch (Pitch Range is 2-1.3) is now based on the age of it from -24000 to -1. Make them sound cute and innocent children.
- Modified schedules for nitwits and Child workers to give that Shien clothing factory experince
- Commands to enlarge your athourity

  NOTES: 
- Villager Leashing Not included in this mod theres plenty of mods out there for that stuff.
- I can't really update this to 1.20.2 or above beacuse my IDE keeps complaining

I was too lazy to build an actual schedule chart so heres some of the code heres how to read it
(ticktime,Activity)
```java
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
```
Directions: Right click a baby villager with a chain to put it in child labor mode right click it again to free it. (Enslaving a villager should make it bobble its head grunting with anger particles while reversing it makes it make happy sounds) (Sometimes particles might be glitched)


### CONFIG INFO

#### New Version:
use midnight lib config
#### Manual (OLD):
This mod has a very tricky way of configuring it it requires you to have basic knowlage of using a command terminal. This is a step by step guide on how to edit the config file (windows 11/10). If your on linux you could probaly interpret this youself

I suggest using winrar or any zip file manager that allows you to edit and save in archives

1. Download the [Config File](https://drive.google.com/uc?export=download&id=1CAhe05ND6B5dq5s2H-kRczJgrIDu1Krf) from the link. Its a direct link it should say you downloaded a file called Config.java
2. Open the file it should have a layout of all the settings what only matters are the true and false words on it change those to your hearts content. Do not change anything else on it. Also to state that you should use a code editor like vs code or notepad++ so you don't get confused by the comment code.
3. Save it and close what text editor you used to configure it and open command prompt.
4. Left click on the directory your config file is stored and click on "Open in terminal" with the powershell icon.
5. In the terminal (Command Prompt) type ```javac -source 17 -target 17 Config.java``` (The command doesn't output anything unless it errors) after that close the command prompt there should be a file that says ```Config.class```
6. Open the file in notepad and copy all the data
7. go to your mods folder and open the mod file with the winrar or the zip manager you are using
8. Go to ```\net\nikev2\nikev23``` for people who don't understand it click on the folder net then click on nikev2 then nikev23
9. Open ```Config.class``` delete the old config code and paste your config code.
10. Close notepad and update the archive (Winrar prompts you I don't know how it works on the other software)
11. Boom you went through the torture to edit the config file.  



##open source info##

use this code to to your hearts content as long as you
give me credit in comments or something.
contributing is welcomed as this is my first mod
and have alot of stuff to do
