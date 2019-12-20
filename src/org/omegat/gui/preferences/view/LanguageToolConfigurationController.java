/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Lev Abashkin, Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.preferences.view;

import static org.omegat.languagetools.LanguageToolNativeBridge.getLTLanguage;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.xml.parsers.ParserConfigurationException;

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.Tools;
import org.omegat.core.Core;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.languagetools.LanguageToolPrefs;
import org.omegat.languagetools.LanguageToolWrapper;
import org.omegat.languagetools.LanguageToolWrapper.BridgeType;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.xml.sax.SAXException;

/**
 * @author Panagiotis Minos
 * @author Lev Abashkin
 * @author Aaron Madlon-Kay
 */
public class LanguageToolConfigurationController extends BasePreferencesController {

    private static final String NEW_RULE_PATTERN = "^[A-Za-z_.]+$";
    private BridgeType selectedBridgeType;
    private Set<String> disabledCategories, disabledRuleIds, enabledRuleIds;
    private String targetLanguageCode;

    private LanguageToolConfigurationPanel panel;

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_LANGUAGETOOL");
    }

    private void initGui() {
        panel = new LanguageToolConfigurationPanel();
        org.openide.awt.Mnemonics.setLocalizedText(panel.bridgeNativeRadioButton,
                StringUtil.format(OStrings.getString("GUI_LANGUAGETOOL_NATIVE_BRIDGE"), JLanguageTool.VERSION));
        panel.bridgeNativeRadioButton.addActionListener(e -> handleBridgeTypeChange(BridgeType.NATIVE));
        panel.bridgeLocalRadioButton.addActionListener(e -> handleBridgeTypeChange(BridgeType.LOCAL_INSTALLATION));
        panel.bridgeRemoteRadioButton.addActionListener(e -> handleBridgeTypeChange(BridgeType.REMOTE_URL));
        panel.directoryChooseButton.addActionListener(e -> chooseLocalInstallation());
        panel.addRuleButton.addActionListener(e -> addRule());
        panel.deleteRuleButton.addActionListener(e -> deleteRules());
        if (Core.getProject().isProjectLoaded()) {
            targetLanguageCode = Core.getProject().getProjectProperties().getTargetLanguage()
                    .getLanguageCode();
            initWithLangs();
        } else {
            initWithoutLangs();
        }
    }

    private void initWithoutLangs() {
        disableRulesUI(OStrings.getString("GUI_LANGUAGETOOL_RULES_UNAVAILABLE_NO_PROJECT"));
    }

    private void chooseLocalInstallation() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileFilter filter = new FileNameExtensionFilter(OStrings.getString("GUI_LANGUAGETOOL_JAR_FILE_FILTER"), "jar");
        fileChooser.setFileFilter(filter);
        fileChooser.setDialogTitle(OStrings.getString("GUI_LANGUAGETOOL_FILE_CHOOSER_TITLE"));
        int result = fileChooser.showOpenDialog(panel);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            panel.localServerJarPathTextField.setText(file.getAbsolutePath());
        }
    }

    private void addRule() {
        String newRuleId = (String) JOptionPane.showInputDialog(panel,
                OStrings.getString("GUI_LANGUAGETOOL_ADD_RULE_MESSAGE"),
                OStrings.getString("GUI_LANGUAGETOOL_ADD_RULE_DIALOG_TITLE"), JOptionPane.PLAIN_MESSAGE, null, null,
                null);

        if (newRuleId == null) {
            return; // Nothing to do
        }

        if (!Pattern.matches(NEW_RULE_PATTERN, newRuleId)) {
            JOptionPane.showMessageDialog(panel, OStrings.getString("GUI_LANGUAGETOOL_BAD_RULE_ID"));
            return;
        }

        DefaultTreeModel model = (DefaultTreeModel) panel.rulesTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) panel.rulesTree.getModel().getRoot();
        DefaultMutableTreeNode externalCategoryNode = null;
        // Check if external category node already exists (should be last
        // child)
        CategoryNode lastCategoryNode = (CategoryNode) root.getLastChild();
        if (lastCategoryNode.getCategory().getId().toString().equals(ExternalRule.CATEGORY_ID)) {
            // Check if rule already exists
            for (int i = 0; i < lastCategoryNode.getChildCount(); i++) {
                if (((RuleNode) lastCategoryNode.getChildAt(i)).getRule().getId().equals(newRuleId)) {
                    JOptionPane.showMessageDialog(panel, OStrings.getString("GUI_LANGUAGETOOL_RULE_ALREADY_EXISTS"));
                    return;
                }
            }
            externalCategoryNode = lastCategoryNode;
        } else {
            externalCategoryNode = new CategoryNode(ExternalRule.DEFAULT_CATEGORY, true);
            model.insertNodeInto(externalCategoryNode, root, root.getChildCount());
        }

        DefaultMutableTreeNode newRuleNode = new RuleNode(new ExternalRule(newRuleId), true);
        model.insertNodeInto(newRuleNode, externalCategoryNode, externalCategoryNode.getChildCount());
        if (!((CategoryNode) externalCategoryNode).isEnabled()) {
            ((CategoryNode) externalCategoryNode).setEnabled(true);
            model.nodeChanged(externalCategoryNode);
        }
        panel.rulesTree.scrollPathToVisible(new TreePath(newRuleNode.getPath()));
    }

    private void deleteRules() {
        TreePath[] currentSelections = panel.rulesTree.getSelectionPaths();
        if (currentSelections != null) {
            DefaultTreeModel model = (DefaultTreeModel) panel.rulesTree.getModel();
            Stream.of(currentSelections).map(TreePath::getLastPathComponent)
                    .filter(LanguageToolConfigurationController::isExternalRuleNode).forEach(node -> {
                        MutableTreeNode currentNode = (MutableTreeNode) node;
                        MutableTreeNode parent = (MutableTreeNode) currentNode.getParent();
                        model.removeNodeFromParent(currentNode);
                        if (parent.getChildCount() == 0) {
                            model.removeNodeFromParent(parent);
                        }
                    });
        }
    }

    private void disableRulesUI(String message) {
        panel.rulesMessagePanel.setVisible(true);
        panel.rulesMessageLabel.setText(message);
        panel.rulesMessageLabel.setEnabled(false);
        panel.rulesScrollPane.setVisible(false);
        panel.rulesButtonsPanel.setVisible(false);
    }

    private void initWithLangs() {
        panel.rulesMessagePanel.setVisible(false);
        panel.rulesTree.addTreeSelectionListener(e -> updateButtonState());
        panel.rulesTree.setCellRenderer(new CheckBoxTreeCellRenderer());
        TreeListener.install(panel.rulesTree);
    }

    private void loadRules(org.omegat.util.Language sourceLang, org.omegat.util.Language targetLang) {
        // Load rule tree
        org.languagetool.Language targetLtLang = getLTLanguage(targetLang);
        org.languagetool.Language sourceLtLang = getLTLanguage(sourceLang);

        if (targetLtLang == null) {
            disableRulesUI(OStrings.getString("GUI_LANGUAGETOOL_RULES_UNAVAILABLE"));
            return;
        }

        JLanguageTool ltInstance;
        try {
            ltInstance = new JLanguageTool(targetLtLang);
        } catch (Throwable e) {
            // Disable tree and return if instance couldn't be gotten
            disableRulesUI(OStrings.getString("GUI_LANGUAGETOOL_RULES_UNAVAILABLE_ERROR"));
            Log.log(e);
            return;
        }

        List<Rule> rules = ltInstance.getAllRules();
        if (sourceLtLang != null) {
            try {
                rules.addAll(Tools.getBitextRules(sourceLtLang, targetLtLang));
            } catch (IOException | ParserConfigurationException | SAXException e) {
                Log.log(e);
            }
        }

        // Collect internal rule IDs
        List<String> internalRuleIds = rules.stream().map(Rule::getId).collect(Collectors.toList());

        // Remove rule IDs of rules disabled by default from set of disabled
        // rules
        rules.stream().filter(Rule::isDefaultOff).map(Rule::getId).forEach(disabledRuleIds::remove);

        // Create ExternalRule instances for rules not found in built-in LT
        // and add them to our rules list
        List<String> externalRuleIds = new ArrayList<>(disabledRuleIds);
        externalRuleIds.addAll(enabledRuleIds);
        externalRuleIds.removeAll(internalRuleIds);
        externalRuleIds.stream().distinct().map(ExternalRule::new).forEach(rules::add);

        DefaultMutableTreeNode rootNode = createTree(rules);
        panel.rulesTree.setModel(getTreeModel(rootNode));
        panel.rulesTree
                .applyComponentOrientation(ComponentOrientation.getOrientation(targetLtLang.getLocale()));

        updateButtonState();
    }

    private DefaultMutableTreeNode createTree(List<Rule> rules) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Rules");
        String lastRuleId = null;
        Map<String, DefaultMutableTreeNode> parents = new TreeMap<>();
        for (Rule rule : rules) {
            if (!parents.containsKey(rule.getCategory().getId().toString())) {
                boolean enabled = true;
                if (disabledCategories != null && disabledCategories.contains(rule.getCategory().getId().toString())) {
                    enabled = false;
                }
                if (rule.getCategory().isDefaultOff()) {
                    enabled = false;
                }
                DefaultMutableTreeNode categoryNode = new CategoryNode(rule.getCategory(), enabled);
                root.add(categoryNode);
                parents.put(rule.getCategory().getId().toString(), categoryNode);
            }
            if (!rule.getId().equals(lastRuleId)) {
                RuleNode ruleNode = new RuleNode(rule, getEnabledState(rule));
                CategoryNode parent = (CategoryNode) parents.get(rule.getCategory().getId().toString());
                parent.add(ruleNode);
                // Ensure that a category appears enabled if any of its children
                // are enabled. We don't persist enabled categories, only
                // disabled ones, so this is purely cosmetic.
                if (ruleNode.isEnabled()) {
                    parent.setEnabled(true);
                }
            }
            lastRuleId = rule.getId();
        }
        return root;
    }

    void updateButtonState() {
        TreePath[] selected = panel.rulesTree.getSelectionPaths();
        boolean deletable = selected != null && Stream.of(selected).map(TreePath::getLastPathComponent)
                .allMatch(LanguageToolConfigurationController::isExternalRuleNode);
        panel.deleteRuleButton.setEnabled(deletable);
    }

    static boolean isExternalRuleNode(Object node) {
        return node instanceof RuleNode && ((RuleNode) node).getRule() instanceof ExternalRule;
    }

    private boolean getEnabledState(Rule rule) {
        boolean ret = true;
        if (disabledRuleIds.contains(rule.getId())) {
            ret = false;
        }
        if (disabledCategories.contains(rule.getCategory().getId().toString())) {
            ret = false;
        }
        if ((rule.isDefaultOff() || rule.getCategory().isDefaultOff()) && !enabledRuleIds.contains(rule.getId())) {
            ret = false;
        }
        if (rule.isDefaultOff() && rule.getCategory().isDefaultOff() && enabledRuleIds.contains(rule.getId())) {
            disabledCategories.remove(rule.getCategory().getId().toString());
        }
        return ret;
    }

    private DefaultTreeModel getTreeModel(DefaultMutableTreeNode rootNode) {
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        treeModel.addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getTreePath().getLastPathComponent();
                int index = e.getChildIndices()[0];
                node = (DefaultMutableTreeNode) node.getChildAt(index);
                if (node instanceof RuleNode) {
                    RuleNode o = (RuleNode) node;

                    if (o.getRule().isDefaultOff() || o.getRule().getCategory().isDefaultOff()) {
                        if (o.isEnabled()) {
                            enabledRuleIds.add(o.getRule().getId());
                        } else {
                            enabledRuleIds.remove(o.getRule().getId());
                        }
                    } else {
                        if (o.isEnabled()) {
                            disabledRuleIds.remove(o.getRule().getId());
                        } else {
                            disabledRuleIds.add(o.getRule().getId());
                        }
                        // External rules are on by default.
                        // Always keep their IDs in one of the sets.
                        if (o.getRule().getCategory().getId().toString().equals(ExternalRule.CATEGORY_ID)) {
                            if (o.isEnabled()) {
                                enabledRuleIds.add(o.getRule().getId());
                            } else {
                                enabledRuleIds.remove(o.getRule().getId());
                            }
                        }
                    }

                    // Clean up disabled rules already disabled by category.
                    // Do not remove IDs of external rules.
                    if (disabledCategories.contains(o.getRule().getCategory().getId().toString())
                            && !o.getRule().getCategory().getId().toString().equals(ExternalRule.CATEGORY_ID)) {
                        disabledRuleIds.remove(o.getRule().getId());
                    }
                }
                if (node instanceof CategoryNode) {
                    CategoryNode o = (CategoryNode) node;
                    if (o.isEnabled()) {
                        disabledCategories.remove(o.getCategory().getId().toString());
                    } else {
                        disabledCategories.add(o.getCategory().getId().toString());
                    }
                }
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                DefaultMutableTreeNode newNode = (DefaultMutableTreeNode) e.getChildren()[0];
                if (newNode instanceof RuleNode) {
                    enabledRuleIds.add(((RuleNode) newNode).getRule().getId());
                }
                if (newNode instanceof CategoryNode) {

                }
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                DefaultMutableTreeNode removedNode = (DefaultMutableTreeNode) e.getChildren()[0];
                if (removedNode instanceof RuleNode) {
                    if (((RuleNode) removedNode).isEnabled()) {
                        enabledRuleIds.remove(((RuleNode) removedNode).getRule().getId());
                    } else {
                        disabledRuleIds.remove(((RuleNode) removedNode).getRule().getId());
                    }
                }
                if (removedNode instanceof CategoryNode) {
                    disabledCategories.remove(((CategoryNode) removedNode).getCategory().getId().toString());
                }
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
            }
        });
        return treeModel;
    }

    private void handleBridgeTypeChange(BridgeType type) {
        selectedBridgeType = type;
        switch (type) {
        case NATIVE:
            panel.localServerJarPathTextField.setEnabled(false);
            panel.directoryChooseButton.setEnabled(false);
            panel.urlTextField.setEnabled(false);
            break;
        case REMOTE_URL:
            panel.localServerJarPathTextField.setEnabled(false);
            panel.directoryChooseButton.setEnabled(false);
            panel.urlTextField.setEnabled(true);
            break;
        case LOCAL_INSTALLATION:
            panel.localServerJarPathTextField.setEnabled(true);
            panel.directoryChooseButton.setEnabled(true);
            panel.urlTextField.setEnabled(false);
            break;
        }
    }

    @Override
    protected void initFromPrefs() {
        BridgeType type = LanguageToolPrefs.getBridgeType();
        switch (type) {
        case NATIVE:
            panel.bridgeNativeRadioButton.setSelected(true);
            break;
        case REMOTE_URL:
            panel.bridgeRemoteRadioButton.setSelected(true);
            break;
        case LOCAL_INSTALLATION:
            panel.bridgeLocalRadioButton.setSelected(true);
            break;
        }

        panel.urlTextField.setText(LanguageToolPrefs.getRemoteUrl());
        panel.localServerJarPathTextField.setText(LanguageToolPrefs.getLocalServerJarPath());

        if (targetLanguageCode != null) {
            disabledCategories = LanguageToolPrefs.getDisabledCategories(targetLanguageCode);
            disabledRuleIds = LanguageToolPrefs.getDisabledRules(targetLanguageCode);
            enabledRuleIds = LanguageToolPrefs.getEnabledRules(targetLanguageCode);
            loadRules(Core.getProject().getProjectProperties().getSourceLanguage(),
                    Core.getProject().getProjectProperties().getTargetLanguage());
        }

        handleBridgeTypeChange(type);
    }

    @Override
    public void restoreDefaults() {
        panel.bridgeNativeRadioButton.setSelected(true);
        panel.urlTextField.setText("");
        panel.localServerJarPathTextField.setText("");

        if (targetLanguageCode != null) {
            disabledCategories = LanguageToolPrefs.getDefaultDisabledCategories();
            disabledRuleIds = LanguageToolPrefs.getDefaultDisabledRules();
            enabledRuleIds = Collections.emptySet();
            loadRules(Core.getProject().getProjectProperties().getSourceLanguage(),
                    Core.getProject().getProjectProperties().getTargetLanguage());
        }

        handleBridgeTypeChange(LanguageToolPrefs.DEFAULT_BRIDGE_TYPE);
    }

    @Override
    public void persist() {
        LanguageToolPrefs.setBridgeType(selectedBridgeType);
        LanguageToolPrefs.setRemoteUrl(panel.urlTextField.getText());
        LanguageToolPrefs.setLocalServerJarPath(panel.localServerJarPathTextField.getText());

        if (targetLanguageCode != null) {
            LanguageToolPrefs.setDisabledCategories(disabledCategories, targetLanguageCode);
            LanguageToolPrefs.setDisabledRules(disabledRuleIds, targetLanguageCode);
            LanguageToolPrefs.setEnabledRules(enabledRuleIds, targetLanguageCode);
            LanguageToolWrapper.setBridgeFromCurrentProject();
        }
        SwingUtilities.invokeLater(() -> {
            if (Core.getProject().isProjectLoaded()) {
                Core.getEditor().refreshView(true);
            }
        });
    }

    /**
     * Helper class to work with manually defined rules unknown to built-in
     * LanguageTool
     *
     * @author Lev Abashkin
     */
    static class ExternalRule extends Rule {

        public static final String CATEGORY_ID = "EXTERNAL";
        public static final Category DEFAULT_CATEGORY = new Category(new CategoryId(CATEGORY_ID),
                OStrings.getString("GUI_LANGUAGETOOL_EXTERNAL_CATEGORY_NAME"));

        private final String id;

        ExternalRule(String id) {
            this.id = id;
            setCategory(DEFAULT_CATEGORY);
            setDefaultOn();
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getDescription() {
            return id;
        }

        @Override
        public RuleMatch[] match(AnalyzedSentence as) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * @author Panagiotis Minos
     */
    static class TreeListener implements KeyListener, MouseListener {

        static void install(JTree tree) {
            TreeListener listener = new TreeListener(tree);
            tree.addMouseListener(listener);
            tree.addKeyListener(listener);
        }

        private static final Dimension CHECKBOX_DIMENSION = new JCheckBox().getPreferredSize();

        private final JTree tree;

        TreeListener(JTree tree) {
            this.tree = tree;
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                TreePath[] paths = tree.getSelectionPaths();
                if (paths != null) {
                    for (TreePath path : paths) {
                        handle(path);
                    }
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());
            if ((path != null) && (path.getPathCount() > 0)) {
                if (isValidNode(path.getLastPathComponent())) {
                    if (isOverCheckBox(e.getX(), e.getY(), path)) {
                        handle(path);
                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        private void handle(TreePath path) {
            if ((path != null) && (path.getPathCount() > 0)) {
                if (path.getLastPathComponent() instanceof CategoryNode) {
                    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

                    CategoryNode node = (CategoryNode) path.getLastPathComponent();
                    node.setEnabled(!node.isEnabled());
                    model.nodeChanged(node);

                    for (int i = 0; i < node.getChildCount(); i++) {
                        RuleNode child = (RuleNode) node.getChildAt(i);
                        if (child.isEnabled() != node.isEnabled()) {
                            child.setEnabled(node.isEnabled());
                            model.nodeChanged(child);
                        }
                    }
                }
                if (path.getLastPathComponent() instanceof RuleNode) {
                    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

                    RuleNode node = (RuleNode) path.getLastPathComponent();
                    node.setEnabled(!node.isEnabled());
                    model.nodeChanged(node);

                    if (node.isEnabled()) {
                        CategoryNode parent = (CategoryNode) node.getParent();
                        parent.setEnabled(true);
                    }
                    model.nodeChanged(node.getParent());
                }
            }
        }

        private boolean isOverCheckBox(int x, int y, TreePath path) {
            int offset = tree.getPathBounds(path).x + CHECKBOX_DIMENSION.width;
            return x <= offset;
        }

        private boolean isValidNode(Object c) {
            return ((c instanceof CategoryNode) || (c instanceof RuleNode));
        }
    }

    /**
     * @author Panagiotis Minos
     */
    @SuppressWarnings("serial")
    static class RuleNode extends DefaultMutableTreeNode {

        private final Rule rule;
        private boolean enabled;

        RuleNode(Rule rule, boolean enabled) {
            super(rule);
            this.rule = rule;
            this.enabled = enabled;
        }

        Rule getRule() {
            return rule;
        }

        boolean isEnabled() {
            return enabled;
        }

        void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public String toString() {
            return rule.getDescription();
        }
    }

    /**
     * @author Panagiotis Minos
     */
    @SuppressWarnings("serial")
    static class CategoryNode extends DefaultMutableTreeNode {

        private final Category category;
        private boolean enabled;

        CategoryNode(Category category, boolean enabled) {
            super(category);
            this.category = category;
            this.enabled = enabled;
        }

        Category getCategory() {
            return category;
        }

        boolean isEnabled() {
            return enabled;
        }

        void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public String toString() {
            int children = this.getChildCount();
            int selected = 0;
            for (int i = 0; i < children; i++) {
                RuleNode child = (RuleNode) this.getChildAt(i);
                if (child.isEnabled()) {
                    selected++;
                }
            }
            return String.format("%s (%d/%d)", category.getName(), selected, children);
        }
    }

    /**
     * @author Panagiotis Minos
     */
    @SuppressWarnings("serial")
    static class CheckBoxTreeCellRenderer extends JPanel implements TreeCellRenderer {

        private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        private final JCheckBox checkBox = new JCheckBox();

        private Component defaultComponent;

        CheckBoxTreeCellRenderer() {
            setLayout(new BorderLayout());
            setOpaque(false);
            checkBox.setOpaque(false);
            renderer.setLeafIcon(null);
            renderer.setOpenIcon(null);
            renderer.setClosedIcon(null);
            add(checkBox, BorderLayout.WEST);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            Component component = renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
                    hasFocus);

            if (value instanceof CategoryNode) {
                if (defaultComponent != null) {
                    remove(defaultComponent);
                }
                defaultComponent = component;
                add(component, BorderLayout.CENTER);
                CategoryNode node = (CategoryNode) value;
                checkBox.setSelected(node.isEnabled());
                return this;
            }

            if (value instanceof RuleNode) {
                if (defaultComponent != null) {
                    remove(defaultComponent);
                }
                defaultComponent = component;
                add(component, BorderLayout.CENTER);
                RuleNode node = (RuleNode) value;
                checkBox.setSelected(node.isEnabled());
                return this;
            }

            return component;
        }
    }
}
