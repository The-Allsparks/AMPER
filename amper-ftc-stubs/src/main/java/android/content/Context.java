package android.content;

/** Compile-only stub. Robot builds use the Android SDK. */
public class Context {
    public java.io.File getExternalFilesDir(String type) {
        return new java.io.File("build/amper-external", type == null ? "amper" : type);
    }
}
