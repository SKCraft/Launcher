package com.skcraft.launcher.dialog.component;

import com.beust.jcommander.internal.Lists;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.List;

public class ListListenerReducer implements ListDataListener {
	private final List<ListDataListener> listeners = Lists.newArrayList();

	@Override
	public void intervalAdded(ListDataEvent e) {
		listeners.forEach(it -> it.intervalAdded(e));
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		listeners.forEach(it -> it.intervalRemoved(e));
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
		listeners.forEach(it -> it.contentsChanged(e));
	}

	public void addListDataListener(ListDataListener l) {
		listeners.add(l);
	}

	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}
}
