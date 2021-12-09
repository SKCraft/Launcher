package com.skcraft.launcher.dialog.component;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;

public class BetterComboBox<E> extends JComboBox<E> {
	public BetterComboBox() {
		setUI(new BetterComboBoxUI());
	}

	private static class BetterComboBoxUI extends BasicComboBoxUI {
		@Override
		protected ComboPopup createPopup() {
			BasicComboPopup popup = new BasicComboPopup(comboBox) {
				@Override
				protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
					return super.computePopupBounds(px, py, Math.max(comboBox.getPreferredSize().width, pw), ph);
				}
			};

			popup.getAccessibleContext().setAccessibleParent(comboBox);
			return popup;
		}
	}
}
