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

package org.omegat.gui.align;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.omegat.core.Core;
import org.omegat.core.data.ParseEntry;
import org.omegat.core.data.ParseEntry.ParseEntryResult;
import org.omegat.core.data.ProtectedPart;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.IParseCallback;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXWriter2;

import net.loomchild.maligna.calculator.Calculator;
import net.loomchild.maligna.calculator.length.NormalDistributionCalculator;
import net.loomchild.maligna.calculator.length.PoissonDistributionCalculator;
import net.loomchild.maligna.calculator.length.counter.CharCounter;
import net.loomchild.maligna.calculator.length.counter.Counter;
import net.loomchild.maligna.calculator.length.counter.SplitCounter;
import net.loomchild.maligna.coretypes.Alignment;
import net.loomchild.maligna.coretypes.Category;
import net.loomchild.maligna.coretypes.CategoryDefaults;
import net.loomchild.maligna.filter.Filter;
import net.loomchild.maligna.filter.aligner.align.AlignAlgorithm;
import net.loomchild.maligna.filter.aligner.align.hmm.fb.ForwardBackwardAlgorithm;
import net.loomchild.maligna.filter.aligner.align.hmm.viterbi.ViterbiAlgorithm;
import net.loomchild.maligna.matrix.FullMatrixFactory;
import net.loomchild.maligna.matrix.MatrixFactory;

/**
 * @author Aaron Madlon-Kay
 */
public class Aligner {

    final String srcFile;
    final Language srcLang;
    final String trgFile;
    final Language trgLang;

    boolean segment = true;
    boolean removeTags = false;

    enum ComparisonMode {
        PARSEWISE, HEAPWISE, ID
    }

    enum AlgorithmClass {
        VITERBI, FB
    }

    enum CalculatorType {
        NORMAL, POISSON
    }

    enum CounterType {
        CHAR, WORD
    }

    ComparisonMode comparisonMode = ComparisonMode.HEAPWISE;
    AlgorithmClass algorithmClass = AlgorithmClass.VITERBI;
    CalculatorType calculatorType = CalculatorType.NORMAL;
    CounterType counterType = CounterType.CHAR;

    private List<String> srcRaw;
    private List<String> trgRaw;
    private List<Entry<String, String>> idPairs;
    List<ComparisonMode> allowedModes;

    public Aligner(String srcFile, Language srcLang, String trgFile, Language trgLang) throws Exception {
        this.srcFile = srcFile;
        this.srcLang = srcLang;
        this.trgFile = trgFile;
        this.trgLang = trgLang;
        if (!srcLang.isSpaceDelimited() || !trgLang.isSpaceDelimited()) {
            this.counterType = CounterType.CHAR;
        }
    }

    void loadFiles() throws Exception {
        Entry<List<String>, List<String>> srcResult = parseFile(srcFile);
        srcRaw = srcResult.getValue();
        Entry<List<String>, List<String>> trgResult = parseFile(trgFile);
        trgRaw = trgResult.getValue();

        List<ComparisonMode> allowed = new ArrayList<>();
        allowed.add(ComparisonMode.HEAPWISE);
        if (srcRaw.size() == trgRaw.size()) {
            allowed.add(ComparisonMode.PARSEWISE);
        }
        List<String> srcIds = srcResult.getKey();
        List<String> trgIds = trgResult.getKey();
        if (srcIds.size() == srcRaw.size() && trgIds.size() == trgRaw.size()) {
            allowed.add(ComparisonMode.ID);
            comparisonMode = ComparisonMode.ID;

            Map<String, String> trgMap = new HashMap<>();
            IntStream.range(0, trgRaw.size()).forEach(i -> trgMap.put(trgIds.get(i), trgRaw.get(i)));
            idPairs = IntStream.range(0, srcRaw.size()).mapToObj(i -> {
                String src = srcRaw.get(i);
                String trg = trgMap.get(srcIds.get(i));
                if (src != null && trg != null) {
                    return new AbstractMap.SimpleImmutableEntry<>(src, trg);
                } else {
                    return null;
                }
            }).filter(e -> e != null).collect(Collectors.toList());
        } else {
            idPairs = Collections.emptyList();
        }
        allowedModes = Collections.unmodifiableList(allowed);
    }

    void clearLoaded() {
        srcRaw = null;
        trgRaw = null;
    }

    boolean canAlignParsewise() throws Exception {
        if (srcRaw == null || trgRaw == null) {
            loadFiles();
        }
        return srcRaw.size() == trgRaw.size();
    }

