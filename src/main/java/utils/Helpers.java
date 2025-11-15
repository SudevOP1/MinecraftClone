package utils;

public class Helpers {
    
    public static float[] hexToRGBAColor(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        if (hex.length() != 8) {
            return new float[] { 0f, 0f, 0f, 1f }; // default black
        }

        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        return new float[] { r / 255f, g / 255f, b / 255f, 1f };
    }

}
