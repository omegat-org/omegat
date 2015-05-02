/* :name=SVN - Clear authentication data :description=Clear authentication data for SVN repository
 * 
 *  Clear authentication data for SVN repository.
 *  This feature is only available with simple password credential kind ("svn.simple").
 *
 * @author  Yu Tang
 * @date    2015-05-02
 * @version 0.1
 */
import groovy.swing.SwingBuilder
import java.awt.BorderLayout as BL
import java.awt.Component
import javax.swing.Box
import javax.swing.JComponent
import javax.swing.JOptionPane
import javax.swing.WindowConstants as WC

import org.apache.commons.lang.StringEscapeUtils as SEU

import org.omegat.core.team.SVNRemoteRepository
import org.omegat.gui.scripting.ScriptingWindow
import org.omegat.util.gui.StaticUIUtils
import org.omegat.util.StaticUtils

import static org.tmatesoft.svn.core.auth.ISVNAuthenticationManager.PASSWORD
import org.tmatesoft.svn.core.wc.SVNWCUtil

// helper methods
def owner = {
    def win = ScriptingWindow.window
    win.isFocused() ? win : mainWindow
}()

def echoIfRunFromShortcut = {
    owner.is(mainWindow) ? { mainWindow.showMessageDialog(it); it } : { it }
}()

def _ = { key ->
    res.getString(key)
}

def isSvnTeamProjectOpened = {
    def repository = project.getRepository()
    repository && (repository instanceof SVNRemoteRepository)
}()

// get authSvnSimpleDirectory
def authSvnSimpleDirectory = {
    File configDir = SVNWCUtil.defaultConfigurationDirectory
    File authDirectory = new File(configDir, 'auth')
    new File(authDirectory, PASSWORD)
}()

// No auth folder
if (!authSvnSimpleDirectory.isDirectory()) {
    return echoIfRunFromShortcut( StaticUtils.format(_('err_auth_dir_not_found'), authSvnSimpleDirectory) )
}

// get SVNConfigFile
Map map = [:]
authSvnSimpleDirectory.eachFile(groovy.io.FileType.FILES) { file ->
    def content = file.getText('UTF-8')
    def realm = content.find(/\nsvn:realmstring\nV \d+\n(.+)\n/) {g0, g1 -> g1}
    if (realm) {
        def username = content.find(/\nusername\nV \d+\n(.+)\n/) {g0, g1 -> g1}
        def escapedRealm = SEU.escapeHtml(realm)
        def escapedUserName = SEU.escapeHtml(username ?: '')
        def title = StaticUtils.format(_('item_html'), escapedRealm, escapedUserName)
        map[title] = file
    }
}

// No auth files
if (!map) {
    return echoIfRunFromShortcut( StaticUtils.format(_('err_auth_file_not_found'), authSvnSimpleDirectory) )
}

// Initializing SwingBuilder
swing = new SwingBuilder()

// Create list
Box list = Box.createVerticalBox()
def updateDeleteButtonEnabled = {
    deleteButton.enabled = list.components.any{ it.isSelected() }
}
map.each {k, v ->
    list.add swing.checkBox(text: k,
                            selected: false,
                            alignmentX: Component.LEFT_ALIGNMENT,
                            actionPerformed: updateDeleteButtonEnabled,
                            toolTipText: v.path)
}

// helper methods
def getSelectedTitles = {
    list.components.findAll{ it.isSelected() }.collect { it.text }
}

def showConfirmation = {
    def message = _('delete_confirmation')
    def title = _('title')
    def optionType = JOptionPane.YES_NO_OPTION
    def messageType = JOptionPane.QUESTION_MESSAGE
    JOptionPane.showConfirmDialog(dialog, message, title, optionType, messageType)
}

def deleteCredentials = {
    def selectedTitles = getSelectedTitles()
    if (showConfirmation() != JOptionPane.YES_OPTION) {
        return
    }

    selectedTitles.each { title ->
        def file = map[title]
        try {
            if (!file.delete()) {
                console.println echoIfRunFromShortcut( StaticUtils.format(_('err_file_could_not_delete'), file.path) )
                return
            }
            map.remove title
            def comp = list.components.find{ it.text == title}
            list.remove comp
            dialog.pack()
            updateDeleteButtonEnabled()
            if (!map) {
                dialog.dispose()
            }
        } catch(e) {
            console.println echoIfRunFromShortcut( StaticUtils.format(_('err_failed_to_delete'), file.path, e) )
        }
    }
}

// create dialog with list as widget
javax.swing.JButton.metaClass.setText = { text ->
    org.openide.awt.Mnemonics.setLocalizedText delegate, text
}

dialog = swing.dialog(owner: owner,
                      modal: true,
                      title: _('title'),
                      layout: new BL(),
                      defaultCloseOperation: WC.DISPOSE_ON_CLOSE) {
    panel(constraints: BL.NORTH) {
        label(text: isSvnTeamProjectOpened ? _('label_description_with_caution') : _('label_description') )
    }
    panel(constraints: BL.CENTER) {
        scrollPane {
            widget(list)
        }
    }
    panel(constraints: BL.SOUTH) {
        deleteButton = button(text: _('button_delete'),
                              actionPerformed: deleteCredentials,
                              enabled: false)
        button(text: _('button_close') ,
               actionPerformed: { dispose() })
    }
}

// Show dialog
StaticUIUtils.setEscapeClosable dialog
dialog.pack()
dialog.setLocationRelativeTo owner
dialog.setVisible true