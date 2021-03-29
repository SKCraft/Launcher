package com.skcraft.plugin.curse.creator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.skcraft.concurrency.DefaultProgress;
import com.skcraft.launcher.creator.model.creator.Pack;
import com.skcraft.launcher.creator.plugin.MenuContext;
import com.skcraft.launcher.creator.plugin.PluginMenu;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.swing.EmptyIcon;
import com.skcraft.launcher.swing.LinedBoxPanel;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SwingExecutor;
import com.skcraft.plugin.curse.CurseApi;
import com.skcraft.plugin.curse.model.AddedMod;
import com.skcraft.plugin.curse.model.CurseProject;
import com.skcraft.plugin.curse.model.ProjectHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static com.skcraft.launcher.util.HttpRequest.url;

@Log
public class CurseModsDialog extends JDialog {
	private final CursePack cursePack;

	private final JPanel panel = new JPanel(new BorderLayout(0, 5));
	private final JTextField searchBox = new JTextField();
	private final JButton searchButton = new JButton("Search");
	private final JList<CurseProject> searchPane = new JList<>();
	private final JList<AddedMod> selectedPane = new JList<>();
	private final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(searchPane), new JScrollPane(selectedPane));
	private final JButton addMod = new JButton("Add Mods >>");
	private final JButton removeMod = new JButton("<< Remove Mods");
	private final JButton done = new JButton("Done");

	private final CurseProjectListRenderer projectRenderer = new CurseProjectListRenderer();

	private final ListeningExecutorService executor;
	private final Pack pack;

	public CurseModsDialog(Window owner, ListeningExecutorService executor, Pack pack, CursePack cursePack) {
		super(owner);
		this.executor = executor;
		this.pack = pack;
		this.cursePack = cursePack;

		initComponents();
		setMinimumSize(new Dimension(500, 450));
		pack();
		setLocationRelativeTo(owner);
	}

	private void initComponents() {
		searchPane.setModel(cursePack.getSearchResults());
		searchPane.setCellRenderer(projectRenderer);
		searchPane.setLayoutOrientation(JList.VERTICAL);
		searchPane.setVisibleRowCount(0);
		searchPane.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		selectedPane.setModel(cursePack.getModList());
		selectedPane.setCellRenderer(projectRenderer);
		selectedPane.setLayoutOrientation(JList.VERTICAL);
		selectedPane.setVisibleRowCount(0);
		selectedPane.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		splitPane.setDividerLocation(250);
		splitPane.setDividerSize(10);

		LinedBoxPanel searchBarPanel = new LinedBoxPanel(true);
		searchBarPanel.addElement(searchBox);
		searchBarPanel.addElement(searchButton);
		searchBarPanel.setAlignmentX(CENTER_ALIGNMENT);

		LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);
		buttonsPanel.addElement(addMod);
		buttonsPanel.addGlue();
		buttonsPanel.addElement(done);
		buttonsPanel.addGlue();
		buttonsPanel.addElement(removeMod);
		buttonsPanel.setAlignmentX(CENTER_ALIGNMENT);

		panel.add(searchBarPanel, BorderLayout.NORTH);
		panel.add(Box.createVerticalStrut(5));
		panel.add(splitPane, BorderLayout.CENTER);
		panel.add(buttonsPanel, BorderLayout.SOUTH);

		add(panel);

		searchBox.addActionListener(e -> search());
		searchButton.addActionListener(e -> search());

		addMod.addActionListener(e -> {
			ListenableFuture<Object> future = executor.submit(cursePack.addMany(searchPane.getSelectedValuesList()));

			// TODO: Update a progress bar built in to the dialog.
			SwingHelper.addErrorDialogCallback(getOwner(), future);
		});

		removeMod.addActionListener(e -> {
			ListenableFuture<Object> future = executor.submit(
					cursePack.removeMany(selectedPane.getSelectedValuesList()));

			SwingHelper.addErrorDialogCallback(getOwner(), future);
		});

		done.addActionListener(e -> dispose());
	}

	@Override
	public void dispose() {
		// Let's make sure the image cache is emptied
		projectRenderer.clearCache();
		super.dispose();
	}

	private void search() {
		String query = searchBox.getText();
		ListenableFuture<List<CurseProject>> future = executor.submit(() ->
				CurseApi.searchForProjects(query, pack.getCachedConfig().getGameVersion()));

		Futures.addCallback(future, new FutureCallback<List<CurseProject>>() {
			@Override
			public void onSuccess(List<CurseProject> result) {
				projectRenderer.clearCacheIfBig();
				cursePack.getSearchResults().updateResults(result);
			}

			@Override
			public void onFailure(Throwable t) {
				SwingHelper.showErrorDialog(getOwner(), t.getMessage(), "Search failure", t);
			}
		}, SwingExecutor.INSTANCE);
	}

	@RequiredArgsConstructor
	private static class ImageWorker extends SwingWorker<BufferedImage, BufferedImage> {
		private final String imgUrl;
		private final Consumer<BufferedImage> consumer;

		@Override
		protected BufferedImage doInBackground() throws Exception {
			return ImageIO.read(url(imgUrl));
		}

		@Override
		protected void done() {
			try {
				consumer.accept(this.get());
			} catch (InterruptedException | ExecutionException e) {
				SwingHelper.showErrorDialog(null, e.getMessage(), "Worker error", e);
			}
		}

		public static void downloadImage(String imageUrl, Consumer<BufferedImage> consumer) {
			ImageWorker worker = new ImageWorker(imageUrl, consumer);
			worker.execute();
		}
	}

	private static class CurseProjectListRenderer extends JLabel implements ListCellRenderer<ProjectHolder> {
		private HashMap<String, BufferedImage> cache;

		public CurseProjectListRenderer() {
			this.cache = new HashMap<>();
		}

		private BufferedImage getCachedIcon(String imgUrl, Runnable cb) {
			if (!cache.containsKey(imgUrl)) {
				ImageWorker.downloadImage(imgUrl, img -> {
					cache.put(imgUrl, img);
					cb.run();
				});

				return null;
			}

			return cache.get(imgUrl);
		}

		public void clearCache() {
			this.cache.clear();
		}

		public void clearCacheIfBig() {
			if (this.cache.size() > 200) {
				this.cache.clear();
			}
		}

		private static Dimension getScaledDimensions(int imageWidth, int imageHeight) {
			double widthRatio = 32.0D / imageWidth;
			double heightRatio = 32.0D / imageHeight;
			double ratio = Math.min(widthRatio, heightRatio);

			int newWidth = (int) Math.floor(imageWidth * ratio);
			int newHeight = (int) Math.floor(imageHeight * ratio);

			return new Dimension(newWidth, newHeight);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends ProjectHolder> list, ProjectHolder value, int index, boolean isSelected, boolean cellHasFocus) {
			CurseProject project = value.getProject();

			setText(project.getName());
			setToolTipText(project.getSummary());

			BufferedImage ref = getCachedIcon(project.getDefaultIcon().getThumbnailUrl(), list::repaint);
			if (ref != null) {
				Dimension scaled = getScaledDimensions(ref.getWidth(), ref.getHeight());
				setIcon(new ImageIcon(ref.getScaledInstance(scaled.width, scaled.height, Image.SCALE_SMOOTH)));
			} else {
				setIcon(new EmptyIcon(32 ,32));
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

	public static class Menu implements PluginMenu {
		private final ObjectMapper mapper = new ObjectMapper();

		@Override
		public String getTitle() {
			return "Add Mods";
		}

		@Override
		public boolean requiresPack() {
			return true;
		}

		@Override
		public void onOpen(MenuContext ctx, ActionEvent e, Pack pack) {
			PackModScanner scanner = new PackModScanner(mapper);

			ListenableFuture<?> future = ctx.getExecutor().submit(() -> {
				File target = new File(pack.getDirectory(), "cursemods");

				if (target.isDirectory()) {
					scanner.walk(target);
				}
				return null;
			});

			ProgressDialog.showProgress(ctx.getOwner(), future,
					new DefaultProgress(-1, "Loading mods..."),"Curse", "Contacting Curse API");
			SwingHelper.addErrorDialogCallback(ctx.getOwner(), future);

			future.addListener(() -> {
				CursePack cursePack = new CursePack(mapper, pack);
				cursePack.getModList().addAll(scanner.getResult());

				CurseModsDialog dialog = new CurseModsDialog(ctx.getOwner(), ctx.getExecutor(), pack, cursePack);
				dialog.setTitle(getTitle());
				dialog.setVisible(true);
			}, SwingExecutor.INSTANCE);
		}
	}
}
