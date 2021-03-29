package com.skcraft.plugin.curse.model;

import com.google.common.collect.Lists;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public class AddedModList extends AbstractListModel<AddedMod> {
	private List<AddedMod> mods = Lists.newArrayList();

	@Override
	public int getSize() {
		return mods.size();
	}

	@Override
	public AddedMod getElementAt(int index) {
		return mods.get(index);
	}

	public void addAll(Collection<? extends AddedMod> other) {
		int start = mods.size();
		mods.addAll(other);
		fireIntervalAdded(this, start, start + other.size());
	}

	public void add(AddedMod mod) {
		mods.add(mod);

		int idx = mods.size() - 1;
		fireIntervalAdded(this, idx, idx);
	}

	public void remove(AddedMod mod) {
		int index = mods.indexOf(mod);
		
		if (index > -1) {
			mods.remove(index);
			fireIntervalRemoved(this, index, index);
		}
	}
}
