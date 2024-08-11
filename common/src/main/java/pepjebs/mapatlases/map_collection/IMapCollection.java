package pepjebs.mapatlases.map_collection;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import pepjebs.mapatlases.utils.MapDataHolder;
import pepjebs.mapatlases.utils.MapType;
import pepjebs.mapatlases.utils.Slice;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Predicate;

public interface IMapCollection {

    @ExpectPlatform
    static IMapCollection get(ItemStack stack, Level level) {
        throw new AssertionError();
    }

    boolean add(int mapId, Level level);

    boolean remove(MapDataHolder obj);

    int getCount();

    boolean isEmpty();

    byte getScale();

    int[] getAllIds();

    Collection<ResourceKey<Level>> getAvailableDimensions();

    Collection<MapType> getAvailableTypes(ResourceKey<Level> dimension);

    TreeSet<Integer> getHeightTree(ResourceKey<Level> dimension, MapType type);

    List<MapDataHolder> selectSection(Slice slice);

    List<MapDataHolder> filterSection(Slice slice, Predicate<MapItemSavedData> predicate);

    @Nullable
    default MapDataHolder select(int x, int z, Slice slice) {
        return select(new MapKey(x, z, slice));
    }

    @Nullable
    MapDataHolder select(MapKey key);


    @Nullable
    MapDataHolder getClosest(double x, double z, Slice slice);

    @Nullable
    default MapDataHolder getClosest(Player player, Slice slice) {
        return getClosest(player.getX(), player.getZ(), slice);
    }

    List<MapDataHolder> getAll();


    void addNotSynced(Level level);

    boolean hasOneSlice();
}
