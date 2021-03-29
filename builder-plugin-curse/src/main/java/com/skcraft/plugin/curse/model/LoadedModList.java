package com.skcraft.plugin.curse.model;

import com.google.common.collect.Lists;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public class LoadedModList extends AbstractListModel<LoadedMod> {
	private List<LoadedMod> mods = Lists.newArrayList();

	@Override
	public int getSize() {
		return mods.size();
	}

	@Override
	public LoadedMod getElementAt(int index) {
		return mods.get(index);
	}

	public void addAll(Collection<? extends LoadedMod> other) {
		int start = mods.size();
		mods.addAll(other);
		fireIntervalAdded(this, start, start + other.size());
	}

	public void add(LoadedMod mod) {
		mods.add(mod);

		int idx = mods.size() - 1;
		fireIntervalAdded(this, idx, idx);
	}

	public void remove(LoadedMod mod) {
		int index = mods.indexOf(mod);
		
		if (index > -1) {
			mods.remove(index);
			fireIntervalRemoved(this, index, index);
		}
	}
}
