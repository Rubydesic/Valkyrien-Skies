package org.valkyrienskies.mod.common.capability.framework;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.util.EnumFacing;
import org.valkyrienskies.mod.common.capability.QueryableShipDataCapability;

/**
 * Implement as follows
 *
 * <pre>{@code
 * public class QueryableShipDataCapability extends VSDefaultCapability<QueryableShipData> {
 *
 *     public QueryableShipDataCapability(ObjectMapper mapper) {
 *         super(mapper, QueryableShipData.class);
 *     }
 *
 *     public QueryableShipDataCapability() {
 *         super(QueryableShipData.class);
 *     }
 *
 * }
 * }</pre>
 *
 * @param <K> The type of object this capability should store
 * @see QueryableShipDataCapability
 * be implemented
 */
@Accessors(fluent = false)
@ParametersAreNonnullByDefault
@Log4j2
public abstract class VSDefaultCapability<K> {

    @Getter(AccessLevel.PROTECTED)
    private final ObjectMapper mapper;
    private final Class<K> kClass;
    @Nonnull
    private K instance;

    public VSDefaultCapability(Class<K> kClass, Supplier<K> factory) {
        this(kClass, factory, createMapper());
    }

    public VSDefaultCapability(Class<K> kClass, Supplier<K> factory, ObjectMapper mapper) {
        this.kClass = kClass;
        this.instance = factory.get();
        this.mapper = mapper;
    }

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new CBORMapper();

        mapper.setVisibility(mapper.getVisibilityChecker()
            .withFieldVisibility(Visibility.ANY)
            .withGetterVisibility(Visibility.NONE)
            .withIsGetterVisibility(Visibility.NONE)
            .withSetterVisibility(Visibility.NONE));

        return mapper;
    }

    @Nullable
    public NBTTagByteArray writeNBT(EnumFacing side) {
        byte[] value;
        try {
            value = getMapper().writeValueAsBytes(instance);
        } catch (Exception ex) {
            log.fatal("Something just broke horrifically. Be wary of your data. "
                + "This will crash the game in future releases", ex);
            value = new byte[0];
        }
        return new NBTTagByteArray(value);
    }

    public K readNBT(NBTBase base, EnumFacing side) {
        byte[] value = ((NBTTagByteArray) base).getByteArray();
        System.out.println(Arrays.toString(value));
        try {
            this.instance = mapper.readValue(value, kClass);
        } catch (IOException ex) {
            log.fatal("Failed to read your ship data? Ships will probably be missing", ex);
        }

        return this.instance;
    }

    public K get() {
        return instance;
    }

    public void set(K instance) {
        this.instance = instance;
    }

}