package pepjebs.mapatlases.map_collection;

import com.google.common.base.Preconditions;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import pepjebs.mapatlases.MapAtlasesMod;
import pepjebs.mapatlases.utils.MapDataHolder;
import pepjebs.mapatlases.utils.MapType;
import pepjebs.mapatlases.utils.Slice;

import java.util.*;
import java.util.function.Predicate;

// The purpose of this object is to save a datastructures with all available maps so we dont have to keep deserializing nbt
public class MapCollection implements IMapCollection {


    public static final String MAP_LIST_NBT = "maps";

    private final Map<MapKey, MapDataHolder> maps = new HashMap<>();
    private final Set<Integer> ids = new HashSet<>();
    //available dimensions and slices
    private final Map<ResourceKey<Level>, Map<MapType, TreeSet<Integer>>> dimensionSlices = new HashMap<>();
    private byte scale = 0;
    private CompoundTag lazyNbt = null;
    // list of ids that have not been received yet
    private final Set<Integer> notSyncedIds = new HashSet<>();


    public boolean isInitialized() {
        return lazyNbt == null;
    }

    private void assertInitialized() {
        Preconditions.checkState(this.lazyNbt == null, "map collection capability was not initialized");
    }

    // if a duplicate exists its likely that its data was not synced yet
    @Override
    public void addNotSynced(Level level) {
        notSyncedIds.removeIf(i -> add(i, level));
    }

    // we need leven context
    public void initialize(Level level) {
        if (level.isClientSide) {
            int aa = 1;
        }
        if (lazyNbt != null) {
            int[] array = lazyNbt.getIntArray(MAP_LIST_NBT);
            lazyNbt = null;
            for (int i : array) {
                add(i, level);
            }
        }
    }

    //@Override
    public CompoundTag serializeNBT() {
        if (!isInitialized()) return lazyNbt;
        CompoundTag c = new CompoundTag();
        c.putIntArray(MAP_LIST_NBT, ids.stream().toList());
        return c;
    }

    @Override
    public int[] getAllIds() {
        if (!isInitialized()) return lazyNbt.getIntArray(MAP_LIST_NBT);
        return ids.stream().mapToInt(Integer::intValue).toArray();
    }

    //@Override
    public void deserializeNBT(CompoundTag c) {
        lazyNbt = c.copy();
    }

    @Override
    public int getCount() {
        assertInitialized();
        return ids.size();
    }

    public boolean isEmpty() {
        assertInitialized();
        return maps.isEmpty();
    }

    @Override
    public boolean add(int intId, Level level) {
        assertInitialized();

        MapDataHolder found = MapDataHolder.findFromId(level, intId);
        if (this.isEmpty() && found != null) {
            scale = found.data.scale;
        }

        if (found == null) {
            if (level instanceof ServerLevel) {
                MapAtlasesMod.LOGGER.error("Map with id {} not found in level {}", intId, level.dimension().location());
            } else {
                //wait till we receive data from server
                ids.add(intId);
                notSyncedIds.add(intId);
            }
            return false;
        }

        MapItemSavedData d = found.data;

        if (d != null && d.scale == scale) {
            MapKey key = found.makeKey();

            //from now on we assume that all client maps cant have their center and data unfilled
            if (maps.containsKey(key)) {
                MapAtlasesMod.LOGGER.error("Duplicate map key {} found in level {}", key, level.dimension().location());
                return false;

            }
            ids.add(intId);
            maps.put(key, found);
            addToDimensionMap(key);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(MapDataHolder map) {
        assertInitialized();
        boolean success = ids.remove(map.id);
        if (maps.remove(map.makeKey()) != null) {
            dimensionSlices.clear();
            for (var j : maps.keySet()) {
                addToDimensionMap(j);
            }
        }
        return success;
    }

    private void addToDimensionMap(MapKey j) {
        dimensionSlices.computeIfAbsent(j.slice().dimension(), d -> new EnumMap<>(MapType.class))
                .computeIfAbsent(j.slice().type(), a -> new TreeSet<>())
                .add(j.slice().height() == null ? Integer.MAX_VALUE : j.slice().height());
    }

    @Override
    public byte getScale() {
        assertInitialized();
        return scale;
    }

    @Override
    public Collection<MapType> getAvailableTypes(ResourceKey<Level> dimension) {
        assertInitialized();
        var mapTypeTreeSetMap = dimensionSlices.get(dimension);
        if (mapTypeTreeSetMap != null) return mapTypeTreeSetMap.keySet();
        else return List.of();
    }


    @Override
    public Collection<ResourceKey<Level>> getAvailableDimensions() {
        assertInitialized();
        return dimensionSlices.keySet();
    }

    private static final TreeSet<Integer> TOP = Util.make(() -> {
        var t = new TreeSet<Integer>();
        t.add(Integer.MAX_VALUE);
        return t;
    });

    @Override
    public TreeSet<Integer> getHeightTree(ResourceKey<Level> dimension, MapType kind) {
        assertInitialized();
        var d = dimensionSlices.get(dimension);
        if (d != null) {
            return d.getOrDefault(kind, TOP);
        }
        return TOP;
    }

    @Override
    public List<MapDataHolder> getAll() {
        assertInitialized();
        return new ArrayList<>(maps.values());
    }

    @Override
    public List<MapDataHolder> selectSection(Slice slice) {
        assertInitialized();
        return maps.entrySet().stream().filter(e -> e.getKey().isSameSlice(slice))
                .map(Map.Entry::getValue).toList();
    }

    @Override
    public List<MapDataHolder> filterSection(Slice slice, Predicate<MapItemSavedData> predicate) {
        assertInitialized();
        return new ArrayList<>(maps.entrySet().stream().filter(e -> e.getKey().isSameSlice(slice)
                        && predicate.test(e.getValue().data))
                .map(Map.Entry::getValue).toList());
    }

    @Nullable
    @Override
    public MapDataHolder select(MapKey key) {
        assertInitialized();
        return maps.get(key);
    }

    @Nullable
    @Override
    public MapDataHolder getClosest(double x, double z, Slice slice) {
        assertInitialized();
        MapDataHolder minDistState = null;
        for (var e : maps.entrySet()) {
            var key = e.getKey();
            if (key.isSameSlice(slice)) {
                if (minDistState == null) {
                    minDistState = e.getValue();
                    continue;
                }
                if (distSquare(minDistState.data, x, z) > distSquare(e.getValue().data, x, z)) {
                    minDistState = e.getValue();
                }
            }
        }
        return minDistState;
    }

    public static double distSquare(MapItemSavedData mapState, double x, double z) {
        return Mth.square(mapState.centerX - x) + Mth.square(mapState.centerZ - z);
    }


    public boolean hasOneSlice() {
        return maps.keySet().stream().anyMatch(k -> k.slice() != null);
    }
}
