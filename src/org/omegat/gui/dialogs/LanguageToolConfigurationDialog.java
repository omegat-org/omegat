/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Lev Abashkin
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
package org.omegat.gui.dialogs;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
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
import org.omegat.languagetools.LanguageToolPrefs;
import org.omegat.languagetools.LanguageToolWrapper;
import org.omegat.languagetools.LanguageToolWrapper.BridgeType;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.StaticUIUtils;
import org.xml.sax.SAXException;

/**
 * Helper class to work with manually defined rules unknown to built-in
 * LanguageTool
 *
 * @author Lev Abashkin
 */
class ExternalRule extends Rule {

    public final static String CATEGORY_ID = "EXTERNAL";
    public final static Category DEFAULT_CATEGORY = new Category(new CategoryId(CATEGORY_ID),
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
class TreeListener implements KeyListener, MouseListener {

    static void install(JTree tree) {
        TreeListener listener = new TreeListener(tree);
        tree.addMouseListener(listener);
        tree.addKeyListener(listener);
    }

    private static final Dimension CHECKBOX_DIMENSION = new JCheckBox().getPreferredSize();

    private final JTree tree;

    private TreeListener(JTree tree) {
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
class RuleNode extends DefaultMutableTreeNode {

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
class CategoryNode extends DefaultMutableTreeNode {

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
class CheckBoxTreeCellRenderer extends JPanel implements TreeCellRenderer {

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

/**
 * @author Panagiotis Minos
 * @author Lev Abashkin
 */
@SuppressWarnings("serial")
public class LanguageToolConfigurationDialog extends javax.swing.JDialog {

    private final JFileChooser fileChooser = new JFileChooser();
    private BridgeType selectedBridgeType;
    private Set<String> disabledCategories, disabledRuleIds, enabledRuleIds;
    private final static String NEW_RULE_PATTERN = "^[A-Za-z_\\.]+$";
    private String targetLanguageCode;

    public LanguageToolConfigurationDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initCommon();
        loadPreferences();
        disableRulesUI(OStrings.getString("GUI_LANGUAGETOOL_RULES_UNAVAILABLE_NO_PROJECT"));
    }

    /**
     * Creates new form LanguageToolConfigurationDialog
     */
    public LanguageToolConfigurationDialog(java.awt.Frame parent, boolean modal, Language sourceLang,
            Language targetLang) {
        super(parent, modal);
        initCommon();

        rulesMessagePanel.setVisible(false);

        targetLanguageCode = targetLang.getLanguageCode();
        loadPreferences();

        // Load rule tree
        Optional<org.languagetool.Language> targetLtLang = getLTLanguage(targetLang);
        Optional<org.languagetool.Language> sourceLtLang = getLTLanguage(sourceLang);

        if (!targetLtLang.isPresent()) {
            disableRulesUI(OStrings.getString("GUI_LANGUAGETOOL_RULES_UNAVAILABLE"));
            return;
        }

        JLanguageTool ltInstance;
        try {
            ltInstance = new JLanguageTool(targetLtLang.get());
        } catch (Throwable e) {
            // Disable tree and return if instance couldn't be gotten
            disableRulesUI(OStrings.getString("GUI_LANGUAGETOOL_RULES_UNAVAILABLE_ERROR"));
            Log.log(e);
            return;
        }
        
        rulesTree.addTreeSelectionListener(e -> updateButtonState());

        List<Rule> rules = ltInstance.getAllRules();
        sourceLtLang.ifPresent(srcLtLang -> {
            try {
                rules.addAll(Tools.getBitextRules(srcLtLang, targetLtLang.get()));
            } catch (IOException | ParserConfigurationException | SAXException e) {
                Log.log(e);
            }
        });

        // Collect internal rule IDs
        List<String> internalRuleIds = rules.stream().map(Rule::getId).collect(Collectors.toList());

        // Remove rule IDs of rules disabled by default from set of disabled rules
        rules.stream().filter(Rule::isDefaultOff).map(Rule::getId).forEach(disabledRuleIds::remove);

        // Create ExternalRule instances for rules not found in built-in LT
        // and add them to our rules list
        List<String> externalRuleIds = new ArrayList<>(disabledRuleIds);
        externalRuleIds.addAll(enabledRuleIds);
        externalRuleIds.removeAll(internalRuleIds);
        externalRuleIds.stream().distinct().map(ExternalRule::new).forEach(rules::add);

        DefaultMutableTreeNode rootNode = createTree(rules);
        rulesTree.setModel(getTreeModel(rootNode));
        rulesTree.applyComponentOrientation(ComponentOrientation.getOrientation(targetLtLang.get().getLocale()));
        rulesTree.setRootVisible(false);
        rulesTree.setEditable(false);
        rulesTree.setCellRenderer(new CheckBoxTreeCellRenderer());
        TreeListener.install(rulesTree);

        updateButtonState();
    }

    final private void initCommon() {
        StaticUIUtils.setEscapeClosable(this);
        initComponents();
        getRootPane().setDefaultButton(okButton);
        bridgeNativeRadioButton.setText(
                StringUtil.format(OStrings.getString("GUI_LANGUAGETOOL_NATIVE_BRIDGE"), JLanguageTool.VERSION));
        setMinimumSize(new Dimension(500, 350));
        setLocationRelativeTo(getParent());
    }

    final private void disableRulesUI(String message) {
        rulesMessagePanel.setVisible(true);
        rulesMessageLabel.setText(message);
        rulesScrollPane.setVisible(false);
        rulesButtonsPanel.setVisible(false);
        pack();
        setLocationRelativeTo(getParent());
    }

    void updateButtonState() {
        TreePath[] selected = rulesTree.getSelectionPaths();
        boolean deletable = selected != null
                && Stream.of(selected).map(TreePath::getLastPathComponent)
                        .allMatch(LanguageToolConfigurationDialog::isExternalRuleNode);
        deleteRuleButton.setEnabled(deletable);
    }

    static boolean isExternalRuleNode(Object node) {
        return node instanceof RuleNode && ((RuleNode) node).getRule() instanceof ExternalRule;
    }

    private void doClose() {
        setVisible(false);
        dispose();
    }

    private void handleBridgeTypeChange(BridgeType type) {
        selectedBridgeType = type;
        switch (type) {
        case NATIVE:
            localServerJarPathTextField.setEnabled(false);
            directoryChooseButton.setEnabled(false);
            urlTextField.setEnabled(false);
            break;
        case REMOTE_URL:
            localServerJarPathTextField.setEnabled(false);
            directoryChooseButton.setEnabled(false);
            urlTextField.setEnabled(true);
            break;
        case LOCAL_INSTALLATION:
            localServerJarPathTextField.setEnabled(true);
            directoryChooseButton.setEnabled(true);
            urlTextField.setEnabled(false);
            break;
        }
    }

    private void loadPreferences() {
        BridgeType type = LanguageToolPrefs.getBridgeType();
        switch (type) {
        case NATIVE:
            bridgeNativeRadioButton.setSelected(true);
            break;
        case REMOTE_URL:
            bridgeRemoteRadioButton.setSelected(true);
            break;
        case LOCAL_INSTALLATION:
            bridgeLocalRadioButton.setSelected(true);
            break;
        }

        urlTextField.setText(LanguageToolPrefs.getRemoteUrl());
        localServerJarPathTextField.setText(LanguageToolPrefs.getLocalServerJarPath());

        if (targetLanguageCode != null) {
            disabledCategories = LanguageToolPrefs.getDisabledCategories(targetLanguageCode);
            disabledRuleIds = LanguageToolPrefs.getDisabledRules(targetLanguageCode);
            enabledRuleIds = LanguageToolPrefs.getEnabledRules(targetLanguageCode);
        }

        handleBridgeTypeChange(type);
    }

    private void savePreferences() {
        LanguageToolPrefs.setBridgeType(selectedBridgeType);
        LanguageToolPrefs.setRemoteUrl(urlTextField.getText());
        LanguageToolPrefs.setLocalServerJarPath(localServerJarPathTextField.getText());

        if (targetLanguageCode != null) {
            LanguageToolPrefs.setDisabledCategories(disabledCategories, targetLanguageCode);
            LanguageToolPrefs.setDisabledRules(disabledRuleIds, targetLanguageCode);
            LanguageToolPrefs.setEnabledRules(enabledRuleIds, targetLanguageCode);
            LanguageToolWrapper.setBridgeFromCurrentProject();
        }
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        centerPanel = new javax.swing.JPanel();
        externalOptionsPanel = new javax.swing.JPanel();
        nativePanel = new javax.swing.JPanel();
        bridgeNativeRadioButton = new javax.swing.JRadioButton();
        remotePanel = new javax.swing.JPanel();
        bridgeRemoteRadioButton = new javax.swing.JRadioButton();
        urlPanel = new javax.swing.JPanel();
        urlLabel = new javax.swing.JLabel();
        urlTextField = new javax.swing.JTextField();
        localPanel = new javax.swing.JPanel();
        bridgeLocalRadioButton = new javax.swing.JRadioButton();
        directoryPanel = new javax.swing.JPanel();
        localPathLabel = new javax.swing.JLabel();
        localServerJarPathTextField = new javax.swing.JTextField();
        directoryChooseButton = new javax.swing.JButton();
        rulesPanel = new javax.swing.JPanel();
        rulesMessagePanel = new javax.swing.JPanel();
        rulesMessageLabel = new javax.swing.JLabel();
        rulesScrollPane = new javax.swing.JScrollPane();
        rulesTree = new javax.swing.JTree();
        rulesButtonsPanel = new javax.swing.JPanel();
        addRuleButton = new javax.swing.JButton();
        deleteRuleButton = new javax.swing.JButton();
        bottomPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(OStrings.getString("GUI_LANGUAGETOOL_DIALOG_TITLE")); // NOI18N

        centerPanel.setLayout(new java.awt.BorderLayout());

        externalOptionsPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5), javax.swing.BorderFactory.createTitledBorder(OStrings.getString("GUI_LANGUAGETOOL_BRIDGE_TYPE")))); // NOI18N
        externalOptionsPanel.setLayout(new javax.swing.BoxLayout(externalOptionsPanel, javax.swing.BoxLayout.PAGE_AXIS));

        nativePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        nativePanel.setLayout(new java.awt.BorderLayout());

        buttonGroup1.add(bridgeNativeRadioButton);
        bridgeNativeRadioButton.setText(OStrings.getString("GUI_LANGUAGETOOL_NATIVE_BRIDGE")); // NOI18N
        bridgeNativeRadioButton.setName(""); // NOI18N
        bridgeNativeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bridgeNativeRadioButtonActionPerformed(evt);
            }
        });
        nativePanel.add(bridgeNativeRadioButton, java.awt.BorderLayout.CENTER);

        externalOptionsPanel.add(nativePanel);

        remotePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        remotePanel.setLayout(new java.awt.BorderLayout());

        buttonGroup1.add(bridgeRemoteRadioButton);
        bridgeRemoteRadioButton.setText(OStrings.getString("GUI_LANGUAGETOOL_REMOTE_BRIDGE")); // NOI18N
        bridgeRemoteRadioButton.setName(""); // NOI18N
        bridgeRemoteRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bridgeRemoteRadioButtonActionPerformed(evt);
            }
        });
        remotePanel.add(bridgeRemoteRadioButton, java.awt.BorderLayout.NORTH);

        urlPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 25, 0, 0));
        urlPanel.setLayout(new java.awt.BorderLayout());

        urlLabel.setText(OStrings.getString("GUI_LANGUAGETOOL_URL")); // NOI18N
        urlPanel.add(urlLabel, java.awt.BorderLayout.NORTH);

        urlTextField.setToolTipText("");
        urlPanel.add(urlTextField, java.awt.BorderLayout.CENTER);

        remotePanel.add(urlPanel, java.awt.BorderLayout.CENTER);

        externalOptionsPanel.add(remotePanel);

        localPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 10, 10));
        localPanel.setLayout(new java.awt.BorderLayout());

        buttonGroup1.add(bridgeLocalRadioButton);
        bridgeLocalRadioButton.setText(OStrings.getString("GUI_LANGUAGETOOL_LOCAL_BRIDGE")); // NOI18N
        bridgeLocalRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bridgeLocalRadioButtonActionPerformed(evt);
            }
        });
        localPanel.add(bridgeLocalRadioButton, java.awt.BorderLayout.NORTH);

        directoryPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 25, 0, 0));
        directoryPanel.setLayout(new java.awt.BorderLayout());

        localPathLabel.setText(OStrings.getString("GUI_LANGUAGETOOL_LOCAL_SERVER_PATH")); // NOI18N
        directoryPanel.add(localPathLabel, java.awt.BorderLayout.NORTH);

        localServerJarPathTextField.setToolTipText("");
        directoryPanel.add(localServerJarPathTextField, java.awt.BorderLayout.CENTER);

        directoryChooseButton.setText(OStrings.getString("GUI_LANGUAGETOOL_CHOOSE_BUTTON")); // NOI18N
        directoryChooseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryChooseButtonActionPerformed(evt);
            }
        });
        directoryPanel.add(directoryChooseButton, java.awt.BorderLayout.EAST);

        localPanel.add(directoryPanel, java.awt.BorderLayout.CENTER);

        externalOptionsPanel.add(localPanel);

        centerPanel.add(externalOptionsPanel, java.awt.BorderLayout.NORTH);

        rulesPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 5, 5), javax.swing.BorderFactory.createTitledBorder(OStrings.getString("GUI_LANGUAGETOOL_RULES")))); // NOI18N
        rulesPanel.setLayout(new java.awt.BorderLayout());

        rulesMessagePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        rulesMessagePanel.setLayout(new java.awt.BorderLayout());

        rulesMessageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rulesMessagePanel.add(rulesMessageLabel, java.awt.BorderLayout.CENTER);

        rulesPanel.add(rulesMessagePanel, java.awt.BorderLayout.NORTH);

        rulesTree.setShowsRootHandles(true);
        rulesScrollPane.setViewportView(rulesTree);

        rulesPanel.add(rulesScrollPane, java.awt.BorderLayout.CENTER);

        rulesButtonsPanel.setLayout(new javax.swing.BoxLayout(rulesButtonsPanel, javax.swing.BoxLayout.LINE_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(addRuleButton, OStrings.getString("BUTTON_ADD_NODOTS")); // NOI18N
        addRuleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRuleButtonActionPerformed(evt);
            }
        });
        rulesButtonsPanel.add(addRuleButton);

        org.openide.awt.Mnemonics.setLocalizedText(deleteRuleButton, OStrings.getString("BUTTON_REMOVE")); // NOI18N
        deleteRuleButton.setToolTipText("");
        deleteRuleButton.setEnabled(false);
        deleteRuleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteRuleButtonActionPerformed(evt);
            }
        });
        rulesButtonsPanel.add(deleteRuleButton);

        rulesPanel.add(rulesButtonsPanel, java.awt.BorderLayout.SOUTH);

        centerPanel.add(rulesPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        bottomPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.setLayout(new java.awt.BorderLayout());

        buttonsPanel.setLayout(new javax.swing.BoxLayout(buttonsPanel, javax.swing.BoxLayout.LINE_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(cancelButton);

        org.openide.awt.Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(okButton);

        bottomPanel.add(buttonsPanel, java.awt.BorderLayout.EAST);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bridgeNativeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bridgeNativeRadioButtonActionPerformed
        handleBridgeTypeChange(BridgeType.NATIVE);
    }//GEN-LAST:event_bridgeNativeRadioButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        savePreferences();
        doClose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void directoryChooseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directoryChooseButtonActionPerformed
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileFilter filter = new FileNameExtensionFilter(OStrings.getString("GUI_LANGUAGETOOL_JAR_FILE_FILTER"), "jar");
        fileChooser.setFileFilter(filter);
        fileChooser.setDialogTitle(OStrings.getString("GUI_LANGUAGETOOL_FILE_CHOOSER_TITLE"));
        int result = fileChooser.showOpenDialog(LanguageToolConfigurationDialog.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            localServerJarPathTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_directoryChooseButtonActionPerformed

    private void bridgeRemoteRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bridgeRemoteRadioButtonActionPerformed
        handleBridgeTypeChange(BridgeType.REMOTE_URL);
    }//GEN-LAST:event_bridgeRemoteRadioButtonActionPerformed

    private void bridgeLocalRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bridgeLocalRadioButtonActionPerformed
        handleBridgeTypeChange(BridgeType.LOCAL_INSTALLATION);
    }//GEN-LAST:event_bridgeLocalRadioButtonActionPerformed

    private void addRuleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRuleButtonActionPerformed
        String newRuleId = (String) JOptionPane.showInputDialog(this,
                OStrings.getString("GUI_LANGUAGETOOL_ADD_RULE_MESSAGE"),
                OStrings.getString("GUI_LANGUAGETOOL_ADD_RULE_DIALOG_TITLE"), JOptionPane.PLAIN_MESSAGE, null, null,
                null);

        if (newRuleId == null) {
            return; // Nothing to do
        }

        if (!Pattern.matches(NEW_RULE_PATTERN, newRuleId)) {
            JOptionPane.showMessageDialog(this, OStrings.getString("GUI_LANGUAGETOOL_BAD_RULE_ID"));
            return;
        }

        DefaultTreeModel model = (DefaultTreeModel) rulesTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) rulesTree.getModel().getRoot();
        DefaultMutableTreeNode externalCategoryNode = null;
        // Check if external category node already exists (should be last child)
        CategoryNode lastCategoryNode = (CategoryNode) root.getLastChild();
        if (lastCategoryNode.getCategory().getId().toString().equals(ExternalRule.CATEGORY_ID)) {
            // Check if rule already exists
            for (int i = 0; i < lastCategoryNode.getChildCount(); i++) {
                if (((RuleNode) lastCategoryNode.getChildAt(i)).getRule().getId().equals(newRuleId)) {
                    JOptionPane.showMessageDialog(this, OStrings.getString("GUI_LANGUAGETOOL_RULE_ALREADY_EXISTS"));
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
        rulesTree.scrollPathToVisible(new TreePath(newRuleNode.getPath()));

    }//GEN-LAST:event_addRuleButtonActionPerformed

    private void deleteRuleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteRuleButtonActionPerformed
        TreePath[] currentSelections = rulesTree.getSelectionPaths();
        if (currentSelections != null) {
            DefaultTreeModel model = (DefaultTreeModel) rulesTree.getModel();
            Stream.of(currentSelections).map(TreePath::getLastPathComponent)
                    .filter(LanguageToolConfigurationDialog::isExternalRuleNode)
                    .forEach(node -> {
                        MutableTreeNode currentNode = (MutableTreeNode) node;
                        MutableTreeNode parent = (MutableTreeNode) currentNode.getParent();
                        model.removeNodeFromParent(currentNode);
                        if (parent.getChildCount() == 0) {
                            model.removeNodeFromParent(parent);
                        }
                    });
        }
    }//GEN-LAST:event_deleteRuleButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addRuleButton;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JRadioButton bridgeLocalRadioButton;
    private javax.swing.JRadioButton bridgeNativeRadioButton;
    private javax.swing.JRadioButton bridgeRemoteRadioButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JButton deleteRuleButton;
    private javax.swing.JButton directoryChooseButton;
    private javax.swing.JPanel directoryPanel;
    private javax.swing.JPanel externalOptionsPanel;
    private javax.swing.JPanel localPanel;
    private javax.swing.JLabel localPathLabel;
    private javax.swing.JTextField localServerJarPathTextField;
    private javax.swing.JPanel nativePanel;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel remotePanel;
    private javax.swing.JPanel rulesButtonsPanel;
    private javax.swing.JLabel rulesMessageLabel;
    private javax.swing.JPanel rulesMessagePanel;
    private javax.swing.JPanel rulesPanel;
    private javax.swing.JScrollPane rulesScrollPane;
    private javax.swing.JTree rulesTree;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JPanel urlPanel;
    private javax.swing.JTextField urlTextField;
    // End of variables declaration//GEN-END:variables
}
