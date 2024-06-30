package dev.efnilite.ip.config;

import dev.efnilite.ip.IP;
import dev.efnilite.ip.api.Registry;
import dev.efnilite.ip.menu.ParkourOption;
import dev.efnilite.ip.style.RandomStyle;
import dev.efnilite.ip.style.Style;
import dev.efnilite.vilib.particle.ParticleData;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Class for variables required in generating without accessing the file a lot (constants)
 */
public class Option {

    public static double BORDER_SIZE;
    public static List<Integer> POSSIBLE_LEADS;

    // Advanced settings
    public static Vector HEADING;

    public static Map<ParkourOption, Boolean> OPTIONS_ENABLED;
    public static Map<ParkourOption, String> OPTIONS_DEFAULTS;

    public static Location GO_BACK_LOC;

    public static void init(boolean firstLoad) {
        initSql();
        initEnums();
        initGeneration();
        initStyles("styles.list", Config.CONFIG.fileConfiguration, RandomStyle::new)
                .forEach(Registry::register);

        GO_BACK_LOC = parseLocation(Config.CONFIG.getString("bungeecord.go-back"));
        String[] axes = Config.CONFIG.getString("bungeecord.go-back-axes").split(",");
        GO_BACK_LOC.setPitch(Float.parseFloat(axes[0]));
        GO_BACK_LOC.setYaw(Float.parseFloat(axes[1]));

        // General settings

        // Options

        List<ParkourOption> options = new ArrayList<>(Arrays.asList(ParkourOption.values()));

        // exceptions
        options.remove(ParkourOption.JOIN);
        options.remove(ParkourOption.ADMIN);

        // =====================================

        OPTIONS_DEFAULTS = new HashMap<>();
        OPTIONS_ENABLED = new HashMap<>();

        String prefix = "default-values";
        for (ParkourOption option : options) {
            String parent = "%s.%s".formatted(prefix, option.path);

            // register enabled value
            OPTIONS_ENABLED.put(option, Config.CONFIG.getBoolean("%s.enabled".formatted(parent)));

            // register default value
            if (!Config.CONFIG.isPath("%s.default".formatted(parent))) {
                continue;
            }

            Object value = Config.CONFIG.get("%s.default".formatted(parent));

            if (value != null) {
                OPTIONS_DEFAULTS.put(option, String.valueOf(value));
            }
        }

        // =====================================

        // Config stuff

        POSSIBLE_LEADS = Config.CONFIG.getIntList("options.leads.amount");
        for (int lead : new ArrayList<>(POSSIBLE_LEADS)) {
            if (lead < 1 || lead > 128) {
                IP.logging().error("Invalid lead: %d. Should be above 1 and below 128.".formatted(lead));
                POSSIBLE_LEADS.remove((Object) lead);
            }
        }

        // Generation
        HEADING = stringToVector(Config.GENERATION.getString("advanced.island.parkour.heading"));

        // Scoring

        if (firstLoad) {
            BORDER_SIZE = Config.GENERATION.getDouble("advanced.border-size");
            SQL = Config.CONFIG.getBoolean("sql.enabled");
        }
    }

    private static Vector stringToVector(String direction) {
        return switch (direction.toLowerCase()) {
            case "north" -> new org.bukkit.util.Vector(0, 0, -1);
            case "south" -> new org.bukkit.util.Vector(0, 0, 1);
            case "west" -> new org.bukkit.util.Vector(-1, 0, 0);
            default -> new Vector(1, 0, 0); // east
        };
    }

