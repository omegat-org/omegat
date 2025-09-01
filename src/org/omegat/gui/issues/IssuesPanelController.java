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

package org.omegat.gui.issues;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.DataUtils;
import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.Preferences;
import org.omegat.util.StreamUtil;
import org.omegat.util.gui.DataTableStyling;
import org.omegat.util.gui.OSXIntegration;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.TableColumnSizer;

/**
 * A controller to orchestrate the {@link IssuesPanel}.
 *
 * @author Aaron Madlon-Kay
 *
 */
public class IssuesPanelController implements IIssues {

    private static final String ACTION_KEY_JUMP_TO_SELECTED_ISSUE = "jumpToSelectedIssue";
    private static final String ACTION_KEY_FOCUS_ON_TYPES_LIST = "focusOnTypesList";
    private static final String ALL_FILES_PATTERN = ".*";
    private static final String NO_INSTRUCTIONS = "";

    private static final double INNER_SPLIT_INITIAL_RATIO = 0.25d;
    private static final double OUTER_SPLIT_INITIAL_RATIO = 0.5d;

    private final Window parent;
    private JFrame frame;
    private IssuesPanel panel;
    private TableColumnSizer colSizer;

    private String filePattern;
    private String instructions;

    private int selectedEntry = -1;
    private List<String> selectedTypes = Collections.emptyList();

    private IssueLoader loader;

    public IssuesPanelController(Window parent) {
        this.parent = parent;
    }

    private static final PropertyChangeSupport pcs = new PropertyChangeSupport(IssuesPanelController.class);

    private static boolean isJTextComponent(Component c) {
        return c instanceof JTextComponent;
    }

