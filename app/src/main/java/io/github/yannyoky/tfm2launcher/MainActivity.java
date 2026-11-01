package io.github.yannyoky.tfm2launcher;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    /** Official build first, then the "gold" variant. */
    private static final String[] GAMENATIVE_PACKAGES = {"app.gamenative", "app.gamenative.gold"};
    private static final String LAUNCH_ACTION = "app.gamenative.LAUNCH_GAME";
    private static final String GAMENATIVE_RELEASES = "https://github.com/utkarshdalal/GameNative/releases/latest";
    private static final String STEAM_PAGE = "https://store.steampowered.com/app/" + LaunchConfig.STEAM_APP_ID + "/";

    private static final String PREFS = "launch";
    private static final String KEY_RES = "resolution";
    private static final String KEY_DX = "dxwrapper";
    private static final String KEY_BOX64 = "box64";
    private static final String KEY_FPS = "fps";

    private Spinner resolution;
    private Spinner dxWrapper;
    private Spinner box64Preset;
    private CheckBox showFps;
    private TextView status;
    private Button play;
    private Button openGameNative;
    private boolean binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.status);
        play = findViewById(R.id.play);
        openGameNative = findViewById(R.id.open_gamenative);
        resolution = findViewById(R.id.resolution);
        dxWrapper = findViewById(R.id.dx_wrapper);
        box64Preset = findViewById(R.id.box64_preset);
        showFps = findViewById(R.id.show_fps);

        setOptions(resolution, new String[]{"Авто (как в GameNative)", "1920×1080", "2560×1440", "1600×900", "1280×720 (рекомендуется)"});
        setOptions(dxWrapper, new String[]{"Авто (как в GameNative)", "DXVK", "WineD3D (если чёрный экран)"});
        setOptions(box64Preset, new String[]{"Авто (как в GameNative)", "Stability (рекомендуется)", "Compatibility", "Intermediate", "Performance"});

        bind(loadConfig());

        AdapterView.OnItemSelectedListener save = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { saveFromUi(); }
            @Override public void onNothingSelected(AdapterView<?> p) { }
        };
        resolution.setOnItemSelectedListener(save);
        dxWrapper.setOnItemSelectedListener(save);
        box64Preset.setOnItemSelectedListener(save);
        showFps.setOnCheckedChangeListener((b, checked) -> saveFromUi());

        play.setOnClickListener(v -> launchGame());
        openGameNative.setOnClickListener(v -> openGameNative());
        findViewById(R.id.get_gamenative).setOnClickListener(v -> openUrl(GAMENATIVE_RELEASES));
        findViewById(R.id.steam_page).setOnClickListener(v -> openUrl(STEAM_PAGE));
        findViewById(R.id.reset).setOnClickListener(v -> {
            bind(LaunchConfig.recommended());
            saveFromUi();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void refreshStatus() {
        String pkg = findGameNative();
        boolean installed = pkg != null;
        status.setText(installed ? getString(R.string.status_ok, pkg) : getString(R.string.status_missing));
        play.setEnabled(installed);
        openGameNative.setEnabled(installed);
    }

    private String findGameNative() {
        PackageManager pm = getPackageManager();
        for (String pkg : GAMENATIVE_PACKAGES) {
            try {
                pm.getPackageInfo(pkg, 0);
                return pkg;
            } catch (PackageManager.NameNotFoundException ignored) {
                // try the next one
            }
        }
        return null;
    }

    private void launchGame() {
        String pkg = findGameNative();
        if (pkg == null) {
            refreshStatus();
            return;
        }
        LaunchConfig config = currentConfig();
        Intent intent = new Intent(LAUNCH_ACTION)
                .setPackage(pkg)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("app_id", LaunchConfig.STEAM_APP_ID)
                .putExtra("game_source", LaunchConfig.GAME_SOURCE);
        String json = config.toContainerConfigJson();
        if (!"{}".equals(json)) {
            intent.putExtra("container_config", json);
        }
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.launch_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void openGameNative() {
        String pkg = findGameNative();
        Intent intent = pkg == null ? null : getPackageManager().getLaunchIntentForPackage(pkg);
        if (intent != null) {
            startActivity(intent);
        } else {
            refreshStatus();
        }
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, url, Toast.LENGTH_LONG).show();
        }
    }

    private void setOptions(Spinner spinner, String[] labels) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void bind(LaunchConfig c) {
        binding = true;
        resolution.setSelection(LaunchConfig.indexOf(LaunchConfig.RESOLUTIONS, c.resolution));
        dxWrapper.setSelection(LaunchConfig.indexOf(LaunchConfig.DX_WRAPPERS, c.dxWrapper));
        box64Preset.setSelection(LaunchConfig.indexOf(LaunchConfig.BOX64_PRESETS, c.box64Preset));
        showFps.setChecked(c.showFps);
        binding = false;
    }

    private LaunchConfig currentConfig() {
        return new LaunchConfig(
                LaunchConfig.RESOLUTIONS[resolution.getSelectedItemPosition()],
                LaunchConfig.DX_WRAPPERS[dxWrapper.getSelectedItemPosition()],
                LaunchConfig.BOX64_PRESETS[box64Preset.getSelectedItemPosition()],
                showFps.isChecked());
    }

    private LaunchConfig loadConfig() {
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (!p.contains(KEY_RES)) return LaunchConfig.recommended();
        return new LaunchConfig(
                p.getString(KEY_RES, LaunchConfig.AUTO),
                p.getString(KEY_DX, LaunchConfig.AUTO),
                p.getString(KEY_BOX64, LaunchConfig.AUTO),
                p.getBoolean(KEY_FPS, false));
    }

    private void saveFromUi() {
        if (binding) return;
        LaunchConfig c = currentConfig();
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString(KEY_RES, c.resolution)
                .putString(KEY_DX, c.dxWrapper)
                .putString(KEY_BOX64, c.box64Preset)
                .putBoolean(KEY_FPS, c.showFps)
                .apply();
    }
}
