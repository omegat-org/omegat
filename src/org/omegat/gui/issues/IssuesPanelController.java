/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.issues;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import org.apache.commons.io.FilenameUtils;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.DataUtils;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.Preferences;
import org.omegat.util.StreamUtil;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.DataTableStyling;
import org.omegat.util.gui.OSXIntegration;
import org.omegat.util.gui.ResourcesUtil;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.TableColumnSizer;

/**
 * A controller to orchestrate the {@link IssuesPanel}.
 * 
 * @author Aaron Madlon-Kay
 *
 */
public class IssuesPanelController implements IIssues {

    static final String ACTION_KEY_JUMP_TO_SELECTED_ISSUE = "jumpToSelectedIssue";
    static final String ACTION_KEY_FOCUS_ON_TYPES_LIST = "focusOnTypesList";
    static final String ALL_FILES_PATTERN = ".*";
    static final String NO_INSTRUCTIONS = "";

    static final double INNER_SPLIT_INITIAL_RATIO = 0.25d;
    static final double OUTER_SPLIT_INITIAL_RATIO = 0.5d;


    static final Icon SETTINGS_ICON = new ImageIcon(ResourcesUtil.getBundledImage("appbar.settings.active.png"));
    static final Icon SETTINGS_ICON_INACTIVE = new ImageIcon(
            ResourcesUtil.getBundledImage("appbar.settings.inactive.png"));
    static final Icon SETTINGS_ICON_PRESSED = new ImageIcon(
            ResourcesUtil.getBundledImage("appbar.settings.pressed.png"));
    static final Icon SETTINGS_ICON_INVISIBLE = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        @Override
        public int getIconWidth() {
            return SETTINGS_ICON.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return SETTINGS_ICON.getIconHeight();
        }
    };

    final Window parent;
    JFrame frame;
    IssuesPanel panel;
    TableColumnSizer colSizer;
    
    String filePattern;
    String instructions;
    int jumpToEntry;

    int mouseoverCol = -1;
    int mouseoverRow = -1;

    IssueLoader loader;

    public IssuesPanelController(Window parent) {
        this.parent = parent;
    }

    @SuppressWarnings("serial")
    synchronized void init() {
        if (frame != null) {
            return;
        }

        frame = new JFrame(OStrings.getString("ISSUES_WINDOW_TITLE"));
        StaticUIUtils.setEscapeClosable(frame);
        StaticUIUtils.setWindowIcon(frame);
        if (Platform.isMacOSX()) {
            OSXIntegration.enableFullScreen(frame);
        }
        panel = new IssuesPanel();
        frame.add(panel);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = menuBar.add(new JMenu(OStrings.getString("ISSUES_WINDOW_MENU_OPTIONS")));
        Set<String> disabledProviders = IssueProviders.getDisabledProviderIds();
        IssueProviders.ISSUE_PROVIDERS.stream().sorted(Comparator.comparing(IIssueProvider::getId))
                .forEach(provider -> {
                    String label = StringUtil.format(OStrings.getString("ISSUES_WINDOW_MENU_OPTIONS_TOGGLE_PROVIDER"),
                            provider.getName());
                    JCheckBoxMenuItem item = new JCheckBoxMenuItem(label);
                    item.addActionListener(e -> {
                        IssueProviders.setProviderEnabled(provider.getId(), item.isSelected());
                        refreshData();
                    });
                    item.setSelected(!disabledProviders.contains(provider.getId()));
                    menu.add(item);
                });
        frame.setJMenuBar(menuBar);

        frame.setPreferredSize(new Dimension(600, 400));
        frame.pack();
        frame.setLocationRelativeTo(parent);
        panel.innerSplitPane.setDividerLocation(INNER_SPLIT_INITIAL_RATIO);
        panel.outerSplitPane.setDividerLocation(OUTER_SPLIT_INITIAL_RATIO);

        StaticUIUtils.persistGeometry(frame, Preferences.ISSUES_WINDOW_GEOMETRY_PREFIX, () -> {
            Preferences.setPreference(Preferences.ISSUES_WINDOW_DIVIDER_LOCATION_TOP,
                    panel.innerSplitPane.getDividerLocation());
            Preferences.setPreference(Preferences.ISSUES_WINDOW_DIVIDER_LOCATION_BOTTOM,
                    panel.outerSplitPane.getDividerLocation());
        });

        try {
            int topDL = Integer.parseInt(Preferences.getPreference(Preferences.ISSUES_WINDOW_DIVIDER_LOCATION_TOP));
            panel.innerSplitPane.setDividerLocation(topDL);
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

        if (Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT)) {
            String fontName = Preferences.getPreference(Preferences.TF_SRC_FONT_NAME);
            int fontSize = Integer.parseInt(Preferences.getPreference(Preferences.TF_SRC_FONT_SIZE));
            setFont(new Font(fontName, Font.PLAIN, fontSize));
        }

        panel.table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                viewSelectedIssueDetail();
            }
        });

        panel.table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ACTION_KEY_JUMP_TO_SELECTED_ISSUE);
        panel.table.getActionMap().put(ACTION_KEY_JUMP_TO_SELECTED_ISSUE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jumpToSelectedIssue();
            }
        });

        // Swap focus between the Types list and Issues table; don't allow
        // tabbing within the table because it's pointless. Maybe this would be
        // better accomplished by adjusting the focus traversal policy?
        panel.table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK),
                ACTION_KEY_FOCUS_ON_TYPES_LIST);
        panel.table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), ACTION_KEY_FOCUS_ON_TYPES_LIST);
        panel.table.getActionMap().put(ACTION_KEY_FOCUS_ON_TYPES_LIST, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panel.typeList.isVisible()) {
                    panel.typeList.requestFocusInWindow();
                }
            }
        });

        panel.closeButton.addActionListener(e -> StaticUIUtils.closeWindowByEvent(frame));

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    jumpToSelectedIssue();
                } else if (e.getButton() == MouseEvent.BUTTON1 && mouseoverCol == IssueColumn.ACTION_BUTTON.index) {
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
        };
        panel.table.addMouseListener(adapter);
        panel.table.addMouseMotionListener(adapter);

        panel.typeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateFilter();
            }
        });

        panel.jumpButton.addActionListener(e -> jumpToSelectedIssue());

        panel.reloadButton.addActionListener(e -> refreshData());

        panel.showAllButton.addActionListener(e -> showAll());

        colSizer = TableColumnSizer.autoSize(panel.table, IssueColumn.DESCRIPTION.index, true);
        
        CoreEvents.registerProjectChangeListener(e -> {
            switch (e) {
            case CLOSE:
                SwingUtilities.invokeLater(() -> {
                    filePattern = ALL_FILES_PATTERN;
                    instructions = NO_INSTRUCTIONS;
                    jumpToEntry = -1;
                    reset();
                    frame.setVisible(false);
                });
                break;
            case MODIFIED:
                if (frame.isVisible()) {
                    SwingUtilities.invokeLater(IssuesPanelController.this::refreshData);
                }
                break;
            default:
            }
        });

        CoreEvents.registerFontChangedEventListener(f -> {
            if (!Preferences.isPreference(Preferences.PROJECT_FILES_USE_FONT)) {
                f = new JTable().getFont();
            }
            setFont(f);
            viewSelectedIssueDetail();
        });
    }

    void updateRollover() {
        // Rows here are all in terms of the view, not the model.
        Point point = panel.table.getMousePosition();
        int oldRow = mouseoverRow;
        int oldCol = mouseoverCol;
        int newRow = point == null ? -1 : panel.table.rowAtPoint(point);
        int newCol = point == null ? -1 : panel.table.columnAtPoint(point);
        boolean doRepaint = newRow != oldRow || newCol != oldCol;
        mouseoverRow = newRow;
        mouseoverCol = newCol;
        if (doRepaint) {
            Rectangle rect = panel.table.getCellRect(oldRow, IssueColumn.ACTION_BUTTON.index, true);
            panel.table.repaint(rect);
            rect = panel.table.getCellRect(newRow, IssueColumn.ACTION_BUTTON.index, true);
            panel.table.repaint(rect);
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
                StaticUIUtils.listHierarchy(comp).stream().filter(c -> c instanceof JTextComponent)
                        .forEach(c -> c.setFont(font));
            }
            panel.outerSplitPane.setBottomComponent(comp);
        });
        panel.jumpButton.setEnabled(issue.isPresent());
    }

    void jumpToSelectedIssue() {
        getSelectedIssue().map(IIssue::getSegmentNumber).ifPresent(Core.getEditor()::gotoEntry);
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
        this.jumpToEntry = jumpToEntry;
        init();
        SwingUtilities.invokeLater(this::refreshData);
    }

    void reset() {
        if (loader != null) {
            loader.cancel(true);
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

    synchronized void refreshData() {
        reset();
        if (!frame.isVisible()) {
            // Don't call setVisible if already visible, because the window will
            // steal focus
            frame.setVisible(true);
        }
        frame.setState(JFrame.NORMAL);
        panel.progressBar.setValue(0);
        panel.progressBar.setMaximum(Core.getProject().getAllEntries().size());
        panel.progressBar.setVisible(true);
        panel.progressBar.setEnabled(true);
        loader = new IssueLoader();
        loader.execute();
    }

    Map.Entry<SourceTextEntry, TMXEntry> makeEntryPair(SourceTextEntry ste) {
        TMXEntry tmxEntry = Core.getProject().getTranslationInfo(ste);
        if (tmxEntry == null) {
            // Project was closed
            return null;
        }
        if (!tmxEntry.isTranslated()) {
            return null;
        }
        if (isShowingAllFiles() && DataUtils.isDuplicate(ste, tmxEntry)) {
            return null;
        }
        return new AbstractMap.SimpleImmutableEntry<SourceTextEntry, TMXEntry>(ste, tmxEntry);
    }

    class IssueLoader extends SwingWorker<List<IIssue>, Integer> {

        private int progress = 0;

        @Override
        protected List<IIssue> doInBackground() throws Exception {
            long start = System.currentTimeMillis();
            Stream<IIssue> tagErrors = Core.getTagValidation().listInvalidTags(filePattern).stream()
                    .map(TagIssue::new);
            List<IIssueProvider> providers = IssueProviders.getEnabledProviders();
            Stream<IIssue> providerIssues = Core.getProject().getAllEntries().parallelStream()
                    .filter(StreamUtil.patternFilter(filePattern, ste -> ste.getKey().file))
                    .filter(this::progressFilter).map(IssuesPanelController.this::makeEntryPair)
                    .filter(Objects::nonNull).flatMap(e -> providers.stream()
                            .flatMap(provider -> provider.getIssues(e.getKey(), e.getValue()).stream()));
            List<IIssue> result = Stream.concat(tagErrors, providerIssues).collect(Collectors.toList());
            Logger.getLogger(IssuesPanelController.class.getName()).log(Level.FINEST,
                    () -> String.format("Issue detection took %.3f s", (System.currentTimeMillis() - start) / 1000f));
            return result;
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
                panel.progressBar.setValue(progress += chunks.size());
            }
        }

        @Override
        protected void done() {
            List<IIssue> allIssues = Collections.emptyList();
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
            panel.typeList.setModel(new TypeListModel(allIssues));
            panel.table.setModel(new IssuesTableModel(allIssues));
            TableRowSorter<?> sorter = (TableRowSorter<?>) panel.table.getRowSorter();
            sorter.setSortable(IssueColumn.ICON.index, false);
            sorter.toggleSortOrder(IssueColumn.SEG_NUM.index);
            panel.typeList.setSelectedIndex(0);
            // Hide Types list if we have fewer than 3 items ("All" and at least
            // two others)
            panel.typeListScrollPanel.setVisible(panel.typeList.getModel().getSize() > 2);
            if (panel.typeListScrollPanel.isVisible() && panel.innerSplitPane.getDividerLocation() == 0) {
                panel.innerSplitPane.setDividerLocation(INNER_SPLIT_INITIAL_RATIO);
            }
            colSizer.reset();
            colSizer.adjustTableColumns();
            if (jumpToEntry >= 0) {
                IntStream.range(0, panel.table.getRowCount())
                        .filter(row -> (int) panel.table.getValueAt(row, IssueColumn.SEG_NUM.index) == jumpToEntry)
                        .findFirst().ifPresent(jump -> panel.table.changeSelection(jump, 0, false, false));
            }
            panel.table.requestFocusInWindow();
        }
    }

    void updateFilter() {
        int selection = panel.typeList.getSelectedIndex();
        if (selection < 0) {
            return;
        }
        TypeListModel model = ((TypeListModel) panel.typeList.getModel());
        String type = model.getTypeAt(selection);
        @SuppressWarnings("unchecked")
        TableRowSorter<IssuesTableModel> sorter = (TableRowSorter<IssuesTableModel>) panel.table.getRowSorter();
        sorter.setRowFilter(new RowFilter<IssuesTableModel, Integer>() {
            @Override
            public boolean include(RowFilter.Entry<? extends IssuesTableModel, ? extends Integer> entry) {
                return type == ALL_TYPES || entry.getStringValue(IssueColumn.TYPE.index).equals(type);
            }
        });
        int totalItems = panel.table.getModel().getRowCount();
        if (type == ALL_TYPES) {
            updateTitle(totalItems);
        } else {
            updateTitle((int) model.getCountAt(selection), totalItems);
        }
        panel.table.changeSelection(0, 0, false, false);
    }

    void updateTitle(int totalItems) {
        if (isShowingAllFiles()) {
            frame.setTitle(StringUtil.format(OStrings.getString("ISSUES_WINDOW_TITLE_TEMPLATE"), totalItems));
        } else {
            String filePath = filePattern.replace("\\Q", "").replace("\\E", "");
            frame.setTitle(StringUtil.format(OStrings.getString("ISSUES_WINDOW_TITLE_FILE_TEMPLATE"),
                    FilenameUtils.getName(filePath), totalItems));
        }
    }

    void updateTitle(int shownItems, int totalItems) {
        if (isShowingAllFiles()) {
            frame.setTitle(StringUtil.format(OStrings.getString("ISSUES_WINDOW_TITLE_FILTERED_TEMPLATE"), shownItems,
                    totalItems));
        } else {
            String filePath = filePattern.replace("\\Q", "").replace("\\E", "");
            frame.setTitle(StringUtil.format(OStrings.getString("ISSUES_WINDOW_TITLE_FILE_FILTERED_TEMPLATE"),
                    FilenameUtils.getName(filePath), shownItems, totalItems));
        }
    }

    boolean isShowingAllFiles() {
        return ALL_FILES_PATTERN.equals(filePattern);
    }

    enum IssueColumn {
        SEG_NUM(0, OStrings.getString("ISSUES_TABLE_COLUMN_ENTRY_NUM"), Integer.class),
        ICON(1, "", Icon.class),
        TYPE(2, OStrings.getString("ISSUES_TABLE_COLUMN_TYPE"), String.class),
        DESCRIPTION(3, OStrings.getString("ISSUES_TABLE_COLUMN_DESCRIPTION"), String.class),
        ACTION_BUTTON(4, "", Icon.class);

        private final int index;
        private final String label;
        private final Class<?> clazz;

        private IssueColumn(int index, String label, Class<?> clazz) {
            this.index = index;
            this.label = label;
            this.clazz = clazz;
        }

        static IssueColumn get(int index) {
            return IssueColumn.values()[index];
        }
    }

    Icon getActionMenuIcon(IIssue issue, int modelRow, int col) {
        // The row argument is in terms of the model while mouseoverRow is in
        // terms of the view, so convert first.
        int viewRow = panel.table.getRowSorter().convertRowIndexToView(modelRow);
        if (!issue.hasMenuComponents()) {
            return SETTINGS_ICON_INVISIBLE;
        } else if (panel.table.getSelectedRow() == viewRow) {
            // Show "pressed" version here for better contrast against the table
            // selection highlight.
            return SETTINGS_ICON_PRESSED;
        } else if (viewRow == mouseoverRow && col == mouseoverCol) {
            return SETTINGS_ICON;
        } else if (viewRow == mouseoverRow) {
            return SETTINGS_ICON_INACTIVE;
        } else {
            return SETTINGS_ICON_INVISIBLE;
        }
    }

    @SuppressWarnings("serial")
    class IssuesTableModel extends AbstractTableModel {

        private final List<IIssue> issues;

        public IssuesTableModel(List<IIssue> issues) {
            this.issues = issues;
        }

        @Override
        public int getRowCount() {
            return issues.size();
        }

        @Override
        public int getColumnCount() {
            return IssueColumn.values().length;
        }

        @Override
        public String getColumnName(int column) {
            return IssueColumn.get(column).label;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            IIssue iss = issues.get(rowIndex);
            switch (IssueColumn.get(columnIndex)) {
            case SEG_NUM:
                return iss.getSegmentNumber();
            case ICON:
                return iss.getIcon();
            case TYPE:
                return iss.getTypeName();
            case DESCRIPTION:
                return iss.getDescription();
            case ACTION_BUTTON:
                return getActionMenuIcon(iss, rowIndex, columnIndex);
            }
            throw new IllegalArgumentException("Unknown column requested: " + columnIndex);
        }

        public IIssue getIssueAt(int rowIndex) {
            return issues.get(rowIndex);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return IssueColumn.get(columnIndex).clazz;
        }
    }

    static final String ALL_TYPES = new String(OStrings.getString("ISSUES_TYPE_ALL"));

    @SuppressWarnings("serial")
    class TypeListModel extends AbstractListModel<String> {

        private final List<Map.Entry<String, Long>> types;

        public TypeListModel(List<IIssue> issues) {
            this.types = calculateData(issues);
        }

        List<Map.Entry<String, Long>> calculateData(List<IIssue> issues) {
            Map<String, Long> counts = issues.stream()
                    .map(IIssue::getTypeName)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            List<Map.Entry<String, Long>> result = new ArrayList<>();
            result.add(new AbstractMap.SimpleImmutableEntry<String, Long>(ALL_TYPES,
                    (long) issues.size()));
            result.addAll(counts.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                    .collect(Collectors.toList()));
            return result;
        }

        @Override
        public int getSize() {
            return types.size();
        }

        @Override
        public String getElementAt(int index) {
            Map.Entry<String, Long> entry = types.get(index);
            return StringUtil.format(OStrings.getString("ISSUES_TYPE_SUMMARY_TEMPLATE"), entry.getKey(),
                    entry.getValue());
        }

        String getTypeAt(int index) {
            return types.get(index).getKey();
        }

        long getCountAt(int index) {
            return types.get(index).getValue();
        }
    }
}
