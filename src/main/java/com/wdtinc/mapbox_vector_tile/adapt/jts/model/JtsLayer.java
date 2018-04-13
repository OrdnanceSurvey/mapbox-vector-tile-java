package com.wdtinc.mapbox_vector_tile.adapt.jts.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final List<Geometry> geometries;

    /**
     * Create an empty JTS layer.
     *
     * @param name layer name
     * @throws IllegalArgumentException when {@code name} is null
     */
    public JtsLayer(String name) {
        this(name, Collections.emptyList());
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
        this.geometries = Collections.unmodifiableList(new ArrayList<>(geometries));
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
        result = 31 * result + (geometries != null ? GeometryEqualitySupport.hash(geometries) : 0);
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
        checkAttributes(geometries);
    }

    private static void checkAttributes(Collection<Geometry> geometries) {
        for (Geometry g : geometries) {
            if (g.getUserData() != null) {
                if (g.getUserData() instanceof Map) {
                    Map attr = (Map) g.getUserData();
                    for (Object key : attr.keySet()) {
                        if (!(key instanceof String)) {
                            throw new IllegalArgumentException("keys must be strings. "
                                + "Unacceptable" + key);
                        }
                    }

                    for (Object value : attr.values()) {
                        if (value instanceof Integer) {
                            throw new IllegalArgumentException("Mapbox specification mandates "
                                + "int64 (Java long).  Unacceptable: " + value);
                        }
                    }
                }
            }
        }
    }

    private static class GeometryEqualitySupport {

        static int hash(Collection<Geometry> geoms) {
            ArrayList<Geometry> list = new ArrayList<>(geoms);
            Comparator<Geometry> c = new Comparator<Geometry>() {
                @Override
                public int compare(Geometry o1, Geometry o2) {
                    return o1.compareTo(o2);
                }
            };
            list.sort(c);

            long result = 17;
            for (Geometry g : geoms) {
                result = 31 * result +  (g != null ? g.hashCode() : 0);
            }
            return (int) result;
        }

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
                return geometries2 != null
                    && areGeometriesSame(geometries, geometries2)
                    && areUserAttributesIdentical(geometries, geometries2);
            } else {
                return geometries == geometries2;
            }
        }

        private static boolean areGeometriesSame(Collection<Geometry>  geometries,
                                                 Collection<Geometry> geometries2) {
            return geometries.containsAll(geometries2) && geometries.containsAll(geometries2);
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
                boolean keyAndValueMatch = l1GeometryByHashCode.containsKey(key)
                    && contains(l2Geometry, l1GeometryByHashCode.get(key));
                if (!keyAndValueMatch) {
                    // user data is different
                    return false;
                }
            }
            return true;
        }

        private static boolean contains(Geometry geometry, List<Geometry> items) {
            // we must check that one of those geometries matches!
            for (Geometry item : items) {
                if (item.equals(geometry)) {
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

