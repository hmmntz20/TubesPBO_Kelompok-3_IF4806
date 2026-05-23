package pbo.backend.graph.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pbo.backend.graph.api.dto.GraphMetaResponse;
import pbo.backend.graph.domain.CampusGraph;
import pbo.backend.graph.service.GraphService;

/**
 * Endpoint metadata untuk graf kampus.
 *
 * <p>Smoke-test fase fondasi: memungkinkan klien (frontend) memverifikasi
 * bahwa parsing graf berhasil dan menampilkan ringkasan ukuran tanpa harus
 * mengunduh keseluruhan GeoJSON.</p>
 *
 * @see GraphService          orchestration parser + cache
 * @see GraphMetaResponse     bentuk respons JSON
 */
@RestController
@RequestMapping("/api/v1/graph")
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    /**
     * Mengembalikan ringkasan graf yang sudah di-cache di {@link GraphService}.
     *
     * @return DTO ringkasan; respons selalu konsisten karena graf immutable.
     */
    @GetMapping("/meta")
    public GraphMetaResponse meta() {
        CampusGraph g = graphService.graph();

        // Bila graf kosong, BBox tidak tersedia → fallback ke array nol.
        // Di praktik dengan telmap.geojson saat ini, BBox selalu ada.
        var bbox = g.boundingBox().map(b -> new GraphMetaResponse.BBox(
                new double[] { b.northEast().latitude(), b.northEast().longitude() },
                new double[] { b.southWest().latitude(), b.southWest().longitude() }
        )).orElseGet(() -> new GraphMetaResponse.BBox(new double[2], new double[2]));

        return new GraphMetaResponse(
                g.nodeCount(),
                g.edgeCount(),
                bbox,
                g.builtAt(),
                graphService.parserName());
    }
}
