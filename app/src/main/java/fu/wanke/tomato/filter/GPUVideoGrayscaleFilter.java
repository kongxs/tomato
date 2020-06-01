package fu.wanke.tomato.filter;

public class GPUVideoGrayscaleFilter extends GPUVideoFilter {
    public static final String GRAYSCALE_FRAGMENT_SHADER = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision highp float;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "uniform samplerExternalOES inputTexture;\n" +
            "\n" +
            "const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "  lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);\n" +
            "  float luminance = dot(textureColor.rgb, W);\n" +
            "\n" +
            "  gl_FragColor = vec4(vec3(luminance), textureColor.a);\n" +
            "}";

    public GPUVideoGrayscaleFilter() {
        super(NO_FILTER_VERTEX_SHADER, GRAYSCALE_FRAGMENT_SHADER);
    }
}
