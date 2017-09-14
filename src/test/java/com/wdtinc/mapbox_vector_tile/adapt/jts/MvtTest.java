package com.wdtinc.mapbox_vector_tile.adapt.jts;

import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.*;

public class MvtTest {

    @Test
    public void testConstructor() {
        Layer layer1 = new Layer("first");
        Layer layer2 = new Layer("second");

        Mvt mvt = new Mvt(layer1, layer2);
        assertTrue(mvt.getLayers().containsAll(Arrays.asList(layer1, layer2)));

        mvt = new Mvt(Arrays.asList(layer1, layer2));
        assertTrue(mvt.getLayers().containsAll(Arrays.asList(layer1, layer2)));
    }

    @Test
    public void testLayerAddition() {
        Layer layer1 = new Layer("first");
        Layer layer2 = new Layer("second");

        Mvt mvt = new Mvt();
        mvt.addLayer(layer1);
        mvt.addLayer(layer2);

        assertTrue(mvt.getLayers().containsAll(Arrays.asList(layer1, layer2)));
    }

    @Test
    public void testLayerByName() {
        Layer layer1 = new Layer("first");
        Layer layer2 = new Layer("second");

        Mvt mvt = new Mvt();
        mvt.addLayer(layer1);
        mvt.addLayer(layer2);

        assertEquals(layer1, mvt.getLayer("first"));
        assertEquals(layer2, mvt.getLayer("second"));
    }

    @Test
    public void testLayerByIndex() {
        Layer layer1 = new Layer("first");
        Layer layer2 = new Layer("second");

        Mvt mvt = new Mvt();
        mvt.addLayer(layer1);
        mvt.addLayer(layer2);

        assertEquals(layer1, mvt.getLayer(0));
        assertEquals(layer2, mvt.getLayer(1));
    }

    @Test
    public void testEquality() {
        Layer layer1 = new Layer("first");
        Layer layer2 = new Layer("second");

        Mvt mvt = new Mvt();
        mvt.addLayer(layer1);
        mvt.addLayer(layer2);

        Layer duplicateLayer1 = new Layer("first");
        Layer duplicateLayer2 = new Layer("second");

        Mvt duplicateLayers = new Mvt();
        duplicateLayers.addLayer(duplicateLayer1);
        duplicateLayers.addLayer(duplicateLayer2);

        assertTrue(mvt.equals(duplicateLayers));

        mvt.addLayer(new Layer("extra"));
        assertFalse(mvt.equals(duplicateLayers));
    }

    @Test
    public void testAddRemoveLayers() {
        Layer layer = new Layer("example");
        Layer layer2 = new Layer("example2");

        Mvt mvt = new Mvt();
        mvt.addLayers(layer, layer2);
        mvt.getLayers().containsAll(Arrays.asList(layer, layer2));

        mvt.removeLayer(layer);
        mvt.getLayers().containsAll(Arrays.asList(layer2));
    }

    @Test
    public void testNoSuchLayer() {
        Layer layer = new Layer("example");

        Mvt mvt = new Mvt();
        mvt.addLayer(layer);

        Layer actual = mvt.getLayer("No Such Layer");
        Layer expected = null;
        assertEquals(expected, actual);
    }

    @Test
    public void testToString() {
        Layer layer = new Layer("example");

        Mvt mvt = new Mvt();
        mvt.addLayer(layer);

        String actual = mvt.toString();
        String expected = "Mvt{layers=[Layer{name='example', geometries=[]}]}";
        assertEquals(expected, actual);
    }

    @Test
    public void testHashcode() {
        Layer layer = new Layer("example");

        Mvt mvt = new Mvt();
        mvt.addLayer(layer);

        int actual = mvt.hashCode();
        int expected = 1937578998;
        assertEquals(expected, actual);
    }
}
