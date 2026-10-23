package canteen.ui.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public final class ThemeManager {

    public enum Theme { LIGHT, DARK }

    private static Theme currentTheme = Theme.LIGHT;

    static {
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");
        System.setProperty("flatlaf.uiScale", "1.0");
    }

    public static void setTheme(Theme theme) {
        currentTheme = theme;
        try {
            if (theme == Theme.DARK) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }
        } catch (Exception e) {
            System.err.println("Could not setup FlatLaf: " + e.getMessage());
        }
        applyThemeCustomizations();
    }

    public static Theme getTheme() {
        return currentTheme;
    }

    public static void toggleTheme() {
        setTheme(currentTheme == Theme.LIGHT ? Theme.DARK : Theme.LIGHT);
    }

    private static void applyThemeCustomizations() {
        // Modern rounded corners and refined control padding
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ScrollBar.thumbArc", 8);
        UIManager.put("ScrollBar.width", 10);

        UIManager.put("Panel.background", getBgPrimary());
        UIManager.put("Panel.foreground", getTextPrimary());
        UIManager.put("Label.foreground", getTextPrimary());

        UIManager.put("Table.background", getBgSecondary());
        UIManager.put("Table.foreground", getTextPrimary());
        UIManager.put("Table.gridColor", getBorderLight());
        UIManager.put("Table.selectionBackground", getAccent());
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("TableHeader.background", getBgTertiary());
        UIManager.put("TableHeader.foreground", getTextPrimary());
        UIManager.put("Table.rowHeight", 36);

        UIManager.put("TextField.background", getBgSecondary());
        UIManager.put("TextField.foreground", getTextPrimary());
        UIManager.put("TextField.caretForeground", getAccent());

        UIManager.put("ComboBox.background", getBgSecondary());
        UIManager.put("ComboBox.foreground", getTextPrimary());

        UIManager.put("List.background", getBgSecondary());
        UIManager.put("List.foreground", getTextPrimary());
        UIManager.put("List.selectionBackground", getAccent());
        UIManager.put("List.selectionForeground", Color.WHITE);
    }

    public static Color getBgPrimary() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.BG_PRIMARY : ColorPalette.Dark.BG_PRIMARY;
    }

    public static Color getBgSecondary() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.BG_SECONDARY : ColorPalette.Dark.BG_SECONDARY;
    }

    public static Color getBgTertiary() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.BG_TERTIARY : ColorPalette.Dark.BG_TERTIARY;
    }

    public static Color getBgHover() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.BG_HOVER : ColorPalette.Dark.BG_HOVER;
    }

    public static Color getBgSelected() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.BG_SELECTED : ColorPalette.Dark.BG_SELECTED;
    }

    public static Color getTextPrimary() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.TEXT_PRIMARY : ColorPalette.Dark.TEXT_PRIMARY;
    }

    public static Color getTextSecondary() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.TEXT_SECONDARY : ColorPalette.Dark.TEXT_SECONDARY;
    }

    public static Color getTextMuted() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.TEXT_MUTED : ColorPalette.Dark.TEXT_MUTED;
    }

    public static Color getBorderLight() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.BORDER_LIGHT : ColorPalette.Dark.BORDER_LIGHT;
    }

    public static Color getBorderMedium() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.BORDER_MEDIUM : ColorPalette.Dark.BORDER_MEDIUM;
    }

    public static Color getShadow() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.SHADOW : ColorPalette.Dark.SHADOW;
    }

    public static Color getAccent() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.ACCENT : ColorPalette.Dark.ACCENT;
    }

    public static Color getAccentHover() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.ACCENT_HOVER : ColorPalette.Dark.ACCENT_HOVER;
    }

    public static Color getAccentBg() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.ACCENT_BG : ColorPalette.Dark.ACCENT_BG;
    }

    public static Color getSuccess() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.SUCCESS : ColorPalette.Dark.SUCCESS;
    }

    public static Color getSuccessBg() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.SUCCESS_BG : ColorPalette.Dark.SUCCESS_BG;
    }

    public static Color getWarning() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.WARNING : ColorPalette.Dark.WARNING;
    }

    public static Color getWarningBg() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.WARNING_BG : ColorPalette.Dark.WARNING_BG;
    }

    public static Color getError() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.ERROR : ColorPalette.Dark.ERROR;
    }

    public static Color getErrorBg() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.ERROR_BG : ColorPalette.Dark.ERROR_BG;
    }

    public static Color getInfo() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.INFO : ColorPalette.Dark.INFO;
    }

    public static Color getInfoBg() {
        return currentTheme == Theme.LIGHT ? ColorPalette.Light.INFO_BG : ColorPalette.Dark.INFO_BG;
    }

    public static void refreshAllFrames() {
        FlatLaf.updateUI();
        for (Window window : Window.getWindows()) {
            if (window.isDisplayable()) {
                SwingUtilities.updateComponentTreeUI(window);
                window.repaint();
            }
        }
    }
}