    @VisibleForTesting
    public IssuesPanel getPanel() {
        return panel;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    private void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    @SuppressWarnings("serial")
    synchronized void init() {
        if (frame != null) {
            // Regenerate menu bar to reflect current prefs
            frame.setJMenuBar(generateMenuBar());
            return;
        }

        frame = new JFrame(OStrings.getString("ISSUES_WINDOW_TITLE"));
        StaticUIUtils.setEscapeClosable(frame);
        StaticUIUtils.setWindowIcon(frame);
        if (Platform.isMacOSX()) {
            OSXIntegration.enableFullScreen(frame);
        }
        panel = new IssuesPanel();
        panel.setName("issues_panel");
        frame.add(panel);

        frame.setJMenuBar(generateMenuBar());

        frame.setPreferredSize(new Dimension(600, 400));
        frame.pack();
        frame.setLocationRelativeTo(parent);
        panel.innerSplitPane.setDividerLocation(INNER_SPLIT_INITIAL_RATIO);
        panel.outerSplitPane.setDividerLocation(OUTER_SPLIT_INITIAL_RATIO);

        StaticUIUtils.persistGeometry(frame, Preferences.ISSUES_WINDOW_GEOMETRY_PREFIX,
                () -> Preferences.setPreference(Preferences.ISSUES_WINDOW_DIVIDER_LOCATION_BOTTOM,
                        panel.outerSplitPane.getDividerLocation()));

        try {
            int bottomDL = Integer
                    .parseInt(Preferences.getPreference(Preferences.ISSUES_WINDOW_DIVIDER_LOCATION_BOTTOM));
            panel.outerSplitPane.setDividerLocation(bottomDL);
        } catch (NumberFormatException e) {
            // Ignore
        }

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                reset();
            }
        });
        setDefaultFont();
        panel.table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                viewSelectedIssueDetail();
                selectedEntry = getSelectedIssue().map(IIssue::getSegmentNumber).orElse(-1);
            }
        });
        setupShortCuts();
        MouseAdapter adapter = new IssuesPanelMouseAdapter();
        panel.table.addMouseListener(adapter);
        panel.table.addMouseMotionListener(adapter);

        panel.typeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateFilter();
                selectedTypes = getSelectedTypes();
            }
        });

        panel.closeButton.addActionListener(e -> StaticUIUtils.closeWindowByEvent(frame));
        panel.jumpButton.addActionListener(e -> jumpToSelectedIssue());
        panel.reloadButton.addActionListener(e -> refreshData(selectedEntry, selectedTypes));
        panel.showAllButton.addActionListener(e -> showAll());

        colSizer = TableColumnSizer.autoSize(panel.table, IssueColumn.DESCRIPTION.getIndex(), true);
        setupProjectChangeListener();
        setupFontChangeListener();
        firePropertyChange("panel", null, panel);
    }

    private void setupShortCuts() {
        panel.table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                ACTION_KEY_JUMP_TO_SELECTED_ISSUE);
        panel.table.getActionMap().put(ACTION_KEY_JUMP_TO_SELECTED_ISSUE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jumpToSelectedIssue();
            }
        });

        // Swap focus between the Types list and Issues table; don't allow
        // tabbing within the table because it's pointless. Maybe this would be
        // better accomplished by adjusting the focus traversal policy?
        panel.table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK),
                ACTION_KEY_FOCUS_ON_TYPES_LIST);
        panel.table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
                ACTION_KEY_FOCUS_ON_TYPES_LIST);
        panel.table.getActionMap().put(ACTION_KEY_FOCUS_ON_TYPES_LIST, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panel.typeList.isVisible()) {
                    panel.typeList.requestFocusInWindow();
                }
            }
        });
    }

    private void setupFontChangeListener() {
        CoreEvents.registerFontChangedEventListener(f -> {
            if (!Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT)) {
                f = new JTable().getFont();
            }
            setFont(f);
            viewSelectedIssueDetail();
        });
    }

    private void setupProjectChangeListener() {
        CoreEvents.registerProjectChangeListener(e -> {
            switch (e) {
            case CLOSE:
                SwingUtilities.invokeLater(() -> {
                    filePattern = ALL_FILES_PATTERN;
                    instructions = NO_INSTRUCTIONS;
                    reset();
                    frame.setVisible(false);
                });
                break;
            case MODIFIED:
                if (frame.isVisible()) {
                    SwingUtilities.invokeLater(() -> refreshData(selectedEntry, selectedTypes));
                }
                break;
            default:
            }
        });
    }

    private void setDefaultFont() {
        if (Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT)) {
            String fontName = Preferences.getPreference(Preferences.TF_SRC_FONT_NAME);
            int fontSize = Integer.parseInt(Preferences.getPreference(Preferences.TF_SRC_FONT_SIZE));
            setFont(new Font(fontName, Font.PLAIN, fontSize));
        }
    }

    JMenuBar generateMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = menuBar.add(new JMenu(OStrings.getString("ISSUES_WINDOW_MENU_OPTIONS")));
        setupTagsMenu(menu);

        Set<String> disabledProviders = IssueProviders.getDisabledProviderIds();
        IssueProviders.getIssueProviders().stream().sorted(Comparator.comparing(IIssueProvider::getId))
                .forEach(provider -> {
                    String label = OStrings.getString("ISSUES_WINDOW_MENU_OPTIONS_TOGGLE_PROVIDER",
                            provider.getName());
                    JCheckBoxMenuItem item = new JCheckBoxMenuItem(label);
                    item.addActionListener(e -> {
                        IssueProviders.setProviderEnabled(provider.getId(), item.isSelected());
                        refreshData(selectedEntry, selectedTypes);
                    });
                    item.setSelected(!disabledProviders.contains(provider.getId()));
                    menu.add(item);
                });

        menu.addSeparator();
        setupAskMenu(menu);
        return menuBar;
    }

    private static void setupTagsMenu(JMenu menu) {
        // Tags item is hard-coded because it is not disableable and is
        // implemented differently from all
        // others.
        JCheckBoxMenuItem tagsItem = new JCheckBoxMenuItem(
                OStrings.getString("ISSUES_WINDOW_MENU_OPTIONS_TOGGLE_PROVIDER",
                        OStrings.getString("ISSUES_TAGS_PROVIDER_NAME")));
        tagsItem.setSelected(true);
        tagsItem.setEnabled(false);
        menu.add(tagsItem);
    }

    private static void setupAskMenu(JMenu menu) {
        JCheckBoxMenuItem askItem = new JCheckBoxMenuItem(OStrings.getString("ISSUES_WINDOW_MENU_DONT_ASK"));
        askItem.setSelected(Preferences.isPreference(Preferences.ISSUE_PROVIDERS_DONT_ASK));
        askItem.addActionListener(
                e -> Preferences.setPreference(Preferences.ISSUE_PROVIDERS_DONT_ASK, askItem.isSelected()));
        menu.add(askItem);
    }

    void updateRollover() {
        // Rows here are all in terms of the view, not the model.
        TableModel model = panel.table.getModel();
        if (model instanceof IssuesTableModel) {
            IssuesTableModel imodel = (IssuesTableModel) model;
            int oldRow = imodel.getMouseoverRow();
            int oldCol = imodel.getMouseoverCol();
            Point point = panel.table.getMousePosition();
            int newRow = point == null ? -1 : panel.table.rowAtPoint(point);
            int newCol = point == null ? -1 : panel.table.columnAtPoint(point);
            boolean doRepaint = newRow != oldRow || newCol != oldCol;
            imodel.setMouseoverRow(newRow);
            imodel.setMouseoverCol(newCol);
            if (doRepaint) {
                Rectangle rect = panel.table.getCellRect(oldRow, IssueColumn.ACTION_BUTTON.getIndex(), true);
                panel.table.repaint(rect);
                rect = panel.table.getCellRect(newRow, IssueColumn.ACTION_BUTTON.getIndex(), true);
                panel.table.repaint(rect);
            }
        }
    }

    void setFont(Font font) {
        panel.typeList.setFont(font);
        DataTableStyling.applyFont(panel.table, font);
        panel.messageLabel.setFont(font);
    }

    void viewSelectedIssueDetail() {
        Optional<IIssue> issue = getSelectedIssue();
        issue.map(IIssue::getDetailComponent).ifPresent(comp -> {
            if (Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT)) {
                Font font = Core.getMainWindow().getApplicationFont();
                StaticUIUtils.visitHierarchy(comp, IssuesPanelController::isJTextComponent,
                        c -> c.setFont(font));
            }
            panel.outerSplitPane.setBottomComponent(comp);
        });
        panel.jumpButton.setEnabled(issue.isPresent());
    }

    void jumpToSelectedIssue() {
        getSelectedIssue().map(IIssue::getSegmentNumber).ifPresent(i -> {
            Core.getEditor().gotoEntry(i);
            JFrame mwf = Core.getMainWindow().getApplicationFrame();
            mwf.setState(Frame.NORMAL);
            mwf.toFront();
        });
    }

    Optional<IIssue> getIssueAt(Point p) {
        return getIssueAtRow(panel.table.rowAtPoint(p));
    }

    Optional<IIssue> getSelectedIssue() {
        return getIssueAtRow(panel.table.getSelectedRow());
    }

    Optional<IIssue> getIssueAtRow(int row) {
        if (row < 0) {
            return Optional.empty();
        }
        TableModel model = panel.table.getModel();
        if (!(model instanceof IssuesTableModel) || model.getRowCount() == 0) {
            return Optional.empty();
        }
        IssuesTableModel imodel = (IssuesTableModel) model;
        int realSelection = panel.table.getRowSorter().convertRowIndexToModel(row);
        return Optional.of(imodel.getIssueAt(realSelection));
    }

    List<String> getSelectedTypes() {
        List<String> types = getTypesAtRows(panel.typeList.getSelectedIndices());
        if (types.contains(ALL_TYPES)) {
            return Collections.singletonList(ALL_TYPES);
        }
        return types;
    }

    List<String> getTypesAtRows(int[] rows) {
        if (rows.length == 0) {
            return Collections.emptyList();
        }
        ListModel<String> model = panel.typeList.getModel();
        if (!(model instanceof IssuesTypeListModel)) {
            return Collections.emptyList();
        }
        IssuesTypeListModel tModel = (IssuesTypeListModel) model;
        return tModel.getTypesAt(rows);
    }

    void showPopupMenu(Component source, Point p, IIssue issue) {
        List<? extends JMenuItem> items = issue.getMenuComponents();
        if (items.isEmpty()) {
            return;
        }

        JPopupMenu menu = new JPopupMenu();
        items.forEach(menu::add);

        menu.show(source, p.x, p.y);
    }

    @Override
    public void showAll() {
        show(ALL_FILES_PATTERN, NO_INSTRUCTIONS, -1);
    }

    @Override
    public void showAll(String instructions) {
        show(ALL_FILES_PATTERN, instructions, -1);
    }

    @Override
    public void showForFiles(String filePattern) {
        show(filePattern, NO_INSTRUCTIONS, -1);
    }

    @Override
    public void showForFiles(String filePattern, String instructions) {
        show(filePattern, instructions, -1);
    }

    @Override
    public void showForFiles(String filePattern, int jumpToEntry) {
        show(filePattern, NO_INSTRUCTIONS, jumpToEntry);
    }

    private void show(String filePattern, String instructions, int jumpToEntry) {
        this.filePattern = filePattern;
        this.instructions = instructions;
        init();
        SwingUtilities.invokeLater(() -> refreshData(jumpToEntry, Collections.emptyList()));
    }

    void reset() {
        if (loader != null) {
            loader.cancel(true);
            loader = null;
        }
        frame.setTitle(OStrings.getString("ISSUES_WINDOW_TITLE"));
        panel.table.setModel(new DefaultTableModel());
        panel.typeList.setModel(new DefaultListModel<>());
        panel.outerSplitPane.setBottomComponent(panel.messageLabel);
        panel.messageLabel.setText(OStrings.getString("ISSUES_LOADING"));
        StaticUIUtils.setHierarchyEnabled(panel, false);
        panel.closeButton.setEnabled(true);
        panel.showAllButtonPanel.setVisible(!isShowingAllFiles());
        panel.instructionsPanel.setVisible(!instructions.equals(NO_INSTRUCTIONS));
        panel.instructionsTextArea.setText(instructions);
    }

    synchronized void refreshData(int jumpToEntry, List<String> jumpToTypes) {
        reset();
        if (!frame.isVisible()) {
            // Don't call setVisible if already visible, because the window will
            // steal focus
            frame.setVisible(true);
        }
        frame.setState(Frame.NORMAL);
        panel.progressBar.setValue(0);
        panel.progressBar.setMaximum(Core.getProject().getAllEntries().size());
        panel.progressBar.setVisible(true);
        panel.progressBar.setEnabled(true);
        loader = new IssueLoader(jumpToEntry, jumpToTypes);
        loader.execute();
    }

    class IssueLoader extends SwingWorker<List<IIssue>, Integer> {

        private final int jumpToEntry;
        private final List<String> jumpToTypes;

        private int progress = 0;

        IssueLoader(int jumpToEntry, @NotNull List<String> jumpToTypes) {
            this.jumpToEntry = jumpToEntry;
            this.jumpToTypes = jumpToTypes;
        }

        @Override
        protected List<IIssue> doInBackground() throws Exception {
            long start = System.currentTimeMillis();
            Stream<IIssue> tagErrors = Core.getTagValidation().listInvalidTags(filePattern).stream()
                    .map(TagIssue::new);
            List<IIssueProvider> providers = IssueProviders.getEnabledProviders();
            Stream<IIssue> providerIssues = getProviderIssues(providers, filePattern);
            List<IIssue> result = Stream.concat(tagErrors, providerIssues).collect(Collectors.toList());
            if (Log.isDebugEnabled()) {
                Log.logDebug(String.format("Issue detection took %.3f s",
                        (System.currentTimeMillis() - start) / 1000f));
            }
            return result;
        }

        private Stream<IIssue> getProviderIssues(List<IIssueProvider> providers, String filePattern) {
            Stream<Map.Entry<SourceTextEntry, TMXEntry>> entriesStream = Core.getProject().getAllEntries()
                    .parallelStream().filter(StreamUtil.patternFilter(filePattern, ste -> ste.getKey().file))
                    .filter(this::progressFilter).map(this::makeEntryPair).filter(Objects::nonNull);

            return entriesStream.flatMap(entry -> providers.stream()
                    .flatMap(provider -> provider.getIssues(entry.getKey(), entry.getValue()).stream()));
        }

        Map.Entry<SourceTextEntry, TMXEntry> makeEntryPair(SourceTextEntry ste) {
            IProject project = Core.getProject();
            if (!project.isProjectLoaded()) {
                return null;
            }
            TMXEntry tmxEntry = project.getTranslationInfo(ste);
            if (!tmxEntry.isTranslated()) {
                return null;
            }
            if (isShowingAllFiles() && DataUtils.isDuplicate(ste, tmxEntry)) {
                return null;
            }
            return new AbstractMap.SimpleImmutableEntry<>(ste, tmxEntry);
        }

        boolean progressFilter(SourceTextEntry ste) {
            boolean continu = !isCancelled();
            if (continu) {
                publish(ste.entryNum());
            }
            return continu;
        }

        @Override
        protected void process(List<Integer> chunks) {
            if (!chunks.isEmpty()) {
                progress += chunks.size();
                panel.progressBar.setValue(progress);
            }
        }

        @Override
        protected void done() {
            if (isCancelled()) {
                return;
            }
            List<IIssue> allIssues;
            try {
                allIssues = get();
            } catch (InterruptedException | ExecutionException e) {
                Log.log(e);
                JOptionPane.showMessageDialog(parent, e.getMessage(), OStrings.getString("ERROR_TITLE"),
                        JOptionPane.ERROR_MESSAGE);
                frame.setVisible(false);
                return;
            } catch (CancellationException e) {
                return;
            }

            if (allIssues.isEmpty()) {
                panel.messageLabel.setText(OStrings.getString("ISSUES_NO_ISSUES_FOUND"));
            }

            panel.progressBar.setVisible(false);
            StaticUIUtils.setHierarchyEnabled(panel, true);
            panel.typeList.setModel(new IssuesTypeListModel(allIssues));
            panel.table.setModel(new IssuesTableModel(panel.table, allIssues));
            TableRowSorter<?> sorter = (TableRowSorter<?>) panel.table.getRowSorter();
            sorter.setSortable(IssueColumn.ICON.getIndex(), false);
            sorter.toggleSortOrder(IssueColumn.SEG_NUM.getIndex());
            panel.typeList.setSelectedIndex(0);
            // Hide Types list if we have fewer than 3 items ("All" and at least
            // two others)
            boolean typeListIsVisible = panel.typeList.getModel().getSize() > 2;
            panel.typeListScrollPanel.setVisible(typeListIsVisible);
            if (typeListIsVisible) {
                SwingUtilities.invokeLater(() -> {
                    int width = panel.typeListScrollPanel.getPreferredSize().width + 10;
                    panel.innerSplitPane.setDividerLocation(width);
                });
            }
            colSizer.reset();
            colSizer.adjustTableColumns();
            if (!jumpToTypes.isEmpty()) {
                int[] indicies = ((IssuesTypeListModel) panel.typeList.getModel())
                        .indiciesOfTypes(jumpToTypes);
                if (indicies.length > 0) {
                    panel.typeList.setSelectedIndices(indicies);
                }
            }
            if (jumpToEntry >= 0) {
                IntStream.range(0, panel.table.getRowCount())
                        .filter(row -> (int) panel.table.getValueAt(row,
                                IssueColumn.SEG_NUM.getIndex()) >= jumpToEntry)
                        .findFirst().ifPresent(jump -> panel.table.changeSelection(jump, 0, false, false));
            }
            panel.table.requestFocusInWindow();
            super.firePropertyChange("table", null, null);
        }
    }

    private void updateFilter() {
        int[] selection = panel.typeList.getSelectedIndices();
        if (selection.length == 0) {
            return;
        }
        IssuesTypeListModel model = ((IssuesTypeListModel) panel.typeList.getModel());
        List<String> types = model.getTypesAt(selection);
        @SuppressWarnings("unchecked")
        TableRowSorter<IssuesTableModel> sorter = (TableRowSorter<IssuesTableModel>) panel.table
                .getRowSorter();
        sorter.setRowFilter(new RowFilter<IssuesTableModel, Integer>() {
            @Override
            public boolean include(RowFilter.Entry<? extends IssuesTableModel, ? extends Integer> entry) {
                return types.contains(ALL_TYPES)
                        || types.contains(entry.getStringValue(IssueColumn.TYPE.getIndex()));
            }
        });
        int totalItems = panel.table.getModel().getRowCount();
        if (types.contains(ALL_TYPES)) {
            updateTitle(totalItems);
        } else {
            updateTitle((int) model.getCountAt(selection), totalItems);
        }
        panel.table.changeSelection(0, 0, false, false);
    }

    private void updateTitle(int totalItems) {
        if (isShowingAllFiles()) {
            frame.setTitle(OStrings.getString("ISSUES_WINDOW_TITLE_TEMPLATE", totalItems));
        } else {
            String filePath = filePattern.replace("\\Q", "").replace("\\E", "");
            frame.setTitle(OStrings.getString("ISSUES_WINDOW_TITLE_FILE_TEMPLATE",
                    FilenameUtils.getName(filePath), totalItems));
        }
    }

    void updateTitle(int shownItems, int totalItems) {
        if (isShowingAllFiles()) {
            frame.setTitle(
                    OStrings.getString("ISSUES_WINDOW_TITLE_FILTERED_TEMPLATE", shownItems, totalItems));
        } else {
            String filePath = filePattern.replace("\\Q", "").replace("\\E", "");
            frame.setTitle(OStrings.getString("ISSUES_WINDOW_TITLE_FILE_FILTERED_TEMPLATE",
                    FilenameUtils.getName(filePath), shownItems, totalItems));
        }
    }

    private boolean isShowingAllFiles() {
        return ALL_FILES_PATTERN.equals(filePattern);
    }

    static final String ALL_TYPES = OStrings.getString("ISSUES_TYPE_ALL");

    private class IssuesPanelMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                jumpToSelectedIssue();
                return;
            }
            TableModel model = panel.table.getModel();
            if (model instanceof IssuesTableModel && e.getButton() == MouseEvent.BUTTON1
                    && ((IssuesTableModel) model).getMouseoverCol() == IssueColumn.ACTION_BUTTON.getIndex()) {
                doPopup(e);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                doPopup(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                doPopup(e);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            updateRollover();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            updateRollover();
        }

        private void doPopup(MouseEvent e) {
            getIssueAt(e.getPoint()).ifPresent(issue -> showPopupMenu(e.getComponent(), e.getPoint(), issue));
        }
    }
}
