package org.omegat.core.statistics;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import org.omegat.util.OStrings;

public enum StatOutputFormat {
    TEXT(".txt"), XML(".xml"), JSON(".json");

    private static Map<String, StatOutputFormat> valueMap = null;
    private String fileExtension;

    StatOutputFormat(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public static StatOutputFormat getValue(String format) {
        if (valueMap == null) {
            valueMap = new HashMap<>();
            for (StatOutputFormat day : values()) {
                valueMap.put(day.toString(), day);
            }
        }
        return valueMap.get(format.toUpperCase());
    }

    public static ComboBoxModel<StatOutputFormat> getComboBoxModel() {
        return new ComboBoxModel<>() {
            private StatOutputFormat selectedObject;

            @Override
            public int getSize() {
                return StatOutputFormat.values().length;
            }

            @Override
            public StatOutputFormat getElementAt(int index) {
                return StatOutputFormat.values()[index];
            }

            @Override
            public void addListDataListener(ListDataListener l) {
                /* empty */
            }

            @Override
            public void removeListDataListener(ListDataListener l) {
                /* empty */
            }

            @Override
            public void setSelectedItem(Object anItem) {
                selectedObject = (StatOutputFormat) anItem;
            }

            @Override
            public StatOutputFormat getSelectedItem() {
                return selectedObject;
            }
        };
    }

    @Override
    public String toString() {
        return OStrings.getString("STATS_FORMAT_" + name());
    }
}
