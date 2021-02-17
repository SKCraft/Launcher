package com.skcraft.launcher.auth;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skcraft.launcher.dialog.component.ListListenerReducer;
import com.skcraft.launcher.persistence.Scrambled;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.RandomStringUtils;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.List;

/**
 * Persisted account list
 */
@Scrambled("ACCOUNT_LIST_NOT_SECURITY!")
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountList implements ListModel<SavedSession> {
	private List<SavedSession> accounts = Lists.newArrayList();
	private String clientId = RandomStringUtils.randomAlphanumeric(24);

	@JsonIgnore private final ListListenerReducer listeners = new ListListenerReducer();

	public synchronized void add(SavedSession session) {
		accounts.add(session);

		int index = accounts.size() - 1;
		listeners.intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index));
	}

	public synchronized void remove(SavedSession session) {
		int index = accounts.indexOf(session);

		if (index > -1) {
			accounts.remove(index);
			listeners.intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index));
		}
	}

	public synchronized void update(SavedSession newSavedSession) {
		int index = accounts.indexOf(newSavedSession);

		if (index > -1) {
			accounts.set(index, newSavedSession);
			listeners.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index));
		} else {
			this.add(newSavedSession);
		}
	}

	@Override
	public int getSize() {
		return accounts.size();
	}

	@Override
	public SavedSession getElementAt(int index) {
		return accounts.get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.addListDataListener(l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.removeListDataListener(l);
	}
}
