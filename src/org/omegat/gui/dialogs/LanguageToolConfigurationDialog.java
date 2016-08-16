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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.xml.parsers.ParserConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.Tools;
import org.omegat.core.Core;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.StaticUIUtils;

import org.omegat.languagetools.LanguageToolWrapper.BridgeType;
import static org.omegat.languagetools.LanguageToolNativeBridge.getLTLanguage;
import org.xml.sax.SAXException;


/**
 * Helper class to work with manually defined rules
 * unknown to built-in LanguageTool
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
    }

    @Override
    public void mousePressed(MouseEvent e) {
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
class CheckBoxTreeCellRenderer extends JPanel implements TreeCellRenderer {

    private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    private final JCheckBox checkBox = new JCheckBox();

    private Component defaultComponent;

    CheckBoxTreeCellRenderer() {
        setLayout(new BorderLayout());
        setOpaque(false);
        checkBox.setOpaque(false);
        renderer.setLeafIcon(null);
        add(checkBox, BorderLayout.WEST);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component component = renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

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
public class LanguageToolConfigurationDialog extends javax.swing.JDialog {

    private final JFileChooser fileChooser = new JFileChooser();
    private BridgeType selectedBridgeType;
    private Set<String> disabledCategories, disabledRuleIds, enabledRuleIds;
    private final static String NEW_RULE_PATTERN = "^[A-Za-z_\\.]+$";

    /**
     * Creates new form LanguageToolConfigurationDialog
     */
    public LanguageToolConfigurationDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        StaticUIUtils.setEscapeClosable(this);
        initComponents();
        getRootPane().setDefaultButton(okButton);
        setLocationRelativeTo(parent);
        loadPreferences();

        // Load rule tree
        Optional<Language> targetLang = getLTLanguage(Core.getProject().getProjectProperties().getTargetLanguage());
        Optional<Language> sourceLang = getLTLanguage(Core.getProject().getProjectProperties().getSourceLanguage());

        // Disable tree and return if targetLang wasn't found
        if (!targetLang.isPresent()) {
            ((DefaultTreeModel) rulesTree.getModel()).setRoot(null);
            rulesTree.setEnabled(false);
            return;
        }

        JLanguageTool ltInstance = new JLanguageTool(targetLang.get());
        List<Rule> rules = ltInstance.getAllRules();
        if (sourceLang.isPresent()) {
            try {
                rules.addAll(Tools.getBitextRules(sourceLang.get(), targetLang.get()));
            } catch (IOException | ParserConfigurationException | SAXException e) {
                // Do nothing
            }
        }
        // Collect internal rule IDs
        List<String> internalRuleIds = rules.stream().map((p) -> p.getId()).
                collect(Collectors.toList());
        // Create ExternalRule instances for rules not found in built-in LT
        // and add them to our rules list
        List<String> externalRuleIds = new ArrayList<>(disabledRuleIds);
        externalRuleIds.addAll(enabledRuleIds);
        rules.addAll(externalRuleIds.stream().
                distinct().
                filter((p) -> !internalRuleIds.contains(p)).
                map((p) -> new ExternalRule(p)).
                collect(Collectors.toList()));

        DefaultMutableTreeNode rootNode = createTree(rules);
        rulesTree.setModel(getTreeModel(rootNode));
        rulesTree.applyComponentOrientation(ComponentOrientation.getOrientation(targetLang.get().getLocale()));
        rulesTree.setRootVisible(false);
        rulesTree.setEditable(false);
        rulesTree.setCellRenderer(new CheckBoxTreeCellRenderer());
        TreeListener.install(rulesTree);
    }

    private void doClose() {
        setVisible(false);
        dispose();
    }

    private void handleBridgeTypeChange(BridgeType type) {
        selectedBridgeType = type;
        switch (type) {
            case NATIVE:
                directoryTextField.setEnabled(false);
                directoryChooseButton.setEnabled(false);
                urlTextField.setEnabled(false);
                break;
            case REMOTE_URL:
                directoryTextField.setEnabled(false);
                directoryChooseButton.setEnabled(false);
                urlTextField.setEnabled(true);
                break;
            case LOCAL_INSTALLATION:
                directoryTextField.setEnabled(true);
                directoryChooseButton.setEnabled(true);
                urlTextField.setEnabled(false);
                break;
        }
    }

    private void loadPreferences() {
        BridgeType type = Preferences.getPreferenceEnumDefault(Preferences.LANGUAGETOOL_BRIDGE_TYPE,
                BridgeType.NATIVE);
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

        urlTextField.setText(Preferences.getPreference(Preferences.LANGUAGETOOL_REMOTE_URL));
        directoryTextField.setText(Preferences.getPreference(Preferences.LANGUAGETOOL_LOCAL_SERVER_JAR_PATH));



        String targetLanguageCode = Core.getProject().getProjectProperties().getTargetLanguage().getLanguageCode();
        disabledCategories = Arrays.asList(Preferences.getPreference(Preferences.LANGUAGETOOL_DISABLED_CATEGORIES).
                                split(",")).stream().filter(s -> !s.equals("")).collect(Collectors.toSet());
        disabledRuleIds = Arrays.asList(Preferences.getPreference(Preferences.LANGUAGETOOL_DISABLED_RULES_PREFIX + "_" + targetLanguageCode).
                                split(",")).stream().filter(s -> !s.equals("")).collect(Collectors.toSet());
        enabledRuleIds = Arrays.asList(Preferences.getPreference(Preferences.LANGUAGETOOL_ENABLED_RULES_PREFIX + "_" + targetLanguageCode).
                                split(",")).stream().filter(s -> !s.equals("")).collect(Collectors.toSet());

        handleBridgeTypeChange(type);
    }

    private void savePreferences() {
        Preferences.setPreference(Preferences.LANGUAGETOOL_REMOTE_URL, urlTextField.getText());
        Preferences.setPreference(Preferences.LANGUAGETOOL_LOCAL_SERVER_JAR_PATH, directoryTextField.getText());

        String targetLanguageCode = Core.getProject().getProjectProperties().getTargetLanguage().getLanguageCode();
        Preferences.setPreference(Preferences.LANGUAGETOOL_DISABLED_CATEGORIES,
                disabledCategories.stream().reduce((t, u) -> t + "," + u).orElse(""));
        Preferences.setPreference(Preferences.LANGUAGETOOL_ENABLED_RULES_PREFIX + "_" + targetLanguageCode,
                enabledRuleIds.stream().reduce((t, u) -> t + "," + u).orElse(""));
        Preferences.setPreference(Preferences.LANGUAGETOOL_DISABLED_RULES_PREFIX + "_" + targetLanguageCode,
                disabledRuleIds.stream().reduce((t, u) -> t + "," + u).orElse(""));

        Preferences.setPreference(Preferences.LANGUAGETOOL_BRIDGE_TYPE, selectedBridgeType);
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
                parents.get(rule.getCategory().getId().toString()).add(ruleNode);
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
        if (rule.isDefaultOff() && rule.getCategory().isDefaultOff()
                && enabledRuleIds.contains(rule.getId())) {
            disabledCategories.remove(rule.getCategory().getId().toString());
        }
        return ret;
    }

    @NotNull
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

                    if (o.getRule().isDefaultOff()) {
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
                    if (disabledCategories.contains(o.getRule().getCategory().getId().toString()) &&
                            !o.getRule().getCategory().getId().toString().equals(ExternalRule.CATEGORY_ID)) {
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
            public void treeStructureChanged(TreeModelEvent e) {}
        });
        return treeModel;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        typePanel = new javax.swing.JPanel();
        bridgeNativeRadioButton = new javax.swing.JRadioButton();
        bridgeRemoteRadioButton = new javax.swing.JRadioButton();
        bridgeLocalRadioButton = new javax.swing.JRadioButton();
        externalOptionsPanel = new javax.swing.JPanel();
        directoryTextField = new javax.swing.JTextField();
        localPathLabel = new javax.swing.JLabel();
        urlLabel = new javax.swing.JLabel();
        directoryChooseButton = new javax.swing.JButton();
        urlTextField = new javax.swing.JTextField();
        bottomPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        rulesPanel = new javax.swing.JPanel();
        rulesScrollPane = new javax.swing.JScrollPane();
        rulesTree = new javax.swing.JTree();
        addRuleButton = new javax.swing.JButton();
        deleteRuleButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(OStrings.getString("GUI_LANGUAGETOOL_DIALOG_TITLE")); // NOI18N
        setResizable(false);

        typePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(OStrings.getString("GUI_LANGUAGETOOL_BRIDGE_TYPE"))); // NOI18N

        buttonGroup1.add(bridgeNativeRadioButton);
        bridgeNativeRadioButton.setSelected(true);
        bridgeNativeRadioButton.setText(OStrings.getString("GUI_LANGUAGETOOL_NATIVE_BRIDGE")); // NOI18N
        bridgeNativeRadioButton.setName(""); // NOI18N
        bridgeNativeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bridgeNativeRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(bridgeRemoteRadioButton);
        bridgeRemoteRadioButton.setText(OStrings.getString("GUI_LANGUAGETOOL_REMOTE_BRIDGE")); // NOI18N
        bridgeRemoteRadioButton.setName(""); // NOI18N
        bridgeRemoteRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bridgeRemoteRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(bridgeLocalRadioButton);
        bridgeLocalRadioButton.setText(OStrings.getString("GUI_LANGUAGETOOL_LOCAL_BRIDGE")); // NOI18N
        bridgeLocalRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bridgeLocalRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout typePanelLayout = new javax.swing.GroupLayout(typePanel);
        typePanel.setLayout(typePanelLayout);
        typePanelLayout.setHorizontalGroup(
            typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bridgeNativeRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bridgeRemoteRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bridgeLocalRadioButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        typePanelLayout.setVerticalGroup(
            typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bridgeNativeRadioButton)
                    .addComponent(bridgeRemoteRadioButton)
                    .addComponent(bridgeLocalRadioButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        externalOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(OStrings.getString("GUI_LANGUAGETOOL_EXTERNAL_SETTINGS"))); // NOI18N

        directoryTextField.setToolTipText("");

        localPathLabel.setText(OStrings.getString("GUI_LANGUAGETOOL_LOCAL_SERVER_PATH")); // NOI18N

        urlLabel.setText(OStrings.getString("GUI_LANGUAGETOOL_URL")); // NOI18N

        directoryChooseButton.setText(OStrings.getString("GUI_LANGUAGETOOL_CHOOSE_BUTTON")); // NOI18N
        directoryChooseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryChooseButtonActionPerformed(evt);
            }
        });

        urlTextField.setToolTipText("");

        javax.swing.GroupLayout externalOptionsPanelLayout = new javax.swing.GroupLayout(externalOptionsPanel);
        externalOptionsPanel.setLayout(externalOptionsPanelLayout);
        externalOptionsPanelLayout.setHorizontalGroup(
            externalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(externalOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(externalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, externalOptionsPanelLayout.createSequentialGroup()
                        .addComponent(directoryTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(directoryChooseButton))
                    .addComponent(urlTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(externalOptionsPanelLayout.createSequentialGroup()
                        .addGroup(externalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(urlLabel)
                            .addComponent(localPathLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        externalOptionsPanelLayout.setVerticalGroup(
            externalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(externalOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(urlLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(urlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(localPathLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(externalOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(directoryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(directoryChooseButton))
                .addGap(40, 40, 40))
        );

        bottomPanel.setPreferredSize(new java.awt.Dimension(631, 40));

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bottomPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(okButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addContainerGap())
        );

        bottomPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPanelLayout.createSequentialGroup()
                .addContainerGap(15, Short.MAX_VALUE)
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton)))
        );

        rulesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(OStrings.getString("GUI_LANGUAGETOOL_RULES"))); // NOI18N

        rulesScrollPane.setViewportView(rulesTree);

        org.openide.awt.Mnemonics.setLocalizedText(addRuleButton, OStrings.getString("BUTTON_ADD_NODOTS")); // NOI18N
        addRuleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRuleButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(deleteRuleButton, OStrings.getString("BUTTON_REMOVE")); // NOI18N
        deleteRuleButton.setToolTipText("");
        deleteRuleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteRuleButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout rulesPanelLayout = new javax.swing.GroupLayout(rulesPanel);
        rulesPanel.setLayout(rulesPanelLayout);
        rulesPanelLayout.setHorizontalGroup(
            rulesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rulesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE)
            .addGroup(rulesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addRuleButton)
                .addGap(18, 18, 18)
                .addComponent(deleteRuleButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        rulesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addRuleButton, deleteRuleButton});

        rulesPanelLayout.setVerticalGroup(
            rulesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rulesPanelLayout.createSequentialGroup()
                .addComponent(rulesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rulesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addRuleButton)
                    .addComponent(deleteRuleButton)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(typePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(externalOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(bottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 608, Short.MAX_VALUE)
            .addComponent(rulesPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(typePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(externalOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rulesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

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
            directoryTextField.setText(file.getAbsolutePath());
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
                    OStrings.getString("GUI_LANGUAGETOOL_ADD_RULE_DIALOG_TITLE"),
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
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
        TreePath currentSelection = rulesTree.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                         (currentSelection.getLastPathComponent());
            if (currentNode instanceof RuleNode && ((RuleNode) currentNode).getRule() instanceof ExternalRule) {
                DefaultTreeModel model = (DefaultTreeModel) rulesTree.getModel();
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) currentNode.getParent();
                model.removeNodeFromParent(currentNode);
                if (parent.getChildCount() == 0) {
                    model.removeNodeFromParent(parent);
                }
            }
        }
    }//GEN-LAST:event_deleteRuleButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LanguageToolConfigurationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LanguageToolConfigurationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LanguageToolConfigurationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LanguageToolConfigurationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                LanguageToolConfigurationDialog dialog = new LanguageToolConfigurationDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addRuleButton;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JRadioButton bridgeLocalRadioButton;
    private javax.swing.JRadioButton bridgeNativeRadioButton;
    private javax.swing.JRadioButton bridgeRemoteRadioButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton deleteRuleButton;
    private javax.swing.JButton directoryChooseButton;
    private javax.swing.JTextField directoryTextField;
    private javax.swing.JPanel externalOptionsPanel;
    private javax.swing.JLabel localPathLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel rulesPanel;
    private javax.swing.JScrollPane rulesScrollPane;
    private javax.swing.JTree rulesTree;
    private javax.swing.JPanel typePanel;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JTextField urlTextField;
    // End of variables declaration//GEN-END:variables
}
