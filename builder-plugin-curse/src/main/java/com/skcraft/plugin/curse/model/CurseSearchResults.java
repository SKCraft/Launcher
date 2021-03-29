package com.skcraft.plugin.curse.model;

import com.beust.jcommander.internal.Lists;

import javax.swing.*;
import java.util.List;

public class CurseSearchResults extends AbstractListModel<CurseProject> {
	private List<CurseProject> results = Lists.newArrayList();

	@Override
	public int getSize() {
		return results.size();
	}

	@Override
	public CurseProject getElementAt(int index) {
		return results.get(index);
	}

	public void updateResults(List<CurseProject> newResults) {
		int before = results.size();

		results = newResults;
		this.fireContentsChanged(this, 0, before - 1);
	}
}
