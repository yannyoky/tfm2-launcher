package io.github.yannyoky.tfm2launcher;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LaunchConfigTest {

    @Test
    public void recommendedSetsResolutionAndBox64Only() {
        assertEquals("{\"screenSize\":\"1920x1080\",\"box64Preset\":\"STABILITY\"}",
                LaunchConfig.recommended().toContainerConfigJson());
    }

    @Test
    public void allAutoGivesEmptyObject() {
        assertEquals("{}", new LaunchConfig("", "", "", false).toContainerConfigJson());
    }

    @Test
    public void dxWrapperAndFps() {
        assertEquals("{\"dxwrapper\":\"wined3d\",\"showFPS\":true}",
                new LaunchConfig("", "wined3d", "", true).toContainerConfigJson());
    }

    @Test
    public void unknownValueFallsBackToAuto() {
        assertEquals(0, LaunchConfig.indexOf(LaunchConfig.DX_WRAPPERS, "vulkan9000"));
    }
}
