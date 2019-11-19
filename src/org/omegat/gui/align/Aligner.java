/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
 * Class to drive alignment of input files. Responsible for filtering and performing automatic alignment with
 * mALIGNa.
 *
 * @author Aaron Madlon-Kay
 *
 * @see <a href="https://github.com/loomchild/maligna">mALIGNa</a>
 */
public class Aligner {

    final String srcFile;
    final Language srcLang;
    final String trgFile;
    final Language trgLang;

    boolean segment = true;
    boolean removeTags = false;

    /**
     * Modes indicating the ways in which the source text can be sent to the alignment algorithm.
     */
    enum ComparisonMode {
        /**
         * Take all source lines and align against all target lines. This is the default as it makes no
         * demands of the input files.
         */
        HEAPWISE,

        /**
         * This mode is only available when the source and target files extract to the same number of text
         * units. Source and target strings with the same index are aligned separately.
         */
        PARSEWISE,

        /**
         * This mode is only available when the source and target files provide IDs for all their text units.
         * Each unit with matching ID is aligned separately.
         */
        ID
    }

    enum AlgorithmClass {
        /**
         * @see <a href=
         *      "https://github.com/loomchild/maligna/blob/3.0.0/maligna/src/main/java/net/loomchild/maligna/filter/aligner/align/hmm/viterbi/ViterbiAlgorithm.java">
         *      ViterbiAlgorithm.java</a>
         */
        VITERBI,

        /**
         * @see <a href=
         *      "https://github.com/loomchild/maligna/blob/3.0.0/maligna/src/main/java/net/loomchild/maligna/filter/aligner/align/hmm/fb/ForwardBackwardAlgorithm.java">
         *      ForwardBackwardAlgorithm.java</a>
         */
        FB
    }

    enum CalculatorType {
        /**
         * @see <a href=
         *      "https://github.com/loomchild/maligna/blob/3.0.0/maligna/src/main/java/net/loomchild/maligna/calculator/length/NormalDistributionCalculator.java">
         *      NormalDistributionCalculator.java</a>
         */
        NORMAL,

        /**
         * @see <a href=
         *      "https://github.com/loomchild/maligna/blob/3.0.0/maligna/src/main/java/net/loomchild/maligna/calculator/length/PoissonDistributionCalculator.java">
         *      PoissonDistributionCalculator.java</a>
         */
        POISSON
    }

    enum CounterType {
        CHAR,
        WORD
    }

    ComparisonMode comparisonMode;
    AlgorithmClass algorithmClass;
    CalculatorType calculatorType;
    CounterType counterType;

    private List<String> srcRaw;
    private List<String> trgRaw;
    private List<Entry<String, String>> idPairs;
    List<ComparisonMode> allowedModes;

    public Aligner(String srcFile, Language srcLang, String trgFile, Language trgLang) {
        this.srcFile = srcFile;
        this.srcLang = srcLang;
        this.trgFile = trgFile;
        this.trgLang = trgLang;
        restoreDefaults();
    }

