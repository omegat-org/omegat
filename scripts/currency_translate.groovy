/*
 *  Translate currencies representation accorfing to source and target locale.
 *  This will translate a string like "$123,399.99" to "123 399,99 USD"
 *
 * @author  Briac Pilpré
 * @date    2011-10-30
 * @version 0.1
 */
import java.text.NumberFormat
 
console.println("Convert currencies according to source and target locales.\n");

// USD Currency Format
// From sun.text.resources.LocalElements_en_US.java : \u00a4#,##0.00;(\u00a4#,##0.00)
def sourceCurrencyPattern = ~/\$\s*((\d+,)*(\d+)(\.\d+)?)/

// Use project source language
def sourceCurrency = NumberFormat.getCurrencyInstance(project.projectProperties.sourceLanguage.locale);
// Use project target language
def targetCurrency = NumberFormat.getCurrencyInstance(project.projectProperties.targetLanguage.locale);
// Don't change the currency (sums in USD stay in USD !)
targetCurrency.setCurrency(sourceCurrency.currency);

def segment_count = 0

project.allEntries.each { ste ->
    source = ste.getSrcText();
    target = project.getTranslationInfo(ste) ? project.getTranslationInfo(ste).translation : null;

    // Skip untranslated segments
    if (target == null) return

    def matcher =  target =~ sourceCurrencyPattern
    matcher.each { m -> 
        def money = m[0]
        def formatted = targetCurrency.format(sourceCurrency.parse(money))
        target = target.replaceFirst("\\Q$money\\E", formatted)

        segment_count++

        editor.gotoEntry(ste.entryNum())
        console.println(ste.entryNum() + "\t" + ste.srcText + "\t" + target )
        // XXX CAUTION -- BACKUP YOUR PROJECT BEFORE REMOVING THE COMMENT XXX // editor.replaceEditText(target)
    }
}

console.println("Segments modifier: " + segment_count);

