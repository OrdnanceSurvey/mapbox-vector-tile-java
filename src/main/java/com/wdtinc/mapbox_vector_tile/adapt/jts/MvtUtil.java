package com.wdtinc.mapbox_vector_tile.adapt.jts;


import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.wdtinc.mapbox_vector_tile.VectorTile;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerBuild;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerParams;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Encode and decode Mapbox Vector Tiles
 *
 * TODO: consider
 * MvtDecoder / decode(byte[] bytes) : Mvt
 * MvtEncoder / encode(Mvt mvt) : byte[]
 */
public class MvtUtil {

    private static final MvtLayerParams DEFAULT_MVT_PARAMS = new MvtLayerParams();

    private static final GeometryFactory GEOMETRY_FACORY = new GeometryFactory();

    public static byte[] encode(Mvt mvt) {

        // Build MVT
        final VectorTile.Tile.Builder tileBuilder = VectorTile.Tile.newBuilder();

        for (Layer layer : mvt.getLayers()) {
            final Collection<Geometry> layerGeoms = layer.getGeometries();

            // Create MVT layer
            final VectorTile.Tile.Layer.Builder layerBuilder =
                    MvtLayerBuild.newLayerBuilder(layer.getName(), DEFAULT_MVT_PARAMS);
            final MvtLayerProps layerProps = new MvtLayerProps();
            final UserDataKeyValueMapConverter userData = new UserDataKeyValueMapConverter();

            // MVT tile geometry to MVT features
            final List<VectorTile.Tile.Feature> features =
                    JtsAdapter.toFeatures(layerGeoms, layerProps, userData);
            layerBuilder.addAllFeatures(features);
            MvtLayerBuild.writeProps(layerBuilder, layerProps);

            // Build MVT layer
            final VectorTile.Tile.Layer vtl = layerBuilder.build();
            tileBuilder.addLayers(vtl);
        }

        /// Build MVT
        return tileBuilder.build().toByteArray();
    }

    public static Mvt decode(byte[] bytes) throws IOException {
        return MvtReader.loadMvt(new ByteArrayInputStream(bytes), GEOMETRY_FACORY, new TagKeyValueMapConverter());
    }
}
