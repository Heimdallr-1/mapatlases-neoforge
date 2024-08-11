package pepjebs.mapatlases.config;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import pepjebs.mapatlases.MapAtlasesMod;
import pepjebs.mapatlases.utils.ActivationLocation;

import java.util.function.Supplier;

public class MapAtlasesConfig {

    static {
        ConfigBuilder builder = ConfigBuilder.create(MapAtlasesMod.MOD_ID, ConfigType.COMMON);


        builder.push("general");
        maxMapCount = builder
                .comment("The maximum number of Maps (Filled & Empty combined) allowed to be inside an Atlas.")
                .define("max_map_count", 512, 0, 10000);

        acceptPaperForEmptyMaps = builder
                .comment("If enabled, you can increase the Empty Map count by inserting Paper")
                .define("accept_paper_for_empty_maps", false);

        requireEmptyMapsToExpand = builder
                .comment("If true, the Atlas is required to have spare Empty Maps stored to expand the Filled Map size")
                .define("require_empty_maps_to_expand", true);

        mapEntryValueMultiplier = builder
                .comment("Controls how many usable Maps are added when you add a single Map to the Atlas")
                .define("map_entry_value_multiplier", 1, 0, 64);

        pityActivationMapCount = builder
                .comment("Controls how many free Empty Maps you get for 'activating' an Inactive Atlas")
                .define("pity_activation_map_count", 9, 0, 64);


        enableEmptyMapEntryAndFill = builder
                .comment("If 'true', Atlases will be able to store Empty Maps and auto-fill them as you explore.")
                .define("enable_empty_map_entry_and_fill", true);

        activationLocation = builder
                .comment("Locations of where an atlas will be scanned for. By default only hotbar will be scanned")
                .define("activation_locations", ActivationLocation.HOTBAR_AND_HANDS);

        creativeTeleport = builder
                .comment("Allows players in creative to teleport using the atlas. Hold shift and press anywhere")
                .define("creative_teleport", true);

        pinMarkerId = builder.comment("Marker id associated with the red pin button on the atlas screen. Set to empty string to disable")
                .define("pin_marked_id", "map_atlases:pin");

        lightMap = builder.comment("Shows light color on maps. Needs Moonlight lib")
                .define("light_map", false);

        builder.pop();
        builder.push("update_logic");
        roundRobinUpdate = builder.comment("Update maps in simple round robin fashion instead of prioritizing the ones closer. Overrides configs below")
                .define("round_robin", false);
        mapUpdatePerTick = builder.comment("Max of maps to update each tick. Increase to make maps update faster")
                .define("map_updates_per_tick", 1, 0, 9);

        mapUpdateMultithreaded = builder.comment("Makes map update on different threads, speeding up the process. Disable if it causes issues. Especially on servers. Try turning on for a big performance improvement regarding map atlas update")
                .define("multithreaded_update", UpdateType.SINGLE_PLAYER_ONLY);
        debugUpdate = builder.comment("Visually shows map updates")
                .define("debug_map_updates", false);
        markersUpdatePeriod = builder.comment("Every how many ticks should markers be updated")
                        .define("markers_update_period", 10, 1, 200);

        builder.pop();



        SPEC = builder.buildAndRegister();
    }

    public static final Supplier<Boolean> debugUpdate;
    public static final Supplier<Integer> markersUpdatePeriod;
    public static final Supplier<UpdateType> mapUpdateMultithreaded;
    public static final Supplier<Integer> maxMapCount;
    public static final Supplier<Integer> mapEntryValueMultiplier;
    public static final Supplier<Integer> pityActivationMapCount;
    public static final Supplier<Boolean> requireEmptyMapsToExpand;
    public static final Supplier<Boolean> acceptPaperForEmptyMaps;
    public static final Supplier<Boolean> enableEmptyMapEntryAndFill;
    public static final Supplier<Boolean> creativeTeleport;
    public static final Supplier<Boolean> roundRobinUpdate;
    public static final Supplier<Boolean> lightMap;
    public static final Supplier<String> pinMarkerId;
    public static final Supplier<Integer> mapUpdatePerTick;
    public static final Supplier<ActivationLocation> activationLocation;

    public static final ConfigSpec SPEC;

    public static enum UpdateType{
        OFF, SINGLE_PLAYER_ONLY, ALWAYS_ON
    }

    public static void init(){

    }
}
