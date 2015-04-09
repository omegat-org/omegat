/* :name=Switch Colour Theme :description=Switch Editor Colour Theme 
 *
 * Apply a colour theme to OmegaT. The application needs to be restarted for the theme to take effect.
 *
 * @author  Briac Pilpre
 * @date    2014-09-22
 * @version 0.1
 */


var theme = 'Dark';


var themes = {
    'Default': {
        'COLOR_BACKGROUND': '#ffffff',
        'COLOR_FOREGROUND': '#000000',
        'COLOR_SOURCE': '#c0ffc0',
        'COLOR_NOTED': '#c0ffff',
        'COLOR_UNTRANSLATED': '#c0c0ff',
        'COLOR_TRANSLATED': '#ffff99',
        'COLOR_NON_UNIQUE': '#808080',
        'COLOR_PLACEHOLDER': '#969696',
        'COLOR_REMOVETEXT_TARGET': '#ff0000',
        'COLOR_NBSP': '#c8c8c8',
        'COLOR_WHITESPACE': '#808080',
        'COLOR_BIDIMARKERS': '#c80000',
        'COLOR_MARK_COMES_FROM_TM': '#fa8072',
        'COLOR_MARK_COMES_FROM_TM_XICE': '#af76df',
        'COLOR_MARK_COMES_FROM_TM_X100PC': '#ff9408',
        'COLOR_MARK_COMES_FROM_TM_XAUTO': '#ffd596',
        'COLOR_REPLACE': '#0000ff',
        'COLOR_LANGUAGE_TOOLS': '#0000ff',
        'COLOR_TRANSTIPS': '#0000ff',
        'COLOR_SPELLCHECK': '#ff0000',
        'COLOR_MOD_INFO_FG': '__DEFAULT__',
        'COLOR_ACTIVE_SOURCE': '__DEFAULT__',
        'COLOR_MATCHES_INS_ACTIVE': '__DEFAULT__',
        'COLOR_MATCHES_DEL_ACTIVE': '__DEFAULT__'
    },
    'Dark': {
        'COLOR_BACKGROUND': '#000000',
        'COLOR_FOREGROUND': '#ffffff',
        'COLOR_SOURCE': '#197a30',
        'COLOR_NOTED': '#c0ffff',
        'COLOR_UNTRANSLATED': '#5574b9',
        'COLOR_TRANSLATED': '#f26522',
        'COLOR_NON_UNIQUE': '#808080',
        'COLOR_PLACEHOLDER': '#969696',
        'COLOR_REMOVETEXT_TARGET': '#ff0000',
        'COLOR_NBSP': '#c8c8c8',
        'COLOR_WHITESPACE': '#909090',
        'COLOR_BIDIMARKERS': '#c82222',
        'COLOR_MARK_COMES_FROM_TM': '#fa8072',
        'COLOR_MARK_COMES_FROM_TM_XICE': '#af76df',
        'COLOR_MARK_COMES_FROM_TM_X100PC': '#ff9408',
        'COLOR_MARK_COMES_FROM_TM_XAUTO': '#ffd596',
        'COLOR_REPLACE': '#3333ff',
        'COLOR_LANGUAGE_TOOLS': '#3333ff',
        'COLOR_TRANSTIPS': '#3333f',
        'COLOR_SPELLCHECK': '#ff3333',
        'COLOR_MOD_INFO_FG': '__DEFAULT__',
        'COLOR_ACTIVE_SOURCE': '__DEFAULT__',
        'COLOR_MATCHES_INS_ACTIVE': '__DEFAULT__',
        'COLOR_MATCHES_DEL_ACTIVE': '__DEFAULT__'
    },
    'Trafficlight': {
        'COLOR_BACKGROUND': '#ffffff',
        'COLOR_FOREGROUND': '#000000',
        'COLOR_SOURCE': '#c0ffc0',
        'COLOR_NOTED': '#c0ffff',
        'COLOR_UNTRANSLATED': '#ffcccc',
        'COLOR_TRANSLATED': '#e5e5e5',
        'COLOR_NON_UNIQUE': '#000000',
        'COLOR_PLACEHOLDER': '#969696',
        'COLOR_REMOVETEXT_TARGET': '#ff0000',
        'COLOR_NBSP': '#c8c8c8',
        'COLOR_WHITESPACE': '#808080',
        'COLOR_BIDIMARKERS': '#c80000',
        'COLOR_MARK_COMES_FROM_TM': '#fa8072',
        'COLOR_MARK_COMES_FROM_TM_XICE': '#af76df',
        'COLOR_MARK_COMES_FROM_TM_X100PC': '#ff9408',
        'COLOR_MARK_COMES_FROM_TM_XAUTO': '#ffd596',
        'COLOR_REPLACE': '#0000ff',
        'COLOR_LANGUAGE_TOOLS': '#0000ff',
        'COLOR_TRANSTIPS': '#0000ff',
        'COLOR_SPELLCHECK': '#ff0000',
        'COLOR_MOD_INFO_FG': '#999999',
        'COLOR_ACTIVE_SOURCE': '#ffff99',
        'COLOR_MATCHES_INS_ACTIVE': '#00ff00',
        'COLOR_MATCHES_DEL_ACTIVE': '#ff0000'
    }
};


console.println('Switch to theme ' + theme);
for (var name in themes[theme]) {
    var color = themes[theme][name];
    //console.println(name + '\t' + color);
    org.omegat.util.Preferences.setPreference(name, color);
}

org.omegat.util.Preferences.save();
console.println('Theme applied, please restart OmegaT for the changes to take effect.');
