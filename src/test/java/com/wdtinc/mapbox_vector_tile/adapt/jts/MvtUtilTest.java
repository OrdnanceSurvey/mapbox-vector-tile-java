package com.wdtinc.mapbox_vector_tile.adapt.jts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class MvtUtilTest {

    @Test
    public void singleLayer() throws IOException {
        Collection<Geometry> geometries = PointGen.australia();

        Layer layer = new Layer("animals", geometries);
        Mvt mvt = new Mvt(layer);

        byte[] encoded = MvtUtil.encode(mvt);

        Mvt actual = MvtUtil.decode(encoded);
        Mvt expected = mvt;

        assertEquals(expected, actual);
    }

    @Test
    public void multipleLayers() throws IOException {
        Layer layer = new Layer("Australia", PointGen.australia());
        Layer layer2 = new Layer("United Kingdom", PointGen.uk());
        Layer layer3 = new Layer("United States of America", PointGen.usa());
        Mvt mvt = new Mvt(layer, layer2, layer3);

        byte[] encoded = MvtUtil.encode(mvt);

        Mvt actual = MvtUtil.decode(encoded);
        Mvt expected = mvt;

        assertEquals(expected, actual);
    }

    private static class PointGen {

        /** Generate Geometries with this default specification */
        private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
        private static final Random RANDOM = new Random();

        private static Collection<Geometry> australia() {
            return getPoints(
                    createPoint("Koala"),
                    createPoint("Wombat"),
                    createPoint("Platypus"),
                    createPoint("Dingo"),
                    createPoint("Croc"));
        }

        private static Collection<Geometry> uk() {
            return getPoints(
                    createPoint("Hare"),
                    createPoint("Frog"),
                    createPoint("Robin"),
                    createPoint("Fox"),
                    createPoint("Hedgehog"),
                    createPoint("Bulldog"));
        }

        private static Collection<Geometry> usa() {
            return getPoints(
                    createPoint("Cougar"),
                    createPoint("Raccoon"),
                    createPoint("Beaver"),
                    createPoint("Wolf"),
                    createPoint("Bear"),
                    createPoint("Coyote"));
        }

        private static Collection<Geometry> getPoints(Point... points) {
            return Arrays.asList(points);
        }

        private static Point createPoint(String name) {
            Coordinate coord = new Coordinate( (int) (RANDOM.nextDouble() * 4095),
                    (int) (RANDOM.nextDouble() * 4095));
            Point point = GEOMETRY_FACTORY.createPoint(coord);

            Map<String, Object> attributes = new LinkedHashMap<>();
            attributes.put("id", name.hashCode());
            attributes.put("name", name);
            point.setUserData(attributes);

            return point;
        }
    }
}