    private static Location parseLocation(String location) {
        String[] values = location.replaceAll("[()]", "").replaceAll("[, ]", " ").split(" ");

        World world = Bukkit.getWorld(values[3]);

        if (world == null) {
            world = Bukkit.getWorlds().get(0);
        }

        return new Location(world, Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]));
    }

    public static ParticleShape PARTICLE_SHAPE;
    public static String SOUND_TYPE;
    public static int SOUND_PITCH;
    public static int SOUND_VOLUME;
    public static Particle PARTICLE_TYPE;
    public static ParticleData<?> PARTICLE_DATA;

    private static void initEnums() {
        String value = Config.CONFIG.getString("particles.sound-type");
        SOUND_TYPE = value;

        value = Config.CONFIG.getString("particles.particle-type");
        try {
            PARTICLE_TYPE = Particle.valueOf(value);
        } catch (IllegalArgumentException ex) {
            PARTICLE_TYPE = Particle.valueOf("SPELL_INSTANT");
            IP.logging().error("Invalid particle type: %s".formatted(value));
        }

        SOUND_PITCH = Config.CONFIG.getInt("particles.sound-pitch");
        SOUND_VOLUME = Config.CONFIG.getInt("particles.sound-volume");
        PARTICLE_SHAPE = ParticleShape.valueOf(Config.CONFIG.getString("particles.particle-shape").toUpperCase());
        PARTICLE_DATA = new ParticleData<>(PARTICLE_TYPE, null, 10, 0, 0, 0, 0);
    }

    public enum ParticleShape {
        DOT, CIRCLE, BOX
    }

    // --------------------------------------------------------------
    // MySQL
    public static boolean SQL;
    public static int SQL_PORT;
    public static String SQL_URL;
    public static String SQL_DB;
    public static String SQL_USERNAME;
    public static String SQL_PASSWORD;
    public static String SQL_PREFIX;

    private static void initSql() {
        SQL_PORT = Config.CONFIG.getInt("sql.port");
        SQL_DB = Config.CONFIG.getString("sql.database");
        SQL_URL = Config.CONFIG.getString("sql.url");
        SQL_USERNAME = Config.CONFIG.getString("sql.username");
        SQL_PASSWORD = Config.CONFIG.getString("sql.password");
        SQL_PREFIX = Config.CONFIG.getString("sql.prefix");
    }

    // --------------------------------------------------------------
    // Generation

    public static double TYPE_NORMAL;
    public static double TYPE_SPECIAL;
    public static double TYPE_SCHEMATICS;

    public static double SPECIAL_ICE;
    public static double SPECIAL_SLAB;
    public static double SPECIAL_PANE;
    public static double SPECIAL_FENCE;

    public static double NORMAL_DISTANCE_1;
    public static double NORMAL_DISTANCE_2;
    public static double NORMAL_DISTANCE_3;
    public static double NORMAL_DISTANCE_4;

    public static double NORMAL_HEIGHT_1;
    public static double NORMAL_HEIGHT_0;
    public static double NORMAL_HEIGHT_NEG1;
    public static double NORMAL_HEIGHT_NEG2;

    public static int MAX_Y;
    public static int MIN_Y;

    private static void initGeneration() {
        TYPE_NORMAL = Config.GENERATION.getInt("generation.type.normal") / 100.0;
        TYPE_SPECIAL = Config.GENERATION.getInt("generation.type.schematic") / 100.0;
        TYPE_SCHEMATICS = Config.GENERATION.getInt("generation.type.special") / 100.0;

        SPECIAL_ICE = Config.GENERATION.getInt("generation.special.ice") / 100.0;
        SPECIAL_SLAB = Config.GENERATION.getInt("generation.special.slab") / 100.0;
        SPECIAL_PANE = Config.GENERATION.getInt("generation.special.pane") / 100.0;
        SPECIAL_FENCE = Config.GENERATION.getInt("generation.special.fence") / 100.0;

        NORMAL_DISTANCE_1 = Config.GENERATION.getInt("generation.normal.distance.1") / 100.0;
        NORMAL_DISTANCE_2 = Config.GENERATION.getInt("generation.normal.distance.2") / 100.0;
        NORMAL_DISTANCE_3 = Config.GENERATION.getInt("generation.normal.distance.3") / 100.0;
        NORMAL_DISTANCE_4 = Config.GENERATION.getInt("generation.normal.distance.4") / 100.0;

        NORMAL_HEIGHT_1 = Config.GENERATION.getInt("generation.normal.height.1") / 100.0;
        NORMAL_HEIGHT_0 = Config.GENERATION.getInt("generation.normal.height.0") / 100.0;
        NORMAL_HEIGHT_NEG1 = Config.GENERATION.getInt("generation.normal.height.-1") / 100.0;
        NORMAL_HEIGHT_NEG2 = Config.GENERATION.getInt("generation.normal.height.-2") / 100.0;

        MAX_Y = Config.GENERATION.getInt("generation.settings.max-y");
        MIN_Y = Config.GENERATION.getInt("generation.settings.min-y");

        if (MIN_Y >= MAX_Y) {
            MIN_Y = 100;
            MAX_Y = 200;

            IP.logging().stack("Provided minimum y is the same or larger than maximum y!", "check your generation.yml file");
        }
    }

    // --------------------------------------------------------------

    public static Set<Style> initStyles(String path, FileConfiguration config, BiFunction<String, List<Material>, Style> fn) {
        var styles = new HashSet<Style>();

        for (String style : Locales.getChildren(config, path, false)) {
            styles.add(fn.apply(style,
                    config.getStringList("%s.%s".formatted(path, style)).stream()
                            .map(name -> {
                                var material = Material.getMaterial(name.toUpperCase());

                                if (material == null) {
                                    IP.logging().error("Invalid material %s in style %s".formatted(name, style));
                                    return Material.STONE;
                                }

                                return material;
                            })
                            .toList()));
        }

        return styles;
    }
}