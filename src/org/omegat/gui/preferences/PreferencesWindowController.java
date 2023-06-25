/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.preferences;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.omegat.core.Core;
import org.omegat.core.team2.gui.RepositoriesCredentialsController;
import org.omegat.externalfinder.gui.ExternalFinderPreferencesController;
import org.omegat.gui.filters2.FiltersCustomizerController;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.gui.preferences.IPreferencesController.FurtherActionListener;
import org.omegat.gui.preferences.view.AppearanceController;
import org.omegat.gui.preferences.view.AutoCompleterController;
import org.omegat.gui.preferences.view.AutotextAutoCompleterOptionsController;
import org.omegat.gui.preferences.view.CharTableAutoCompleterOptionsController;
import org.omegat.gui.preferences.view.CustomColorSelectionController;
import org.omegat.gui.preferences.view.DictionaryPreferencesController;
import org.omegat.gui.preferences.view.EditingBehaviorController;
import org.omegat.gui.preferences.view.FontSelectionController;
import org.omegat.gui.preferences.view.GeneralOptionsController;
import org.omegat.gui.preferences.view.GlossaryAutoCompleterOptionsController;
import org.omegat.gui.preferences.view.GlossaryPreferencesController;
import org.omegat.gui.preferences.view.HistoryAutoCompleterOptionsController;
import org.omegat.gui.preferences.view.LanguageToolConfigurationController;
import org.omegat.gui.preferences.view.MachineTranslationPreferencesController;
import org.omegat.gui.preferences.view.PluginsPreferencesController;
import org.omegat.gui.preferences.view.SaveOptionsController;
import org.omegat.gui.preferences.view.SecureStoreController;
import org.omegat.gui.preferences.view.SpellcheckerConfigurationController;
import org.omegat.gui.preferences.view.TMMatchesPreferencesController;
import org.omegat.gui.preferences.view.TagProcessingOptionsController;
import org.omegat.gui.preferences.view.TeamOptionsController;
import org.omegat.gui.preferences.view.UserPassController;
import org.omegat.gui.preferences.view.VersionCheckPreferencesController;
import org.omegat.gui.preferences.view.ViewOptionsController;
import org.omegat.gui.segmentation.SegmentationCustomizerController;
import org.omegat.util.Java8Compat;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.StaticUIUtils;

/**
 * A modal window aggregating all preference "views"
 * ({@link IPreferencesController}s). Plugins can provide their own views via
 * {@link PreferencesControllers#addSupplier(java.util.function.Supplier)}.
 *
 * @author Aaron Madlon-Kay
 */
public class PreferencesWindowController implements FurtherActionListener {

    private static final Logger LOGGER = Logger.getLogger(PreferencesWindowController.class.getName());

    private static final String ACTION_KEY_NEW_SEARCH = "clearSearch";
    private static final String ACTION_KEY_CLEAR_OR_CLOSE = "clearSearchOrClose";
    private static final String ACTION_KEY_DO_SEARCH = "doSearch";

    private JDialog dialog;
    private PreferencePanel outerPanel;
    private PreferenceViewSelectorPanel innerPanel;
    private HighlightablePanel overlay;
    private IPreferencesController currentView;
    private boolean didLoadGuis;
    private final Map<String, Runnable> persistenceRunnables = new HashMap<>();

    public void show(Window parent) {
        show(parent, null);
    }

