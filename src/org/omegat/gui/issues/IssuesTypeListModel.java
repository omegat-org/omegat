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

import org.omegat.util.OStrings;

import javax.swing.AbstractListModel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("serial")
class IssuesTypeListModel extends AbstractListModel<String> {

    private final transient List<Map.Entry<String, Long>> types;

    IssuesTypeListModel(List<IIssue> issues) {
        this.types = calculateData(issues);
    }

    List<Map.Entry<String, Long>> calculateData(List<IIssue> issues) {
        Map<String, Long> counts = issues.stream().map(IIssue::getTypeName)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<Map.Entry<String, Long>> result = new ArrayList<>();
        result.add(new AbstractMap.SimpleImmutableEntry<>(IssuesPanelController.ALL_TYPES,
                (long) issues.size()));
        counts.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(result::add);
        return result;
    }

    @Override
    public int getSize() {
        return types.size();
    }

    @Override
    public String getElementAt(int index) {
        Map.Entry<String, Long> entry = types.get(index);
        return OStrings.getString("ISSUES_TYPE_SUMMARY_TEMPLATE", entry.getKey(), entry.getValue());
    }

    List<String> getTypesAt(int[] indicies) {
        return IntStream.of(indicies).mapToObj(types::get).map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    long getCountAt(int[] indicies) {
        return IntStream.of(indicies).mapToObj(types::get).mapToLong(Map.Entry::getValue).sum();
    }

    int[] indiciesOfTypes(List<String> queryTypes) {
        return queryTypes.stream()
                .map(type -> IntStream.range(0, queryTypes.size())
                        .filter(i -> types.get(i).getKey().equals(type)).findFirst())
                .filter(OptionalInt::isPresent).mapToInt(OptionalInt::getAsInt).toArray();
    }
}
