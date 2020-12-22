/*
 * SKCraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.skin;

import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.SubstanceColorSchemeBundle;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.SubstanceSlices;
import org.pushingpixels.substance.api.colorscheme.ColorSchemeSingleColorQuery;
import org.pushingpixels.substance.api.colorscheme.ColorSchemeTransform;
import org.pushingpixels.substance.api.colorscheme.SubstanceColorScheme;
import org.pushingpixels.substance.api.painter.border.ClassicBorderPainter;
import org.pushingpixels.substance.api.painter.border.CompositeBorderPainter;
import org.pushingpixels.substance.api.painter.border.DelegateBorderPainter;
import org.pushingpixels.substance.api.painter.decoration.FlatDecorationPainter;
import org.pushingpixels.substance.api.painter.fill.FractionBasedFillPainter;
import org.pushingpixels.substance.api.painter.highlight.ClassicHighlightPainter;
import org.pushingpixels.substance.api.skin.GraphiteSkin;

public class LauncherSkin extends GraphiteSkin {

    public LauncherSkin() {
        ColorSchemes schemes = SubstanceSkin.getColorSchemes(
                this.getClass().getResourceAsStream("graphite.colorschemes"));

        SubstanceColorScheme activeScheme = schemes.get("Graphite Active");
        SubstanceColorScheme selectedDisabledScheme = schemes.get("Graphite Selected Disabled");
        SubstanceColorScheme selectedScheme = schemes.get("Graphite Selected");
        SubstanceColorScheme disabledScheme = schemes.get("Graphite Disabled");
        SubstanceColorScheme enabledScheme = schemes.get("Graphite Enabled");
        SubstanceColorScheme backgroundScheme = schemes.get("Graphite Background");
        SubstanceColorScheme highlightScheme = schemes.get("Graphite Highlight");
        SubstanceColorScheme borderScheme = schemes.get("Graphite Border");
        SubstanceColorScheme separatorScheme = schemes.get("Graphite Separator");
        SubstanceColorScheme textHighlightScheme = schemes.get("Graphite Text Highlight");
        SubstanceColorScheme highlightMarkScheme = schemes.get("Graphite Highlight Mark");
        SubstanceColorScheme tabHighlightScheme = schemes.get("Graphite Tab Highlight");

        SubstanceColorSchemeBundle scheme = new SubstanceColorSchemeBundle(activeScheme, enabledScheme, disabledScheme);

        // highlight fill scheme + custom alpha for rollover unselected state
        scheme.registerHighlightColorScheme(highlightScheme,
                ComponentState.ROLLOVER_UNSELECTED, ComponentState.SELECTED, ComponentState.ROLLOVER_SELECTED,
                ComponentState.ARMED, ComponentState.ROLLOVER_ARMED
        );

        scheme.registerHighlightAlpha(0.6f, ComponentState.ROLLOVER_UNSELECTED);
        scheme.registerHighlightAlpha(0.8f, ComponentState.SELECTED);
        scheme.registerHighlightAlpha(1.0f, ComponentState.ROLLOVER_SELECTED);
        scheme.registerHighlightAlpha(0.75f, ComponentState.ARMED, ComponentState.ROLLOVER_ARMED);

        // highlight border scheme
        scheme.registerColorScheme(highlightScheme, SubstanceSlices.ColorSchemeAssociationKind.HIGHLIGHT_BORDER, ComponentState.getActiveStates());
        scheme.registerColorScheme(borderScheme, SubstanceSlices.ColorSchemeAssociationKind.BORDER);
        scheme.registerColorScheme(separatorScheme, SubstanceSlices.ColorSchemeAssociationKind.SEPARATOR);

        // text highlight scheme
        scheme.registerColorScheme(textHighlightScheme, SubstanceSlices.ColorSchemeAssociationKind.HIGHLIGHT_TEXT, ComponentState.SELECTED, ComponentState.ROLLOVER_SELECTED);
        scheme.registerColorScheme(highlightScheme, ComponentState.ARMED, ComponentState.ROLLOVER_ARMED);
        scheme.registerColorScheme(highlightMarkScheme, SubstanceSlices.ColorSchemeAssociationKind.HIGHLIGHT_MARK, ComponentState.getActiveStates());
        scheme.registerColorScheme(highlightMarkScheme, SubstanceSlices.ColorSchemeAssociationKind.MARK, ComponentState.ROLLOVER_SELECTED, ComponentState.ROLLOVER_UNSELECTED);
        scheme.registerColorScheme(borderScheme, SubstanceSlices.ColorSchemeAssociationKind.MARK, ComponentState.SELECTED);
        scheme.registerColorScheme(disabledScheme, ComponentState.DISABLED_UNSELECTED);
        scheme.registerColorScheme(selectedDisabledScheme, ComponentState.DISABLED_SELECTED);
        scheme.registerColorScheme(highlightScheme, ComponentState.ROLLOVER_SELECTED);
        scheme.registerColorScheme(selectedScheme, ComponentState.SELECTED);

        scheme.registerAlpha(0.5f, ComponentState.DISABLED_UNSELECTED);
        scheme.registerAlpha(0.65f, ComponentState.DISABLED_SELECTED);

        scheme.registerColorScheme(tabHighlightScheme, SubstanceSlices.ColorSchemeAssociationKind.TAB, ComponentState.ROLLOVER_SELECTED);

        this.registerDecorationAreaSchemeBundle(scheme, backgroundScheme, SubstanceSlices.DecorationAreaType.NONE);

        this.setTabFadeStart(0.1);
        this.setTabFadeEnd(0.3);

        this.buttonShaper = new LauncherButtonShaper();
        this.fillPainter = new FractionBasedFillPainter("Graphite",
                new float[] { 0.0f, 0.5f, 1.0f },
                new ColorSchemeSingleColorQuery[] {
                        ColorSchemeSingleColorQuery.ULTRALIGHT,
                        ColorSchemeSingleColorQuery.LIGHT,
                        ColorSchemeSingleColorQuery.LIGHT });
        this.decorationPainter = new FlatDecorationPainter();
        this.highlightPainter = new ClassicHighlightPainter();
        this.borderPainter = new CompositeBorderPainter("Graphite",
                new ClassicBorderPainter(), new DelegateBorderPainter(
                "Graphite Inner", new ClassicBorderPainter(),
                0xA0FFFFFF, 0x60FFFFFF, 0x60FFFFFF,
                new ColorSchemeTransform() {
                    @Override
                    public SubstanceColorScheme transform(
                            SubstanceColorScheme scheme) {
                        return scheme.tint(0.25f);
                    }
                }));

        this.highlightBorderPainter = new ClassicBorderPainter();
    }

}
