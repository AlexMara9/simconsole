import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ProcessImage {
    public static void main(String[] args) throws Exception {
        File inputFile = new File("src/main/resources/org/simconsole/simconsole/img/search_placeholder.png");
        BufferedImage img = ImageIO.read(inputFile);
        
        int width = img.getWidth();
        int height = img.getHeight();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = img.getRGB(x, y);
                Color color = new Color(pixel, true);
                
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                
                // Calculate brightness (0 to 255)
                int brightness = (r + g + b) / 3;
                
                // We want the original black lines (brightness ~0) to become light gray (e.g., 200, 200, 200)
                // and the white background (brightness ~255) to become completely transparent.
                // Alpha is based on how dark the original pixel is.
                int alpha = 255 - brightness;
                
                // Increase alpha a bit for better visibility of anti-aliased edges
                alpha = Math.min(255, (int)(alpha * 1.5));
                
                if (alpha < 10) {
                    img.setRGB(x, y, new Color(0, 0, 0, 0).getRGB());
                } else {
                    // Gray-white color (200, 200, 200) with calculated alpha
                    img.setRGB(x, y, new Color(200, 200, 200, alpha).getRGB());
                }
            }
        }
        
        ImageIO.write(img, "png", new File("src/main/resources/org/simconsole/simconsole/img/search_placeholder_processed.png"));
    }
}
