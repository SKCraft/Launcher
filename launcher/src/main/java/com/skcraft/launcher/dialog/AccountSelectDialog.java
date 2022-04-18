package com.skcraft.launcher.dialog;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.concurrency.SettableProgress;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.*;
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
	private final JButton loginButton = new JButton(SharedLocale.tr("accounts.play"));
	private final JButton cancelButton = new JButton(SharedLocale.tr("button.cancel"));
	private final JButton addMojangButton = new JButton(SharedLocale.tr("accounts.addMojang"));
	private final JButton addMicrosoftButton = new JButton(SharedLocale.tr("accounts.addMicrosoft"));
	private final JButton removeSelected = new JButton(SharedLocale.tr("accounts.removeSelected"));
	private final JButton offlineButton = new JButton(SharedLocale.tr("login.playOffline"));
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
		setMinimumSize(new Dimension(350, 250));
		setResizable(false);
		pack();
		setLocationRelativeTo(owner);
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		accountList.setLayoutOrientation(JList.VERTICAL);
		accountList.setVisibleRowCount(0);
		accountList.setCellRenderer(new AccountRenderer());

		JScrollPane accountPane = new JScrollPane(accountList);
		accountPane.setPreferredSize(new Dimension(280, 150));
		accountPane.setAlignmentX(CENTER_ALIGNMENT);

		loginButton.setFont(loginButton.getFont().deriveFont(Font.BOLD));
		loginButton.setMargin(new Insets(0, 10, 0, 10));

		//Start Buttons
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(26, 13, 13, 13));
		if (launcher.getConfig().isOfflineEnabled()) {
			buttonsPanel.addElement(offlineButton);
		}
		buttonsPanel.addGlue();
		buttonsPanel.addElement(cancelButton);
		buttonsPanel.addElement(loginButton);

		//Login Buttons
		JPanel loginButtonsRow = new JPanel(new BorderLayout(0, 5));
		addMojangButton.setAlignmentX(CENTER_ALIGNMENT);
		addMicrosoftButton.setAlignmentX(CENTER_ALIGNMENT);
		removeSelected.setAlignmentX(CENTER_ALIGNMENT);
		loginButtonsRow.add(addMojangButton, BorderLayout.NORTH);
		loginButtonsRow.add(addMicrosoftButton, BorderLayout.CENTER);
		loginButtonsRow.add(removeSelected, BorderLayout.SOUTH);
		loginButtonsRow.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		JPanel listAndLoginContainer = new JPanel();
		listAndLoginContainer.add(accountPane, BorderLayout.WEST);
		listAndLoginContainer.add(loginButtonsRow, BorderLayout.EAST);
		listAndLoginContainer.add(Box.createVerticalStrut(5));
		listAndLoginContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		add(listAndLoginContainer, BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.SOUTH);

		loginButton.addActionListener(ev -> attemptExistingLogin(accountList.getSelectedValue()));
		cancelButton.addActionListener(ev -> dispose());

		addMojangButton.addActionListener(ev -> {
			Session newSession = LoginDialog.showLoginRequest(this, launcher);

			if (newSession != null) {
				launcher.getAccounts().update(newSession.toSavedSession());
				setResult(newSession);
			}
		});

		addMicrosoftButton.addActionListener(ev -> attemptMicrosoftLogin());

		offlineButton.addActionListener(ev ->
				setResult(new OfflineSession(launcher.getProperties().getProperty("offlinePlayerName"))));

		removeSelected.addActionListener(ev -> {
			if (accountList.getSelectedValue() != null) {
				boolean confirmed = SwingHelper.confirmDialog(this, SharedLocale.tr("accounts.confirmForget"),
						SharedLocale.tr("accounts.confirmForgetTitle"));

				if (confirmed) {
					launcher.getAccounts().remove(accountList.getSelectedValue());
				}
			}
		});

		accountList.setSelectedIndex(0);
	}

	@Override
	public void dispose() {
		accountList.setModel(new DefaultListModel<>());
		super.dispose();
	}

	public static Session showAccountRequest(Window owner, Launcher launcher) {
		AccountSelectDialog dialog = new AccountSelectDialog(owner, launcher);
		dialog.setVisible(true);

		if (dialog.selected != null && dialog.selected.isOnline()) {
			launcher.getAccounts().update(dialog.selected.toSavedSession());
		}

		Persistence.commitAndForget(launcher.getAccounts());

		return dialog.selected;
	}

	private void setResult(Session result) {
		this.selected = result;
		dispose();
	}

	private void attemptMicrosoftLogin() {
		String status = SharedLocale.tr("login.microsoft.seeBrowser");
		SettableProgress progress = new SettableProgress(status, -1);

		ListenableFuture<?> future = launcher.getExecutor().submit(() -> {
			Session newSession = launcher.getMicrosoftLogin().login(() ->
					progress.set(SharedLocale.tr("login.loggingInStatus"), -1));

			if (newSession != null) {
				launcher.getAccounts().update(newSession.toSavedSession());
				setResult(newSession);
			}

			return null;
		});

		ProgressDialog.showProgress(this, future, progress,
				SharedLocale.tr("login.loggingInTitle"), status);
		SwingHelper.addErrorDialogCallback(this, future);
	}

	private void attemptExistingLogin(SavedSession session) {
		if (session == null) return;

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
				if (t instanceof AuthenticationException) {
					if (((AuthenticationException) t).isInvalidatedSession()) {
						// Just need to log in again
						LoginDialog.ReloginDetails details = new LoginDialog.ReloginDetails(session.getUsername(),
								SharedLocale.tr("login.relogin", t.getLocalizedMessage()));
						Session newSession = LoginDialog.showLoginRequest(AccountSelectDialog.this, launcher, details);

						setResult(newSession);
					}
				} else {
					SwingHelper.showErrorDialog(AccountSelectDialog.this, t.getLocalizedMessage(), SharedLocale.tr("errorTitle"), t);
				}
			}
		}, SwingExecutor.INSTANCE);

		ProgressDialog.showProgress(this, future, SharedLocale.tr("login.loggingInTitle"),
				SharedLocale.tr("login.loggingInStatus"));
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
			setHorizontalAlignment(LEFT);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends SavedSession> list, SavedSession value, int index, boolean isSelected, boolean cellHasFocus) {
			setText(value.getUsername());
			if (value.getAvatarImage() != null) {
				setIcon(new ImageIcon(value.getAvatarImage()));
			} else {
				setIcon(SwingHelper.createIcon(Launcher.class, "default_skin.png", 32, 32));
			}

			if (isSelected) {
				setOpaque(true);
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setOpaque(false);
				setForeground(list.getForeground());
			}

			return this;
		}
	}
}
