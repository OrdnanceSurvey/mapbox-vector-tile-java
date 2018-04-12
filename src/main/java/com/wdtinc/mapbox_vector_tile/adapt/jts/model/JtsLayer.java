package com.wdtinc.mapbox_vector_tile.adapt.jts.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>JTS model of a Mapbox Vector Tile (MVT) layer.</p>
 *
 * <p>A layer contains a subset of all geographic geometries in the tile.</p>
 */
public class JtsLayer {

    private final String name;
    private final Collection<Geometry> geometries;

    /**
     * Create an empty JTS layer.
     *
     * @param name layer name
     * @throws IllegalArgumentException when {@code name} is null
     */
    public JtsLayer(String name) {
        this(name, new ArrayList<>(0));
    }

    /**
     * Create a JTS layer with geometries.
     *
     * @param name layer name
     * @param geometries
     * @throws IllegalArgumentException when {@code name} or {@code geometries} are null
     */
    public JtsLayer(String name, Collection<Geometry> geometries) {
        validate(name, geometries);
        this.name = name;
        this.geometries = geometries;
    }

    /**
     * Get a read-only collection of geometry.
     *
     * @return unmodifiable collection of geometry.
     */
    public Collection<Geometry> getGeometries() {
        return geometries;
    }

    /**
     * Get the layer name.
     *
     * @return name of the layer
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JtsLayer layer = (JtsLayer) o;

        if (name != null ? !name.equals(layer.name) : layer.name != null) return false;
        return GeometryEqualitySupport.areEqual(geometries, layer.geometries);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (geometries != null ? geometries.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Layer{" +
                "name='" + name + '\'' +
                ", geometries=" + geometries +
                '}';
    }

    /**
     * Validate the JtsLayer.
     *
     * @param name mvt layer name
     * @param geometries geometries in the tile
     * @throws IllegalArgumentException when {@code name} or {@code geometries} are null
     */
    private static void validate(String name, Collection<Geometry> geometries) {
        if (name == null) {
            throw new IllegalArgumentException("layer name is null");
        }
        if (geometries == null) {
            throw new IllegalArgumentException("geometry collection is null");
        }
    }

    private static class GeometryEqualitySupport {

        /**
         * JTS geometry equality does not consider User Data.
         *
         * This is an enhanced check to ensure that both the geometry and userdata is identical.
         *
         * See:
         * https://github.com/locationtech/jts/blob/181003a733623467968833fb08440fa921561596/modules/core/src/main/java/org/locationtech/jts/geom/Geometry.java#L1511
         *
         * @return true if geometries and userdata are identical
         */
        static boolean areEqual(Collection<Geometry> geometries, Collection<Geometry> geometries2) {
            if (geometries != null) {
                return geometries.equals(geometries2)
                    && GeometryEqualitySupport.areUserAttributesIdentical(geometries, geometries2);
            } else {
                return geometries == geometries2;
            }
        }

        private static boolean areUserAttributesIdentical(Collection<Geometry>  geometries,
                                                  Collection<Geometry> geometries2) {
            // first pass - build lookup based on Geometry and UserData
            Map<Integer, List<Geometry>> l1GeometryByHashCode = new HashMap<>();
            for (Geometry geom : geometries) {
                final int key = getLookupId(geom);
                if (!l1GeometryByHashCode.containsKey(key)) {
                    l1GeometryByHashCode.put(key, Arrays.asList(geom));
                } else {
                    // hash collision
                    l1GeometryByHashCode.get(key).add(geom);
                }
            }

            // second pass - verify each geometry against l1
            for (Geometry l2Geometry : geometries2) {
                int key = getLookupId(l2Geometry);
                if (! (l1GeometryByHashCode.containsKey(key)
                    && contains(l2Geometry, l1GeometryByHashCode.get(key)))) {
                    // user data is different
                    return false;
                }
            }
            return true;
        }

        private static boolean contains(Geometry geometry, List<Geometry> items) {
            // we must check that one of those geometries matches!
            for (Geometry item : items) {
                if (Objects.equals(item, geometry)) {
                    Object a = item.getUserData();
                    Object b = geometry.getUserData();

                    if ((a == b) || (a != null && a.equals(b))) {
                        return true; // this is good
                    }
                }
            }
            return false;
        }

        private static int getLookupId(Geometry geom) {
            return 31 * geom.hashCode()
                + (geom.getUserData() != null ? geom.getUserData().hashCode() : 0);
        }
    }
}

