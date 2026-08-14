package engine.graph;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import org.lwjgl.nanovg.NSVGImage;
import static org.lwjgl.nanovg.NanoSVG.nsvgCreateRasterizer;
import static org.lwjgl.nanovg.NanoSVG.nsvgDelete;
import static org.lwjgl.nanovg.NanoSVG.nsvgDeleteRasterizer;
import static org.lwjgl.nanovg.NanoSVG.nsvgParse;
import static org.lwjgl.nanovg.NanoSVG.nsvgRasterize;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.system.MemoryUtil.NULL;

import engine.Utils;

// Rasterizes SVG resources into OpenGL textures with NanoSVG. Rasterizing at
// load time keeps the icons independent of the size they were authored at.
public class SvgLoader {

    private static final float DPI = 96.0f;

    private SvgLoader() {
        // Utility class
    }

    // Renders the SVG into a size x size RGBA texture, scaled to fit and centered
    // so the icon keeps its aspect ratio.
    public static Texture load(String resourcePath, int size) {
        String svgSource = Utils.readFileFromResources(resourcePath);

        NSVGImage svg = nsvgParse(svgSource, "px", DPI);
        if (svg == null) {
            throw new RuntimeException("Failed to parse SVG [" + resourcePath + "]");
        }

        long rasterizer = nsvgCreateRasterizer();
        if (rasterizer == NULL) {
            nsvgDelete(svg);
            throw new RuntimeException("Failed to create the SVG rasterizer");
        }

        ByteBuffer pixels = MemoryUtil.memAlloc(size * size * 4);
        try {
            float scale = Math.min(size / svg.width(), size / svg.height());
            float offsetX = (size - svg.width() * scale) / 2.0f;
            float offsetY = (size - svg.height() * scale) / 2.0f;

            nsvgRasterize(rasterizer, svg, offsetX, offsetY, scale, pixels, size, size, size * 4);
            return new Texture(size, size, pixels, GL_LINEAR);
        } finally {
            MemoryUtil.memFree(pixels);
            nsvgDeleteRasterizer(rasterizer);
            nsvgDelete(svg);
        }
    }

}