    @SuppressWarnings("serial")
    public void show(Window parent, Class<? extends IPreferencesController> initialSelection) {
        dialog = new JDialog();
        dialog.setTitle(OStrings.getString("PREFERENCES_TITLE_NO_SELECTION"));
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        StaticUIUtils.setWindowIcon(dialog);

        outerPanel = new PreferencePanel();
        innerPanel = new PreferenceViewSelectorPanel();
        outerPanel.prefsViewPanel.add(innerPanel, BorderLayout.CENTER);
        dialog.getContentPane().add(outerPanel);

        overlay = new HighlightablePanel(dialog.getRootPane(), innerPanel.selectedPrefsScrollPane);

        // Prevent ugly white viewport background with GTK LAF
        innerPanel.selectedPrefsScrollPane.getViewport().setOpaque(false);
        innerPanel.selectedPrefsScrollPane.setBackground(innerPanel.getBackground());

        innerPanel.searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(PreferencesWindowController.this::searchAndFilterTree);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(PreferencesWindowController.this::searchAndFilterTree);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(PreferencesWindowController.this::searchAndFilterTree);
            }
        });
        innerPanel.searchTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP) {
                    innerPanel.availablePrefsTree.getActionForKeyStroke(KeyStroke.getKeyStrokeForEvent(e))
                            .actionPerformed(new ActionEvent(innerPanel.availablePrefsTree, 0, null));
                    innerPanel.availablePrefsTree.requestFocusInWindow();
                    e.consume();
                }
            }
        });
        innerPanel.searchTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                innerPanel.searchTextField.selectAll();
                searchCurrentView();
                preloadGuis();
            }
        });
        innerPanel.clearButton.addActionListener(e -> {
            innerPanel.searchTextField.clear();
        });
        innerPanel.availablePrefsTree.getSelectionModel()
                .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        DefaultMutableTreeNode root = createNodeTree();
        walkTree(root, node -> {
            IPreferencesController view = (IPreferencesController) node.getUserObject();
            if (view != null) {
                view.addFurtherActionListener(this);
            }
        });
        innerPanel.availablePrefsTree.setModel(new DefaultTreeModel(root));
        innerPanel.availablePrefsTree.addTreeSelectionListener(e -> {
            handleViewSelection(e);
            updateTitle();
        });
        innerPanel.availablePrefsTree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                SwingUtilities.invokeLater(PreferencesWindowController.this::adjustTreeSize);
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
            }
        });
        innerPanel.selectedPrefsScrollPane.getViewport().setBackground(innerPanel.getBackground());
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) innerPanel.availablePrefsTree
                .getCellRenderer();
        renderer.setIcon(null);
        renderer.setLeafIcon(null);
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        renderer.setDisabledIcon(null);

        outerPanel.okButton.addActionListener(e -> {
            if (currentView == null || currentView.validate()) {
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        doSave();
                        return null;
                    }

                    @Override
                    protected void done() {
                        if (getIsReloadRequired()) {
                            SwingUtilities.invokeLater(ProjectUICommands::promptReload);
                        }
                    }
                }.execute();
                StaticUIUtils.closeWindowByEvent(dialog);
            }
        });
        outerPanel.cancelButton.addActionListener(e -> StaticUIUtils.closeWindowByEvent(dialog));

        // Hide undo, reset buttons on outer panel
        outerPanel.undoButton.setVisible(false);
        outerPanel.resetButton.setVisible(false);
        // Use ones on inner panel to indicate that actions are view-specific
        innerPanel.undoButton.addActionListener(e -> currentView.undoChanges());
        innerPanel.resetButton.addActionListener(e -> currentView.restoreDefaults());

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                walkTree(root, node -> {
                    // Start with tree fully expanded
                    if (node.getChildCount() > 0) {
                        innerPanel.availablePrefsTree.expandPath(new TreePath(node.getPath()));
                    }
                });
                SwingUtilities.invokeLater(() -> {
                    if (initialSelection != null) {
                        selectView(initialSelection);
                    }
                });
            }
        });
        innerPanel.availablePrefsScrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(PreferencesWindowController.this::adjustTreeSize);
            }
        });

        ActionMap actionMap = innerPanel.getActionMap();
        InputMap inputMap = innerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        actionMap.put(ACTION_KEY_NEW_SEARCH, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                innerPanel.searchTextField.requestFocusInWindow();
                innerPanel.searchTextField.selectAll();
            }
        });
        KeyStroke searchKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F,
                Java8Compat.getMenuShortcutKeyMaskEx());
        inputMap.put(searchKeyStroke, ACTION_KEY_NEW_SEARCH);
        actionMap.put(ACTION_KEY_CLEAR_OR_CLOSE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!innerPanel.searchTextField.isEmpty()) {
                    // Move focus away from search field
                    innerPanel.availablePrefsTree.requestFocusInWindow();
                    innerPanel.clearButton.doClick();
                } else {
                    StaticUIUtils.closeWindowByEvent(dialog);
                }
            }
        });
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ACTION_KEY_CLEAR_OR_CLOSE);

        // Don't let Enter close the dialog
        innerPanel.searchTextField.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ACTION_KEY_DO_SEARCH);
        innerPanel.searchTextField.getActionMap().put(ACTION_KEY_DO_SEARCH, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchCurrentView();
            }
        });

        String searchKeyText = StaticUIUtils.getKeyStrokeText(searchKeyStroke);
        innerPanel.searchTextField.setHintText(OStrings.getString("PREFERENCES_SEARCH_HINT", searchKeyText));

        // Set initial state
        searchAndFilterTree();
        adjustTreeSize();

        dialog.getRootPane().setDefaultButton(outerPanel.okButton);

        dialog.setPreferredSize(new Dimension(800, 500));
        dialog.pack();
        // Prevent search field from getting initial focus
        innerPanel.availablePrefsTree.requestFocusInWindow();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static DefaultMutableTreeNode createNodeTree() {
        HideableNode root = new HideableNode();
        root.add(new HideableNode(new GeneralOptionsController()));
        root.add(new HideableNode(new MachineTranslationPreferencesController()));
        root.add(new HideableNode(new GlossaryPreferencesController()));
        root.add(new HideableNode(new DictionaryPreferencesController()));
        HideableNode appearanceNode = new HideableNode(new AppearanceController());
        appearanceNode.add(new HideableNode(new FontSelectionController()));
        appearanceNode.add(new HideableNode(new CustomColorSelectionController()));
        root.add(appearanceNode);
        root.add(new HideableNode(new FiltersCustomizerController()));
        root.add(new HideableNode(new SegmentationCustomizerController()));
        HideableNode acNode = new HideableNode(new AutoCompleterController());
        acNode.add(new HideableNode(new GlossaryAutoCompleterOptionsController()));
        acNode.add(new HideableNode(new AutotextAutoCompleterOptionsController()));
        acNode.add(new HideableNode(new CharTableAutoCompleterOptionsController()));
        acNode.add(new HideableNode(new HistoryAutoCompleterOptionsController()));
        root.add(acNode);
        root.add(new HideableNode(new SpellcheckerConfigurationController()));
        root.add(new HideableNode(new LanguageToolConfigurationController()));
        root.add(new HideableNode(new ExternalFinderPreferencesController()));
        root.add(new HideableNode(new EditingBehaviorController()));
        root.add(new HideableNode(new TagProcessingOptionsController()));
        HideableNode teamNode = new HideableNode(new TeamOptionsController());
        teamNode.add(new HideableNode(new RepositoriesCredentialsController()));
        root.add(teamNode);
        root.add(new HideableNode(new TMMatchesPreferencesController()));
        root.add(new HideableNode(new ViewOptionsController()));
        root.add(new HideableNode(new SaveOptionsController()));
        root.add(new HideableNode(new UserPassController()));
        root.add(new HideableNode(new SecureStoreController()));
        HideableNode pluginsNode = new HideableNode(new PluginsPreferencesController());
        root.add(pluginsNode);
        root.add(new HideableNode(new VersionCheckPreferencesController()));
        PreferencesControllers.getSuppliers().forEach(s -> placePluginView(root, s.get()));
        return root;
    }

    private static void placePluginView(HideableNode root, IPreferencesController view) {
        Class<? extends IPreferencesController> parentClass = view.getParentViewClass();
        Class<? extends IPreferencesController> effectiveParentClass = parentClass == null
                ? PluginsPreferencesController.class : parentClass;
        walkTree(root, node -> {
            IPreferencesController parent = (IPreferencesController) node.getUserObject();
            if (parent != null && parent.getClass().equals(effectiveParentClass)) {
                node.add(new HideableNode(view));
            }
        });
    }

    @Override
    public void setRestartRequired(boolean restartRequired) {
        updateMessage();
    }

    @Override
    public void setReloadRequired(boolean reloadRequired) {
        updateMessage();
    }

    private HideableNode getRoot() {
        return (HideableNode) innerPanel.availablePrefsTree.getModel().getRoot();
    }

    private boolean getIsRestartRequired() {
        return anyInTree(getRoot(), node -> {
            IPreferencesController view = (IPreferencesController) node.getUserObject();
            return view != null && view.isRestartRequired();
        });
    }

    private boolean getIsReloadRequired() {
        if (!Core.getProject().isProjectLoaded()) {
            return false;
        }
        return anyInTree(getRoot(), node -> {
            IPreferencesController view = (IPreferencesController) node.getUserObject();
            return view != null && view.isReloadRequired();
        });
    }

    private void updateMessage() {
        String message = null;
        if (getIsRestartRequired()) {
            message = OStrings.getString("PREFERENCES_WARNING_NEEDS_RESTART");
        } else if (getIsReloadRequired()) {
            message = OStrings.getString("PREFERENCES_WARNING_NEEDS_RELOAD");
        }
        outerPanel.messageTextArea.setText(message);
    }

    private void handleViewSelection(TreeSelectionEvent e) {
        TreePath selectedPath = e.getNewLeadSelectionPath();
        if (selectedPath == null) {
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
        if (node == null) {
            return;
        }
        IPreferencesController oldView = currentView;
        Object obj = node.getUserObject();
        if (!(obj instanceof IPreferencesController)) {
            return;
        }
        IPreferencesController newView = (IPreferencesController) obj;
        if (Objects.equals(oldView, newView)) {
            return;
        }
        if (oldView != null && !oldView.validate()) {
            innerPanel.availablePrefsTree.getSelectionModel().setSelectionPath(e.getOldLeadSelectionPath());
            return;
        }
        if (!persistenceRunnables.containsKey(newView.getClass().getName())) {
            persistenceRunnables.put(newView.getClass().getName(), newView::persist);
        }
        overlay.setHighlightComponent(null);
        innerPanel.innerViewHolder.removeAll();
        innerPanel.innerViewHolder.add(newView.getGui(), BorderLayout.CENTER);
        innerPanel.selectedPrefsScrollPane.setViewportView(innerPanel.viewHolder);
        currentView = newView;
        innerPanel.resetButton.setEnabled(currentView.canRestoreDefaults());
        SwingUtilities.invokeLater(() -> {
            adjustSize();
            searchCurrentView();
        });
    }

    private void updateTitle() {
        if (currentView == null) {
            dialog.setTitle(OStrings.getString("PREFERENCES_TITLE_NO_SELECTION"));
        } else {
            dialog.setTitle(StringUtil.format(OStrings.getString("PREFERENCES_TITLE_WITH_SELECTION"), currentView));
        }
    }

    private void adjustSize() {
        Dimension viewPreferredSize = innerPanel.viewHolder.getPreferredSize();
        Dimension viewActualSize = innerPanel.selectedPrefsScrollPane.getViewport().getSize();

        Dimension dialogSize = dialog.getSize();
        boolean shouldAdjust = false;

        if (viewPreferredSize.width > viewActualSize.width) {
            dialogSize.width += viewPreferredSize.width - viewActualSize.width;
            shouldAdjust = true;
        }

        if (viewPreferredSize.height > viewActualSize.height) {
            dialogSize.height += viewPreferredSize.height - viewActualSize.height;
            shouldAdjust = true;
        }

        if (shouldAdjust) {
            dialog.setSize(dialogSize);
            StaticUIUtils.fitInScreen(dialog);
        }
    }

    private void adjustTreeSize() {
        JScrollBar hScrollBar = innerPanel.availablePrefsScrollPane.getHorizontalScrollBar();
        if (hScrollBar != null) {
            int currentWidth = innerPanel.availablePrefsScrollPane.getViewport().getWidth();
            int preferredWidth = hScrollBar.getMaximum();
            if (preferredWidth > currentWidth) {
                int newWidth = innerPanel.leftPanel.getWidth() + (preferredWidth - currentWidth);
                innerPanel.leftPanel.setMinimumSize(new Dimension(newWidth, 0));
                innerPanel.mainSplitPane.setDividerLocation(-1);
            }
        }
    }

    private void searchAndFilterTree() {
        incrementalSearchImpl(true, getRoot());
    }

    private void searchCurrentView() {
        TreePath selection = innerPanel.availablePrefsTree.getSelectionPath();
        if (selection != null) {
            HideableNode root = (HideableNode) selection.getLastPathComponent();
            incrementalSearchImpl(false, root);
        }
    }

    private void incrementalSearchImpl(boolean filterTree, HideableNode root) {
        boolean isEmptyQuery = innerPanel.searchTextField.isEmpty();
        innerPanel.clearButton.setEnabled(!isEmptyQuery);
        if (isEmptyQuery) {
            if (filterTree) {
                if (setTreeVisible(root, true)) {
                    ((DefaultTreeModel) innerPanel.availablePrefsTree.getModel()).reload();
                }
                selectView(currentView);
            }
            overlay.setHighlightComponent(null);
            return;
        }
        String query = innerPanel.searchTextField.getText().trim();
        if (currentView != null && !currentView.validate()) {
            innerPanel.searchTextField.clear();
            return;
        }
        Pattern pattern = Pattern.compile(".*" + Pattern.quote(query) + ".*",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        List<GuiSearchResult> results = searchTree(root, pattern.asPredicate());
        if (filterTree) {
            setTreeVisible(root, false);
        }
        if (results.isEmpty()) {
            innerPanel.searchTextField.setForeground(UIManager.getColor("OmegaT.searchFieldErrorText"));
            if (filterTree) {
                ((DefaultTreeModel) innerPanel.availablePrefsTree.getModel()).reload();
            }
            overlay.setHighlightComponent(null);
            innerPanel.selectedPrefsScrollPane.setViewportView(innerPanel.selectedPrefsPlaceholderPanel);
            currentView = null;
            updateTitle();
        } else {
            GuiSearchResult topResult = results.get(0);
            if (filterTree) {
                for (GuiSearchResult result : results) {
                    setNodePathVisible((HideableNode) result.node, true);
                }
                ((DefaultTreeModel) innerPanel.availablePrefsTree.getModel()).reload();
                selectNode(topResult.node);
            }
            if (topResult.comp != null) {
                ((JComponent) topResult.comp).scrollRectToVisible(topResult.comp.getBounds());
                overlay.setHighlightComponent(topResult.comp);
            }
            innerPanel.searchTextField.setForeground(UIManager.getColor("TextField.foreground"));
        }
    }

    private void selectView(IPreferencesController view) {
        firstNodeInTree(getRoot(), node -> node.getUserObject() == view)
                .ifPresent(this::selectNode);
    }

    private void selectView(Class<? extends IPreferencesController> viewClass) {
        firstNodeInTree(getRoot(), node -> {
            Object obj = node.getUserObject();
            return obj != null && obj.getClass().equals(viewClass);
        }).ifPresent(this::selectNode);
    }

    void selectNode(DefaultMutableTreeNode node) {
        innerPanel.availablePrefsTree.setSelectionPath(new TreePath(node.getPath()));
    }

    /**
     * Set the visibility of the entire subtree starting at root.
     *
     * @param root
     *            Root node
     * @param isVisible
     *            Visible or not
     * @return true if any of the nodes' visibility values were changed
     */
    private static boolean setTreeVisible(HideableNode root, boolean isVisible) {
        List<Boolean> changes = mapTree(root, node -> {
            HideableNode hideable = (HideableNode) node;
            boolean wasVisible = hideable.isVisible;
            hideable.setVisible(isVisible);
            return wasVisible != isVisible;
        });
        root.setVisible(true);
        return changes.contains(true);
    }

    private static void setNodePathVisible(HideableNode node, boolean isVisible) {
        node.setVisible(isVisible);
        for (TreeNode parent : node.getPath()) {
            ((HideableNode) parent).setVisible(isVisible);
        }
    }

    private static List<GuiSearchResult> searchTree(DefaultMutableTreeNode root, Predicate<String> filter) {
        List<GuiSearchResult> result = new ArrayList<>();
        walkTree(root, node -> {
            IPreferencesController view = (IPreferencesController) node.getUserObject();
            if (view != null) {
                visitUiStrings(view.getGui(), (str, comp) -> {
                    if (filter.test(str)) {
                        result.add(new GuiSearchResult(node, str, comp));
                    }
                });
                if (filter.test(view.toString())) {
                    result.add(new GuiSearchResult(node, view.toString(), null));
                }
            }
        });
        return result;
    }

    static class GuiSearchResult {
        public final DefaultMutableTreeNode node;
        public final String string;
        public final Component comp;

        GuiSearchResult(DefaultMutableTreeNode node, String string, Component comp) {
            this.node = node;
            this.string = string;
            this.comp = comp;
        }
    }

    private static void visitUiStrings(Component comp, BiConsumer<String, Component> consumer) {
        StaticUIUtils.visitHierarchy(comp, c -> c.isVisible(), c -> {
            try {
                Method getText = c.getClass().getMethod("getText");
                String str = (String) getText.invoke(c);
                if (!StringUtil.isEmpty(str)) {
                    consumer.accept(str, c);
                }
            } catch (NoSuchMethodException e) {
                // Skip
            } catch (SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    private static void walkTree(DefaultMutableTreeNode node, Consumer<DefaultMutableTreeNode> consumer) {
        consumer.accept(node);
        Enumeration<?> e = node.children();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            walkTree((DefaultMutableTreeNode) o, consumer);
        }
    }

    private static <T> List<T> mapTree(DefaultMutableTreeNode node,
            Function<DefaultMutableTreeNode, T> function) {
        List<T> results = new ArrayList<>();
        walkTree(node, n -> {
            results.add(function.apply(n));
        });
        return results;
    }

    private static boolean anyInTree(DefaultMutableTreeNode node,
            Predicate<DefaultMutableTreeNode> predicate) {
        return firstNodeInTree(node, predicate).isPresent();
    }

    private static Optional<DefaultMutableTreeNode> firstNodeInTree(DefaultMutableTreeNode node,
            Predicate<DefaultMutableTreeNode> predicate) {
        if (predicate.test(node)) {
            return Optional.of(node);
        } else {
            Enumeration<?> e = node.children();
            while (e.hasMoreElements()) {
                Object o = e.nextElement();
                Optional<DefaultMutableTreeNode> child = firstNodeInTree((DefaultMutableTreeNode) o, predicate);
                if (child.isPresent()) {
                    return child;
                }
            }
        }
        return Optional.empty();
    }

    private void preloadGuis() {
        if (didLoadGuis) {
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                preloadGuisImpl();
                didLoadGuis = true;
                return null;
            }
        }.execute();
    }

    private void preloadGuisImpl() {
        walkTree(getRoot(), node -> {
            IPreferencesController view = (IPreferencesController) node.getUserObject();
            if (view != null) {
                try {
                    view.getGui();
                } catch (Throwable t) {
                    LOGGER.log(Level.SEVERE, t.getMessage(), t);
                }
            }
        });
    }

    private void doSave() {
        persistenceRunnables.entrySet().forEach(e -> {
            long start = System.currentTimeMillis();
            e.getValue().run();
            long end = System.currentTimeMillis();
            if (end - start > 100) {
                LOGGER.finer(() -> String.format("Persisting %s took %d ms", e.getKey(), end - start));
            }
        });
        Preferences.save();
    }

    @SuppressWarnings("serial")
    static class HideableNode extends DefaultMutableTreeNode {
        private boolean isVisible = true;

        HideableNode() {
            super();
        }

        HideableNode(Object userObject) {
            super(userObject);
        }

        public boolean isVisible() {
            return isVisible;
        }

        public void setVisible(boolean isVisible) {
            this.isVisible = isVisible;
        }

        @Override
        public TreeNode getChildAt(int index) {
            if (children == null) {
                throw new IndexOutOfBoundsException("node has no children");
            }

            int realIndex = -1;
            int visibleIndex = -1;
            Enumeration<?> e = children.elements();
            while (e.hasMoreElements()) {
                HideableNode node = (HideableNode) e.nextElement();
                if (node.isVisible()) {
                    visibleIndex++;
                }
                realIndex++;
                if (visibleIndex == index) {
                    // Java 11 compatibility: suppress redundant cast warning
                    Object result = children.elementAt(realIndex);
                    return (TreeNode) result;
                }
            }

            throw new IndexOutOfBoundsException("index unmatched");
        }

        @Override
        public int getChildCount() {
            if (children == null) {
                return 0;
            }

            int count = 0;
            Enumeration<?> e = children.elements();
            while (e.hasMoreElements()) {
                HideableNode node = (HideableNode) e.nextElement();
                if (node.isVisible()) {
                    count++;
                }
            }

            return count;
        }
    }

    @SuppressWarnings("serial")
    static class HighlightablePanel extends JPanel {

        private static final Color SHADOW_COLOR = UIManager.getColor("OmegaT.searchDimmedBackground");
        private static final Color STROKE_COLOR = UIManager.getColor("OmegaT.searchResultBorder");
        private static final int STROKE = 2;
        private static final BasicStroke STROKE_OBJ = new BasicStroke(STROKE);
        private final transient ComponentListener compListener;
        private final transient MouseAdapter mouseAdapter;
        private final JRootPane rootPane;
        private final Component overlayComponent;
        private final Rectangle clipRect = new Rectangle();
        private final Rectangle highlightRect = new Rectangle();
        private Component comp;

        HighlightablePanel(JRootPane rootPane, Component overlayComponent) {
            this.rootPane = rootPane;
            this.overlayComponent = overlayComponent;
            this.mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    forward(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    forward(e);
                    setHighlightComponent(null);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    forward(e);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    forward(e);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    forward(e);
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    forward(e);
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    forward(e);
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    forward(e);
                }

                private void forward(MouseEvent e) {
                    Container contentPane = rootPane.getContentPane();
                    Point p = SwingUtilities.convertPoint(HighlightablePanel.this, e.getPoint(), contentPane);
                    Component target = contentPane.findComponentAt(p);
                    if (target != null) {
                        e = SwingUtilities.convertMouseEvent(HighlightablePanel.this, e, target);
                        target.dispatchEvent(e);
                    }
                }
            };
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
            addMouseWheelListener(mouseAdapter);
            this.compListener = new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    update();
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    update();
                }

                @Override
                public void componentShown(ComponentEvent e) {
                    update();
                }

                @Override
                public void componentHidden(ComponentEvent e) {
                    update();
                }
            };
            SwingUtilities.windowForComponent(rootPane).addWindowFocusListener(new WindowAdapter() {
                @Override
                public void windowLostFocus(WindowEvent e) {
                    setVisible(false);
                }

                @Override
                public void windowGainedFocus(WindowEvent e) {
                    setVisible(true);
                }
            });
            setOpaque(false);
        }

        public void setHighlightComponent(Component comp) {
            Component oldComp = this.comp;
            this.comp = comp;
            if (comp == null) {
                uninstall();
            } else if (Objects.equals(oldComp, comp)) {
                update();
            } else {
                install();
            }
        }

        private void update() {
            Rectangle bounds = SwingUtilities.convertRectangle(overlayComponent.getParent(),
                    overlayComponent.getBounds(), rootPane.getLayeredPane());
            setBounds(bounds);
            repaint();
        }

        private void install() {
            JLayeredPane layeredPane = rootPane.getLayeredPane();
            if (!layeredPane.isAncestorOf(this)) {
                layeredPane.add(this, JLayeredPane.MODAL_LAYER);
                overlayComponent.addComponentListener(compListener);
            }
            update();
        }

        private void uninstall() {
            JLayeredPane layeredPane = rootPane.getLayeredPane();
            layeredPane.remove(this);
            overlayComponent.removeComponentListener(compListener);
            overlayComponent.repaint();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (comp != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(STROKE_OBJ);
                paintHighlight(g2);
                g2.dispose();
            }
        }

        private void paintHighlight(Graphics2D g) {
            g.getClipBounds(clipRect);
            Area realClip = new Area(clipRect);
            Rectangle rect = getHighlightRect();
            Shape knockOut = roundifyRect(rect);
            realClip.subtract(new Area(knockOut));
            g.setClip(realClip);
            g.setColor(SHADOW_COLOR);
            g.fillRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);
            g.setClip(clipRect);
            g.setColor(STROKE_COLOR);
            g.draw(knockOut);
        }

        private Rectangle getHighlightRect() {
            comp.getBounds(highlightRect);
            Point p = SwingUtilities.convertPoint(comp.getParent(), highlightRect.x, highlightRect.y, this);
            highlightRect.setLocation(p);
            return highlightRect;
        }

        private Shape roundifyRect(Rectangle rect) {
            int r = Math.min(10, Math.round(rect.height * 0.5f));
            int halfR = r / 2;
            RoundRectangle2D roundRect = new RoundRectangle2D.Float(rect.x - halfR, rect.y - halfR,
                    rect.width + r - (STROKE - 0.5f), rect.height + r - STROKE, r, r);
            return roundRect;
        }
    }
}