    private Entry<List<String>, List<String>> parseFile(String file) throws Exception {
        final List<String> ids = new ArrayList<>();
        final List<String> rawSegs = new ArrayList<>();
        Core.getFilterMaster().loadFile(file, new FilterContext(srcLang, trgLang, true).setRemoveAllTags(removeTags),
                new IParseCallback() {
                    @Override
                    public void linkPrevNextSegments() {
                    }

                    @Override
                    public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
                            IFilter filter) {
                        process(source, id);
                    }

                    @Override
                    public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
                            String path, IFilter filter, List<ProtectedPart> protectedParts) {
                        process(source, id != null ? id : path != null ? path : null);
                    }

                    @Override
                    public void addEntryWithProperties(String id, String source, String translation,
                            boolean isFuzzy, String[] props, String path, IFilter filter,
                            List<ProtectedPart> protectedParts) {
                        process(source, id != null ? id : path != null ? path : null);

                    }

                    private void process(String text, String id) {
                        boolean removeSpaces = Core.getFilterMaster().getConfig().isRemoveSpacesNonseg();
                        text = StringUtil.normalizeUnicode(ParseEntry.stripSomeChars(text,
                                new ParseEntryResult(), removeTags, removeSpaces));
                        if (!text.trim().isEmpty()) {
                            if (id != null) {
                                ids.add(id);
                            }
                            rawSegs.add(text);
                        }
                    }
                });
        return new AbstractMap.SimpleImmutableEntry<>(ids, rawSegs);
    }

    private List<String> segmentAll(Language language, List<String> rawTexts) {
        return rawTexts.stream().map(text -> Core.getSegmenter().segment(language, text, null, null))
                .flatMap(List::stream).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    private List<MutableBead> alignParsewiseNotSegmented() {
        if (srcRaw.size() != trgRaw.size()) {
            throw new UnsupportedOperationException();
        }
        return IntStream.range(0, srcRaw.size()).mapToObj(i -> new MutableBead(srcRaw.get(i), trgRaw.get(i)))
                .collect(Collectors.toList());
    }

    private List<MutableBead> alignParsewiseSegmented() {
        if (srcRaw.size() != trgRaw.size()) {
            throw new UnsupportedOperationException();
        }
        return IntStream.range(0, srcRaw.size()).mapToObj(i -> {
            List<String> source = Core.getSegmenter().segment(srcLang, srcRaw.get(i), null, null).stream()
                    .filter(s -> !s.isEmpty()).collect(Collectors.toList());
            List<String> target = Core.getSegmenter().segment(trgLang, trgRaw.get(i), null, null).stream()
                    .filter(s -> !s.isEmpty()).collect(Collectors.toList());
            return doAlign(algorithmClass, calculatorType, counterType, source, target);
        }).flatMap(List::stream).map(MutableBead::new).collect(Collectors.toList());
    }

    private List<MutableBead> alignByIdNotSegmented() {
        return idPairs.stream().map(e -> new MutableBead(e.getKey(), e.getKey()))
                .collect(Collectors.toList());
    }

    private List<MutableBead> alignByIdSegmented() {
        return idPairs.stream().map(e -> {
            List<String> source = Core.getSegmenter().segment(srcLang, e.getKey(), null, null).stream()
                    .filter(s -> !s.isEmpty()).collect(Collectors.toList());
            List<String> target = Core.getSegmenter().segment(trgLang, e.getValue(), null, null).stream()
                    .filter(s -> !s.isEmpty()).collect(Collectors.toList());
            return doAlign(algorithmClass, calculatorType, counterType, source, target);
        }).flatMap(List::stream).map(MutableBead::new).collect(Collectors.toList());
    }

    private List<MutableBead> alignHeapwise(boolean doSegmenting) {
        List<String> srcSegs = doSegmenting ? segmentAll(srcLang, srcRaw) : srcRaw;
        List<String> trgSegs = doSegmenting ? segmentAll(trgLang, trgRaw) : trgRaw;
        return doAlign(algorithmClass, calculatorType, counterType, srcSegs, trgSegs).stream()
                .map(MutableBead::new).collect(Collectors.toList());
    }

    static double calculateAvgDist(List<MutableBead> beads) {
        return beads.stream().mapToDouble(bead -> bead.score).average().orElse(Double.MAX_VALUE);
    }

    public void writePairsToTMX(File outFile, List<Entry<String, String>> pairs) throws Exception {
        TMXWriter2 writer = null;
        String creator = OStrings.getApplicationName() + " Aligner";
        long time = System.currentTimeMillis();
        try {
            writer = new TMXWriter2(outFile, srcLang, trgLang, true, true, true);
            for (Entry<String, String> e : pairs) {
                writer.writeEntry(e.getKey(), e.getValue(), null, creator, time, null, 0L, null);
            }
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ex) {
                    Log.log(ex);
                }
            }
        }
    }

    List<MutableBead> alignToBeads() throws Exception {
        if (srcRaw == null || trgRaw == null) {
            loadFiles();
        }
        switch (comparisonMode) {
        case PARSEWISE:
            return segment ? alignParsewiseSegmented() : alignParsewiseNotSegmented();
        case HEAPWISE:
            return alignHeapwise(segment);
        case ID:
            return segment ? alignByIdSegmented() : alignByIdNotSegmented();
        }
        throw new UnsupportedOperationException("Unknown comparison mode: " + comparisonMode);
    }

    List<Entry<String, String>> beadsToEntries(List<MutableBead> beads) {
        return beads.stream().filter(bead -> bead.enabled).map(bead -> {
            String srcOut = join(srcLang, bead.sourceLines);
            String trgOut = join(trgLang, bead.targetLines);
            return new AbstractMap.SimpleImmutableEntry<String, String>(srcOut, trgOut);
        }).collect(Collectors.toList());
    }

    public List<Entry<String, String>> align() throws Exception {
        return beadsToEntries(alignToBeads());
    }

    static String join(Language lang, List<?> items) {
        return Util.join(lang.isSpaceDelimited() ? " " : "", items);
    }

    enum Status {
        DEFAULT, ACCEPTED, NEEDS_REVIEW
    }

    static class MutableBead {
        public final float score;
        public final List<String> sourceLines;
        public final List<String> targetLines;
        public boolean enabled;
        public Status status;

        private MutableBead(float score, List<String> sourceLines, List<String> targetLines) {
            this.score = score;
            this.sourceLines = new ArrayList<String>(sourceLines);
            this.targetLines = new ArrayList<String>(targetLines);
            this.enabled = !Util.deepEquals(sourceLines, targetLines);
            this.status = Status.DEFAULT;
        }

        public MutableBead(Alignment alignment) {
            this(alignment.getScore(), alignment.getSourceSegmentList(), alignment.getTargetSegmentList());
        }

        public MutableBead(List<String> sourceLines, List<String> targetLines) {
            this(Float.MAX_VALUE, sourceLines, targetLines);
        }

        public MutableBead(String source, String target) {
            this(Arrays.asList(source), Arrays.asList(target));
        }

        public MutableBead() {
            this(Collections.emptyList(), Collections.emptyList());
        }

        public boolean isBalanced() {
            return sourceLines.size() == targetLines.size();
        }

        public boolean isEmpty() {
            return sourceLines.isEmpty() && targetLines.isEmpty();
        }
    }

    private static Calculator getCalculator(CalculatorType calculatorType, CounterType counterType,
            List<Alignment> aligns) {
        Counter counter = getCounter(counterType);
        switch (calculatorType) {
        case NORMAL:
            return new NormalDistributionCalculator(counter);
        case POISSON:
            return new PoissonDistributionCalculator(counter, aligns);
        }
        throw new UnsupportedOperationException("Unsupported calculator type: " + calculatorType);
    }

    private static Counter getCounter(CounterType counterType) {
        switch (counterType) {
        case CHAR:
            return new CharCounter();
        case WORD:
            return new SplitCounter();
        }
        throw new UnsupportedOperationException("Unsupported counter type: " + counterType);
    }

    private static AlignAlgorithm getAlgorithm(AlgorithmClass algorithmClass, Calculator calculator) {
        MatrixFactory matrixFactory = new FullMatrixFactory();
        Map<Category, Float> map = CategoryDefaults.BEST_CATEGORY_MAP;
        switch (algorithmClass) {
        case VITERBI:
            return new ViterbiAlgorithm(calculator, map, matrixFactory);
        case FB:
            return new ForwardBackwardAlgorithm(calculator, map, matrixFactory);
        }
        throw new UnsupportedOperationException("Unsupported algorithm class: " + algorithmClass);
    }

    private static List<Alignment> doAlign(AlgorithmClass algorithmClass, CalculatorType calculatorType,
            CounterType counterType, List<String> source, List<String> target) {
        List<Alignment> aligns = Arrays.asList(new Alignment(source, target));
        Calculator calculator = getCalculator(calculatorType, counterType, aligns);
        AlignAlgorithm algorithm = getAlgorithm(algorithmClass, calculator);
        Filter filter = new net.loomchild.maligna.filter.aligner.Aligner(algorithm);
        // filter = FilterDecorators.decorate(filter);
        return filter.apply(aligns);
    }

}