    /**
     * Parse the input files and extract the alignable text, which is retained in memory so that different
     * alignment settings can be tried without re-parsing the files. This determines the available
     * {@link ComparisonMode}s, available in {@link #allowedModes}.
     *
     * @throws Exception
     *             If the parsing fails for whatever reason
     */
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
            }).filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            idPairs = Collections.emptyList();
        }
        allowedModes = Collections.unmodifiableList(allowed);
    }

    /**
     * Release all content loaded from the input files.
     */
    void clearLoaded() {
        srcRaw = null;
        trgRaw = null;
        idPairs = null;
    }

    void restoreDefaults() {
        comparisonMode = ComparisonMode.HEAPWISE;
        algorithmClass = AlgorithmClass.VITERBI;
        calculatorType = CalculatorType.NORMAL;
        if (!srcLang.isSpaceDelimited() || !trgLang.isSpaceDelimited()) {
            counterType = CounterType.CHAR;
        } else {
            counterType = CounterType.WORD;
        }
    }

    /**
     * Parse the specified file and return the contents as a pair of lists:
     * <ul>
     * <li>Key: A list of IDs for the parsed text units
     * <li>Value: A list of parsed text units
     * </ul>
     *
     * @param file
     *            Path to input file
     * @return Pair of lists
     * @throws Exception
     *             If parsing fails
     */
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

    /**
     * Segment the specified list of strings into a flat list of strings. The resulting list will be free of
     * empty strings.
     *
     * @param language
     *            The language of the texts to be segmented
     * @param rawTexts
     *            List of texts to be segmented
     * @return Flattened list of segments
     */
    private List<String> segmentAll(Language language, List<String> rawTexts) {
        return rawTexts.stream().flatMap(text -> Core.getSegmenter().segment(language, text, null, null).stream())
                .filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    /**
     * Align {@link ComparisonMode#PARSEWISE} without first segmenting the source and target strings. No
     * alignment algorithm is applied.
     *
     * @return List of beads where each entry of {@link #srcRaw} is aligned by index with each entry of
     *         {@link #trgRaw}
     */
    private Stream<Alignment> alignParsewiseNotSegmented() {
        if (!allowedModes.contains(ComparisonMode.PARSEWISE)) {
            throw new UnsupportedOperationException();
        }
        return IntStream.range(0, srcRaw.size())
                .mapToObj(i -> new Alignment(Arrays.asList(srcRaw.get(i)), Arrays.asList(trgRaw.get(i))));
    }

    /**
     * Align {@link ComparisonMode#PARSEWISE} the source and target strings. Each pair is segmented and
     * aligned separately by algorithm.
     *
     * @return List of beads where each entry of {@link #srcRaw} is aligned by index with each entry of
     *         {@link #trgRaw}
     */
    private Stream<Alignment> alignParsewiseSegmented() {
        if (!allowedModes.contains(ComparisonMode.PARSEWISE)) {
            throw new UnsupportedOperationException();
        }
        return IntStream.range(0, srcRaw.size()).mapToObj(i -> {
            List<String> source = Core.getSegmenter().segment(srcLang, srcRaw.get(i), null, null).stream()
                    .filter(s -> !s.isEmpty()).collect(Collectors.toList());
            List<String> target = Core.getSegmenter().segment(trgLang, trgRaw.get(i), null, null).stream()
                    .filter(s -> !s.isEmpty()).collect(Collectors.toList());
            return doAlign(algorithmClass, calculatorType, counterType, source, target);
        }).flatMap(List::stream);
    }

    /**
     * Align by {@link ComparisonMode#ID} without first segmenting the source and target strings. No alignment
     * algorithm is applied.
     *
     * @return List of beads aligned by ID
     */
    private Stream<Alignment> alignByIdNotSegmented() {
        if (!allowedModes.contains(ComparisonMode.ID)) {
            throw new UnsupportedOperationException();
        }
        return idPairs.stream()
                .map(e -> new Alignment(Arrays.asList(e.getKey()), Arrays.asList(e.getValue())));
    }

    /**
     * Align source and target strings by {@link ComparisonMode#ID}. Each pair is segmented and aligned
     * separately by algorithm.
     *
     * @return List of beads aligned by ID
     */
    private Stream<Alignment> alignByIdSegmented() {
        if (!allowedModes.contains(ComparisonMode.ID)) {
            throw new UnsupportedOperationException();
        }
        return idPairs.stream().map(e -> {
            List<String> source = Core.getSegmenter().segment(srcLang, e.getKey(), null, null).stream()
                    .filter(s -> !s.isEmpty()).collect(Collectors.toList());
            List<String> target = Core.getSegmenter().segment(trgLang, e.getValue(), null, null).stream()
                    .filter(s -> !s.isEmpty()).collect(Collectors.toList());
            return doAlign(algorithmClass, calculatorType, counterType, source, target);
        }).flatMap(List::stream);
    }

    /**
     * Align {@link ComparisonMode#HEAPWISE}. Input text is optionally segmented, then aligned by algorithm.
     *
     * @param doSegmenting
     *            Whether to segment the text
     * @return List of beads aligned heapwise
     */
    private Stream<Alignment> alignHeapwise(boolean doSegmenting) {
        List<String> srcSegs = doSegmenting ? segmentAll(srcLang, srcRaw) : srcRaw;
        List<String> trgSegs = doSegmenting ? segmentAll(trgLang, trgRaw) : trgRaw;
        return doAlign(algorithmClass, calculatorType, counterType, srcSegs, trgSegs).stream();
    }

    public void writePairsToTMX(File outFile, List<Entry<String, String>> pairs) throws Exception {
        TMXWriter2 writer = null;
        String creator = OStrings.getApplicationName() + " Aligner";
        long time = System.currentTimeMillis();
        try {
            writer = new TMXWriter2(outFile, srcLang, trgLang, true, true, false);
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

    /**
     * Perform alignment according to the current settings and return the resulting list of beads. Will call
     * {@link #loadFiles()} if it has not yet been called.
     *
     * @return List of beads
     * @throws Exception
     *             If parsing the input files fails
     */
    Stream<Alignment> alignImpl() throws Exception {
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

    /**
     * Align the input files according to the current settings to a list of pairs where
     * <ol>
     * <li>key = source text
     * <li>value = target text
     * </ol>
     *
     * Calls {@link #loadFiles()} if it has not yet been called.
     *
     * @return
     * @throws Exception
     */
    public List<Entry<String, String>> align() throws Exception {
        return alignImpl().map(bead -> {
            String srcOut = Util.join(srcLang, bead.getSourceSegmentList());
            String trgOut = Util.join(trgLang, bead.getTargetSegmentList());
            return new AbstractMap.SimpleImmutableEntry<String, String>(srcOut, trgOut);
        }).collect(Collectors.toList());
    }

    /**
     * Obtain appropriate calculator according to the specified {@link CalculatorType}.
     *
     * @param calculatorType
     * @param counterType
     * @param aligns
     * @return
     */
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

    /**
     * Obtain appropriate counter according to the specified {@link CounterType}.
     *
     * @param counterType
     * @return
     */
    private static Counter getCounter(CounterType counterType) {
        switch (counterType) {
        case CHAR:
            return new CharCounter();
        case WORD:
            return new SplitCounter();
        }
        throw new UnsupportedOperationException("Unsupported counter type: " + counterType);
    }

    /**
     * Obtain appropriate align algorithm object according to the specified {@link AlgorithmClass}.
     *
     * @param algorithmClass
     * @param calculator
     * @return
     */
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

    /**
     * Use mALIGNa to align the specified source and target texts, according to the specified parameters.
     *
     * @param algorithmClass
     * @param calculatorType
     * @param counterType
     * @param source
     * @param target
     * @return
     */
    private static List<Alignment> doAlign(AlgorithmClass algorithmClass, CalculatorType calculatorType,
            CounterType counterType, List<String> source, List<String> target) {
        List<Alignment> aligns = Arrays.asList(new Alignment(source, target));
        Calculator calculator = getCalculator(calculatorType, counterType, aligns);
        AlignAlgorithm algorithm = getAlgorithm(algorithmClass, calculator);
        Filter filter = new net.loomchild.maligna.filter.aligner.Aligner(algorithm);
        // filter = FilterDecorators.decorate(filter);
        return filter.apply(aligns);
    }

    List<MutableBead> doAlign(List<MutableBead> beads) {
        List<String> source = new ArrayList<>();
        List<String> target = new ArrayList<>();
        for (MutableBead bead : beads) {
            source.addAll(bead.sourceLines);
            target.addAll(bead.targetLines);
        }
        return doAlign(algorithmClass, calculatorType, counterType, source, target).stream()
                .map(MutableBead::new).collect(Collectors.toList());
    }
}
