package org.samo_lego.golfiv.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static org.samo_lego.golfiv.utils.BallLogger.logError;

public class GolfConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Main part of the config.
     */
    public static class Main {
        /**
         * Checks item picked that come in players inventory,
         * whether they have disallowed enchantments. This
         * can be disabled if you believe that players
         * didn't spawn in illegals.
         *
         * Status: working
         */
        public boolean checkForStrangeItems = true;
        /**
         * Client can tell server its onGround status and
         * server blindly accepts it. This can allow
         * client to not take any fall damage.
         * This setting re-enables the check server-side
         * and doesn't care about the client's onGround status.
         *
         * Status: working (might throw false positives when lagging, not sure)
         */
        public boolean yesFall = true;
        /**
         * Detects flight, either it being
         * a boat-flight or normal player flight
         *
         * Status: working (might throw false positives when lagging, not sure)
         */
        public boolean noFly = true;
        /**
         * Tries to detect the timer check, which allows
         * client to speed up the game, in order to move faster or use items
         * faster.
         *
         * Status: ohh, we are halfway there
         */
        public boolean antiTimer = true;
        /**
         * Tries to detect speed hacks.
         * Kinda works, but needs overhaul.
         *
         * Status: not really usable, as ice paths still throw false positives
         */
        public boolean noSpeed = true;
        /**
         * Tries to prevent "fake" elytra movement.
         * Bad.
         *
         * Status: even worse than speed checks, needs overhaul
         */
        public boolean preventElytraHacks = true;
        /**
         * Clears NBT items, but still allows block-picking.
         *
         * Status: working
         */
        public boolean preventCreativeStrangeItems = true;

        /**
         * Checks whether is doing actions
         * that cannot be done while having the GUI open.
         * (e. g. hitting, moving, etc.)
         *
         * Status: partially working
         */
        public boolean checkIllegalActions = true;

        /**
         * Detects player hitting entity through full blocks.
         *
         * Status: working
         */
        public boolean hitThroughWallCheck = true;
    }

    /**
     * Where to log cheaters.
     */
    public static class Logging {
        /**
         * Logs cheat attempts to console.
         *
         * Status: working
         */
        public boolean toConsole = true;
        /**
         * Logs cheat attempts to ops.
         *
         * Status: not yet implemented
         */
        public boolean toOps = true;
    }

    /**
     * Which messages should be used when kicking client on cheat attempts.
     * Messages are chosen randomly.
     *
     * Status: working
     */
    public ArrayList<String> kickMessages = new ArrayList<>(Arrays.asList(
            "Only who dares wins!",
            "Bad Liar ...",
            "Script kiddo?",
            "No risk it, no biscuit!",
            "Playing God? How about no?",
            "Who flies high falls low",
            "If you cheat, you only cheat yourself.",
            "I'm not upset that you lied to me,\n I'm upset that from now on I can't believe you.",
            "Hax bad.",
            "You better check your client. It seems to be lying.",
            "Impossible = cannot be done. But it was done by you?"
    ));

    public final GolfConfig.Main main = new Main();
    public final GolfConfig.Logging logging = new Logging();

    /**
     * Loads GolfIV config from file.
     *
     * @param configFile file to read GolfIV config from.
     * @return GolfConfig object
     */
    public static GolfConfig loadConfig(File configFile) {
        GolfConfig golfConfig;
        if(configFile.exists() && configFile.isFile()) {
            try(
                    FileInputStream fileInputStream = new FileInputStream(configFile);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            ) {
                golfConfig = GSON.fromJson(bufferedReader, GolfConfig.class);
            }
            catch (IOException e) {
                throw new RuntimeException("[GolfIV] Problem occurred when trying to load config: ", e);
            }
        }
        else {
            golfConfig = new GolfConfig();
        }
        golfConfig.saveConfig(configFile);

        return golfConfig;
    }

    /**
     * Saves GolfIV config to the file.
     *
     * @param configFile file where to save config to.
     */
    public void saveConfig(File configFile) {
        try (
                FileOutputStream stream = new FileOutputStream(configFile);
                Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)
        ) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            logError("Problem occurred when saving config: " + e.getMessage());
        }
    }
}
