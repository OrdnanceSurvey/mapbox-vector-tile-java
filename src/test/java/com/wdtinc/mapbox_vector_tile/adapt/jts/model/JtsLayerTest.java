package com.wdtinc.mapbox_vector_tile.adapt.jts.model;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class JtsLayerTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @Test
    public void testLayerName() {
        String layerName = "Points of Interest";
        JtsLayer layer = new JtsLayer(layerName);

        String actual = layer.getName();
        String expected = layerName;
        assertEquals(expected, actual);
    }

    @Test
    public void testLayerCollection() {
        // Using Collections (unmodifiableCollection) rather than Lists or Sets prevents the equals
        // and hashcode from working as one might expect.  Specifically, the Java equals contract
        // mandates that equality must be symmetric.  Lists and Sets can only be equal to their
        // respective types - and therefore a collection comparison would fail.
        // An UnmodifiableList is an UnmodifiableCollection BUT the same is not true in reverse.
        // See:
        // https://stackoverflow.com/questions/31733537/unmodifiable-collection-equality-in-java#answer-31733658
        // https://stackoverflow.com/questions/12851229/hashcode-and-equals-for-collections-unmodifiablecollection/12851469#12851469
        String layerName = "Points of Interest";
        List<Geometry> geometries = new ArrayList<>();

        JtsLayer layer = new JtsLayer(layerName, geometries);

        String actualName = layer.getName();
        String expectedName = layerName;
        assertEquals(expectedName, actualName);

        Collection<Geometry> actualGeometry = layer.getGeometries();
        Collection<Geometry> expectedGeometry = geometries;
        assertEquals(expectedGeometry, actualGeometry);
    }

    @Test
    public void testAddGeometry() {
        String layerName = "Points of Interest";

        Point point = createPoint(new int[]{51, 0});
        List<Geometry> geometries = new ArrayList<>();
        geometries.add(point);

        JtsLayer layer = new JtsLayer(layerName, geometries);
        assertTrue(layer.getGeometries().contains(point));
    }


    @Test
    public void testAddGeometries() {
        String layerName = "Points of Interest";
        List<Geometry> geometries = new ArrayList<>();

        Point point = createPoint(new int[]{50, 0});
        Point point2 = createPoint(new int[]{51, 1});
        Collection<Geometry> points = Arrays.asList(point, point2);
        geometries.addAll(points);

        JtsLayer layer = new JtsLayer(layerName, geometries);

        assertTrue(layer.getGeometries().containsAll(Arrays.asList(point, point2)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableLayerCollection() {
        String layerName = "Points of Interest";

        Point point = createPoint(new int[]{51, 0});
        List<Geometry> geometries = new ArrayList<>();
        geometries.add(point);

        JtsLayer layer = new JtsLayer(layerName, geometries);
        layer.getGeometries().add(point);
    }

    @Test
    public void testEquality() {
        JtsLayer layer1 = new JtsLayer("apples");
        JtsLayer layer1Duplicate = new JtsLayer("apples");
        assertTrue(layer1.equals(layer1Duplicate));

        JtsLayer layer2 = new JtsLayer("oranges");
        assertFalse(layer1.equals(layer2));
    }

    @Test
    public void testEqualityWithUserAttributes() {
        // two identical points
        Point point = createPoint(new int[]{50, 0});
        Point point2 = createPoint(new int[]{50, 0});

        // noise
        Point point3 = createPoint(new int[]{60, 10});

        // add attributes only to the first point
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("id", "test");
        point.setUserData(attributes);

        // define two layers (geometries are the same _but_ the attributes are not)
        JtsLayer layer = new JtsLayer("Point", Arrays.asList(point, point3));
        JtsLayer layer2 = new JtsLayer("Point", Arrays.asList(point2, point3));

        assertFalse(layer.equals(layer2));
    }

    @Test
    public void testEqualityWithoutUserAttributes() {
        // two identical points
        Point point = createPoint(new int[]{50, 0});
        Point point2 = createPoint(new int[]{50, 0});
        assertEquals(point.hashCode(), point2.hashCode());

        // noise
        Point point3 = createPoint(new int[]{60, 10});

        // define two layers (geometries are the same _but_ the attributes are not)
        JtsLayer layer = new JtsLayer("Point", Arrays.asList(point, point3));
        JtsLayer layer2 = new JtsLayer("Point", Arrays.asList(point2, point3));

        assertTrue(layer.equals(layer2));
    }

    @Test
    public void testEqualityLargeNullUserData() {
        // first set
        List<Geometry> geometries = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            geometries.add(createPointWithUserData(i));
        }

        // second set
        List<Geometry> geometries2 = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            geometries2.add(createPointWithUserData(i));
        }

        // make the second set different
        geometries2.get(0).setUserData(null);

        JtsLayer layer = new JtsLayer("Point", geometries);
        JtsLayer layer2 = new JtsLayer("Point", geometries2);
        assertFalse(layer.equals(layer2));
    }

    @Test
    public void testEqualityLargeDifferentUserData() {
        // first set
        List<Geometry> geometries = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            geometries.add(createPointWithUserData(i));
        }

        // second set
        List<Geometry> geometries2 = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            geometries2.add(createPointWithUserData(i));
        }

        // make the second set have different metadata
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("id", "DIFFERENT DATA!!!!");
        geometries2.get(0).setUserData(attributes);

        JtsLayer layer = new JtsLayer("Point", geometries);
        JtsLayer layer2 = new JtsLayer("Point", geometries2);
        assertFalse(layer.equals(layer2));
    }

    private Point createPointWithUserData(int id) {
        Point point = createPoint(new int[]{id, 0});
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("id", "test " + id);
        point.setUserData(attributes);
        return point;
    }

    @Test
    public void testToString() {
        JtsLayer layer1 = new JtsLayer("apples");
        String actual = layer1.toString();
        String expected = "Layer{name='apples', geometries=[]}";
        assertEquals(expected, actual);
    }

    @Test
    public void testHash() {
        JtsLayer layer = new JtsLayer("code");
        int actual = layer.hashCode();
        int expected = -1355094307;
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullName() {
        new JtsLayer(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullCollection() {
        new JtsLayer("apples", null);
    }

    private Point createPoint(int[] coordinates) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(coordinates[0], coordinates[1]));
    }
}
