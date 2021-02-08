package com.skcraft.launcher.dialog;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.LoginService;
import com.skcraft.launcher.auth.SavedSession;
import com.skcraft.launcher.auth.Session;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.LinedBoxPanel;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Callable;

public class AccountSelectDialog extends JDialog {
	private final JList<SavedSession> accountList;
	private final JButton loginButton = new JButton(SharedLocale.tr("login.login"));
	private final JButton cancelButton = new JButton(SharedLocale.tr("button.cancel"));
	private final JButton addAccountButton = new JButton(SharedLocale.tr("accounts.addMojang"));
	private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);

	private final Launcher launcher;
	private Session selected;

	public AccountSelectDialog(Window owner, Launcher launcher) {
		super(owner, ModalityType.DOCUMENT_MODAL);

		this.launcher = launcher;
		this.accountList = new JList<>(launcher.getAccounts());

		setTitle(SharedLocale.tr("accounts.title"));
		initComponents();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(420, 50));
		setResizable(false);
		pack();
		setLocationRelativeTo(owner);
	}

	private void initComponents() {
		accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		accountList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		accountList.setVisibleRowCount(0);
		accountList.setCellRenderer(new AccountRenderer());

		JScrollPane accountPane = new JScrollPane(accountList);
		accountPane.setPreferredSize(new Dimension(250, 100));
		accountPane.setAlignmentX(LEFT_ALIGNMENT);

		loginButton.setFont(loginButton.getFont().deriveFont(Font.BOLD));

		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(26, 13, 13, 13));
		buttonsPanel.addGlue();
		buttonsPanel.addElement(loginButton);
		buttonsPanel.addElement(cancelButton);

		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		listPane.add(accountPane);
		listPane.add(Box.createVerticalStrut(5));
		listPane.add(addAccountButton);
		listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		add(listPane, BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.SOUTH);

		loginButton.addActionListener(ev -> attemptLogin(accountList.getSelectedValue()));
		cancelButton.addActionListener(ev -> dispose());

		addAccountButton.addActionListener(ev -> {
			Session newSession = LoginDialog.showLoginRequest(this, launcher);

			if (newSession != null) {
				launcher.getAccounts().add(newSession.toSavedSession());
				setResult(newSession);
			}
		});
	}

	@Override
	public void dispose() {
		accountList.setModel(new DefaultListModel<>());
		super.dispose();
	}

	public static Session showAccountRequest(Window owner, Launcher launcher) {
		AccountSelectDialog dialog = new AccountSelectDialog(owner, launcher);
		dialog.setVisible(true);

		if (dialog.selected != null) {
			launcher.getAccounts().update(dialog.selected.toSavedSession());
			Persistence.commitAndForget(launcher.getAccounts());
		}

		return dialog.selected;
	}

	private void setResult(Session result) {
		this.selected = result;
		dispose();
	}

	private void attemptLogin(SavedSession session) {
		LoginService loginService = launcher.getLoginService(session.getType());
		RestoreSessionCallable callable = new RestoreSessionCallable(loginService, session);

		ObservableFuture<Session> future = new ObservableFuture<>(launcher.getExecutor().submit(callable), callable);
		Futures.addCallback(future, new FutureCallback<Session>() {
			@Override
			public void onSuccess(Session result) {
				setResult(result);
			}

			@Override
			public void onFailure(Throwable t) {
				t.printStackTrace();
			}
		}, SwingExecutor.INSTANCE);

		ProgressDialog.showProgress(this, future, SharedLocale.tr("login.loggingInTitle"),
				SharedLocale.tr("login.loggingInStatus"));
		SwingHelper.addErrorDialogCallback(this, future);
	}

	@RequiredArgsConstructor
	private static class RestoreSessionCallable implements Callable<Session>, ProgressObservable {
		private final LoginService service;
		private final SavedSession session;

		@Override
		public Session call() throws Exception {
			return service.restore(session);
		}

		@Override
		public String getStatus() {
			return SharedLocale.tr("accounts.refreshingStatus");
		}

		@Override
		public double getProgress() {
			return -1;
		}
	}

	private static class AccountRenderer extends JLabel implements ListCellRenderer<SavedSession> {
		public AccountRenderer() {
			setHorizontalAlignment(CENTER);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends SavedSession> list, SavedSession value, int index, boolean isSelected, boolean cellHasFocus) {
			setText(value.getUsername());
			if (value.getAvatarImage() != null) {
				setIcon(new ImageIcon(value.getAvatarBytes()));
			} else {
				setIcon(SwingHelper.createIcon(Launcher.class, "default_skin.png", 32, 32));
			}

			if (isSelected) {
				setOpaque(true);
				setBackground(new Color(0x397BBF));
			} else {
				setOpaque(false);
			}

			return this;
		}
	}
}
