import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Genera el icono .ico del instalador y del ejecutable a partir del logo del
 * negocio (el mismo de la factura). Escribe entradas de mapa de bits de 32 bits
 * en varios tamanos (16 a 256), que es lo que esperan Windows, jpackage e Inno.
 *
 * Uso:  java GenerarIcono <logo.jpg> <salida.ico>
 */
public class GenerarIcono {

    public static void main(String[] args) throws Exception {
        String entrada = args.length > 0 ? args[0] : "logo_empresa.jpg";
        String salida = args.length > 1 ? args[1] : "electroservis.ico";

        BufferedImage origen = ImageIO.read(new File(entrada));
        if (origen == null) throw new IOException("No se pudo leer la imagen: " + entrada);

        int[] tamanos = {16, 24, 32, 48, 64, 128, 256};
        List<byte[]> imagenes = new ArrayList<>();
        for (int t : tamanos) imagenes.add(dib(origen, t));

        // La cabecera del ICO va en little-endian (ojo: DataOutputStream escribe big-endian).
        int total = 6 + 16 * tamanos.length;
        for (byte[] datos : imagenes) total += datos.length;

        ByteBuffer out = ByteBuffer.allocate(total).order(ByteOrder.LITTLE_ENDIAN);
        out.putShort((short) 0);                  // reserved
        out.putShort((short) 1);                  // type = icono
        out.putShort((short) tamanos.length);     // cantidad

        int offset = 6 + 16 * tamanos.length;
        for (int i = 0; i < tamanos.length; i++) {
            int t = tamanos[i];
            out.put((byte) (t >= 256 ? 0 : t));   // ancho (0 = 256)
            out.put((byte) (t >= 256 ? 0 : t));   // alto
            out.put((byte) 0);                    // colores de la paleta
            out.put((byte) 0);                    // reservado
            out.putShort((short) 1);              // planos
            out.putShort((short) 32);             // bits por pixel
            out.putInt(imagenes.get(i).length);   // tamano de la imagen
            out.putInt(offset);                   // donde empieza
            offset += imagenes.get(i).length;
        }
        for (byte[] datos : imagenes) out.put(datos);

        try (FileOutputStream fos = new FileOutputStream(salida)) {
            fos.write(out.array());
        }

        System.out.println("Icono generado: " + salida + " (" + tamanos.length + " tamanos desde " + entrada + ")");
    }

    /** Arma una entrada de icono en formato DIB (BITMAPINFOHEADER + pixeles + mascara AND). */
    private static byte[] dib(BufferedImage src, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);
        g.drawImage(src, 0, 0, size, size, null);
        g.dispose();

        int filaMascara = ((size + 31) / 32) * 4;   // fila del AND, alineada a 4 bytes
        int xor = size * size * 4;
        int and = filaMascara * size;

        ByteBuffer bb = ByteBuffer.allocate(40 + xor + and).order(ByteOrder.LITTLE_ENDIAN);

        // BITMAPINFOHEADER
        bb.putInt(40);
        bb.putInt(size);
        bb.putInt(size * 2);        // alto x2: mapa de color + mascara
        bb.putShort((short) 1);
        bb.putShort((short) 32);
        bb.putInt(0);               // BI_RGB
        bb.putInt(xor);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(0);
        bb.putInt(0);

        // Pixeles: de abajo hacia arriba, en BGRA
        for (int y = size - 1; y >= 0; y--) {
            for (int x = 0; x < size; x++) {
                int argb = img.getRGB(x, y);
                bb.put((byte) (argb & 0xFF));
                bb.put((byte) ((argb >> 8) & 0xFF));
                bb.put((byte) ((argb >> 16) & 0xFF));
                bb.put((byte) ((argb >>> 24) & 0xFF));
            }
        }

        // Mascara AND en cero: se usa el canal alfa
        for (int i = 0; i < and; i++) bb.put((byte) 0);

        return bb.array();
    }
}
