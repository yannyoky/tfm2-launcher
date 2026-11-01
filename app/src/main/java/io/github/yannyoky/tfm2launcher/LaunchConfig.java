package io.github.yannyoky.tfm2launcher;

/**
 * Launch settings for TFM2 and the GameNative launch-intent payload built from them.
 *
 * GameNative accepts a "container_config" JSON extra on its app.gamenative.LAUNCH_GAME
 * intent and applies it as a temporary override. Keys we leave out keep GameNative's
 * own per-device defaults (it already picks the right Turnip driver for Adreno GPUs).
 */
public final class LaunchConfig {

    /** Steam app id of Teamfight Manager 2. */
    public static final int STEAM_APP_ID = 3009300;
    public static final String GAME_SOURCE = "STEAM";

    /** Empty string means "auto": the key is left out and GameNative decides. */
    public static final String AUTO = "";

    public static final String[] RESOLUTIONS = {AUTO, "1920x1080", "2560x1440", "1600x900", "1280x720"};
    public static final String[] DX_WRAPPERS = {AUTO, "dxvk", "wined3d"};
    public static final String[] BOX64_PRESETS = {AUTO, "STABILITY", "COMPATIBILITY", "INTERMEDIATE", "PERFORMANCE"};

    public String resolution;
    public String dxWrapper;
    public String box64Preset;
    public boolean showFps;

    public LaunchConfig(String resolution, String dxWrapper, String box64Preset, boolean showFps) {
        this.resolution = resolution == null ? AUTO : resolution;
        this.dxWrapper = dxWrapper == null ? AUTO : dxWrapper;
        this.box64Preset = box64Preset == null ? AUTO : box64Preset;
        this.showFps = showFps;
    }

    /** Recommended start point for a Snapdragon 8 Gen 3 phone such as the Galaxy S24 Ultra. */
    public static LaunchConfig recommended() {
        return new LaunchConfig("1920x1080", AUTO, "STABILITY", false);
    }

    /** JSON for the container_config extra. Values come only from the fixed lists above. */
    public String toContainerConfigJson() {
        StringBuilder sb = new StringBuilder("{");
        appendString(sb, "screenSize", resolution);
        appendString(sb, "dxwrapper", dxWrapper);
        appendString(sb, "box64Preset", box64Preset);
        if (showFps) {
            if (sb.length() > 1) sb.append(',');
            sb.append("\"showFPS\":true");
        }
        return sb.append('}').toString();
    }

    private static void appendString(StringBuilder sb, String key, String value) {
        if (value.isEmpty()) return;
        if (sb.length() > 1) sb.append(',');
        sb.append('"').append(key).append("\":\"").append(escape(value)).append('"');
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static int indexOf(String[] options, String value) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(value)) return i;
        }
        return 0;
    }
}
