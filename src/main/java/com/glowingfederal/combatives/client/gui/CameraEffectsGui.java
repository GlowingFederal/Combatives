package com.glowingfederal.combatives.client.gui;

import com.glowingfederal.combatives.config.CameraVisualSetting;
import com.glowingfederal.combatives.config.CombativesConfig;
import com.glowingfederal.combatives.config.CombativesConfigDefaults;
import cpw.mods.fml.client.config.GuiSlider;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Live client presentation controls. No gameplay property or packet is accessible here. */
public final class CameraEffectsGui extends GuiScreen implements GuiSlider.ISlider {
    private final GuiScreen parent;
    private int page, pageSize, pages;
    private final CameraVisualSetting[] settings = CameraVisualSetting.values();
    private static final String[] FEATURES = { "Camera", "Procedural bob", "Movement lean", "Movement FOV",
            "Camera rotations", "Explosion shake", "Landing", "Freefall", "Inertia", "Collision", "Crawl", "Horse" };
    private static final boolean[] FEATURE_DEFAULTS = { CombativesConfigDefaults.ENABLE_COMBATIVES_CAMERA,
            CombativesConfigDefaults.ENABLE_PROCEDURAL_BOB, CombativesConfigDefaults.ENABLE_MOVEMENT_LEAN,
            CombativesConfigDefaults.ENABLE_MOVEMENT_FOV, CombativesConfigDefaults.ENABLE_CAMERA_ROTATIONS,
            CombativesConfigDefaults.ENABLE_CAMERA_SHAKE, CombativesConfigDefaults.ENABLE_LANDING_CAMERA_FEEDBACK,
            CombativesConfigDefaults.ENABLE_PLAYER_FREEFALL_CAMERA, CombativesConfigDefaults.ENABLE_PLAYER_INERTIA_CAMERA,
            CombativesConfigDefaults.ENABLE_PLAYER_COLLISION_CAMERA, CombativesConfigDefaults.ENABLE_CRAWL_CAMERA,
            CombativesConfigDefaults.ENABLE_HORSE_CAMERA };

    public CameraEffectsGui(GuiScreen parent) { this.parent = parent; }

    @Override public void initGui() {
        buttonList.clear();
        pageSize = Math.max(1, (height - 112) / 24) * 2;
        pages = (settings.length + FEATURES.length + pageSize - 1) / pageSize;
        page = Math.min(page, pages - 1);
        for (int slot = 0, index = page * pageSize; slot < pageSize && index < settings.length + FEATURES.length; slot++, index++) {
            int x = width / 2 - 155 + (slot % 2) * 160, y = 54 + (slot / 2) * 24;
            if (index < settings.length) {
                CameraVisualSetting s = settings[index];
                GuiSlider slider = new GuiSlider(index, x, y, 150, 20, s.label + ": ", "", s.min, s.max, s.get(), true, true, this);
                slider.precision = 2;
                buttonList.add(slider);
            } else {
                int feature = index - settings.length;
                buttonList.add(new GuiButton(index, x, y, 150, 20, FEATURES[feature] + ": " + (enabled(feature) ? "On" : "Off")));
            }
        }
        buttonList.add(new GuiButton(1000, width / 2 - 155, height - 52, 70, 20, "Previous"));
        buttonList.add(new GuiButton(1001, width / 2 + 85, height - 52, 70, 20, "Next"));
        buttonList.add(new GuiButton(1002, width / 2 - 155, height - 28, 150, 20, "Reset to Defaults"));
        buttonList.add(new GuiButton(1003, width / 2 + 5, height - 28, 150, 20, "Done"));
    }

    @Override public void onChangeSliderValue(GuiSlider slider) { settings[slider.id].set((float) slider.getValue()); }

    @Override protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        if (button == 0) CombativesConfig.saveCameraSettings();
    }

    @Override protected void actionPerformed(GuiButton button) {
        if (button.id < settings.length) return;
        if (button.id < settings.length + FEATURES.length) {
            int feature = button.id - settings.length;
            setEnabled(feature, !enabled(feature));
        } else if (button.id == 1000) page = (page + pages - 1) % pages;
        else if (button.id == 1001) page = (page + 1) % pages;
        else if (button.id == 1002) {
            for (CameraVisualSetting s : settings) s.set(s.defaultValue);
            for (int feature = 0; feature < FEATURES.length; feature++) {
                setEnabled(feature, FEATURE_DEFAULTS[feature]);
            }
        } else if (button.id == 1003) { mc.displayGuiScreen(parent); return; }
        CombativesConfig.saveCameraSettings();
        initGui();
    }

    @Override public void drawScreen(int x, int y, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Combatives Camera Effects", width / 2, 12, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "Visual only - aim, spread and collision unchanged", width / 2, 30, 0xBBBBBB);
        drawCenteredString(fontRendererObj, "Page " + (page + 1) + " / " + pages, width / 2, height - 46, 0xFFFFFF);
        super.drawScreen(x, y, partialTicks);
        for (Object entry : buttonList) {
            GuiButton button = (GuiButton) entry;
            if (button.id < settings.length && x >= button.xPosition && x < button.xPosition + button.width
                    && y >= button.yPosition && y < button.yPosition + button.height) {
                CameraVisualSetting s = settings[button.id];
                List<String> tooltip = fontRendererObj.listFormattedStringToWidth(s.description + " Default: " + s.defaultValue, 220);
                drawHoveringText(tooltip, x, y, fontRendererObj);
            }
        }
    }

    @Override protected void keyTyped(char character, int key) {
        if (key == 1) mc.displayGuiScreen(parent);
        else super.keyTyped(character, key);
    }
    @Override public void onGuiClosed() { CombativesConfig.saveCameraSettings(); }
    @Override public boolean doesGuiPauseGame() { return false; }

    private static boolean enabled(int i) {
        switch (i) {
            case 0: return CombativesConfig.enableCombativesCamera;
            case 1: return CombativesConfig.enableProceduralBob;
            case 2: return CombativesConfig.enableMovementLean;
            case 3: return CombativesConfig.enableMovementFov;
            case 4: return CombativesConfig.enableCameraRotations;
            case 5: return CombativesConfig.enableCameraShake;
            case 6: return CombativesConfig.enableLandingCameraFeedback;
            case 7: return CombativesConfig.enablePlayerFreefallCamera;
            case 8: return CombativesConfig.enablePlayerInertiaCamera;
            case 9: return CombativesConfig.enablePlayerCollisionCamera;
            case 10: return CombativesConfig.enableCrawlCamera;
            default: return CombativesConfig.enableHorseCamera;
        }
    }
    private static void setEnabled(int i, boolean value) {
        switch (i) {
            case 0: CombativesConfig.enableCombativesCamera = value; break;
            case 1: CombativesConfig.enableProceduralBob = value; break;
            case 2: CombativesConfig.enableMovementLean = value; break;
            case 3: CombativesConfig.enableMovementFov = value; break;
            case 4: CombativesConfig.enableCameraRotations = value; break;
            case 5: CombativesConfig.enableCameraShake = value; CombativesConfig.enableExplosionCameraFeedback = value; break;
            case 6: CombativesConfig.enableLandingCameraFeedback = value; break;
            case 7: CombativesConfig.enablePlayerFreefallCamera = value; break;
            case 8: CombativesConfig.enablePlayerInertiaCamera = value; break;
            case 9: CombativesConfig.enablePlayerCollisionCamera = value; break;
            case 10: CombativesConfig.enableCrawlCamera = value; break;
            default: CombativesConfig.enableHorseCamera = value;
        }
    }
}
