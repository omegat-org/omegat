/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2009 Didier Briel
               2012 Alex Buloichik, Didier Briel
               2014 Alex Buloichik, Aaron Madlon-Kay
               2015-2016 Aaron Madlon-Kay
               2020 Briac Pilpre
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

package org.omegat.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.omegat.core.Core;

/**
 * Files processing utilities.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 */
public final class FileUtil {
    public static final long RENAME_RETRY_TIMEOUT = 3000;

    private FileUtil() {
    }

    /**
     * Get most recent backup file path.
     * 
     * @param originalFile
     *            target original file name.
     */
    public static File getRecentBackup(final File originalFile) {
        File[] bakFiles = originalFile.getParentFile()
                .listFiles(f -> !f.isDirectory() && f.getName().startsWith(originalFile.getName())
                        && f.getName().endsWith(OConsts.BACKUP_EXTENSION));
        if (bakFiles == null || bakFiles.length == 0) {
            return null;
        }
        Arrays.sort(bakFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        return bakFiles[0];
    }

    /**
     * Removes older backups to be under specified number of files.
     * 
     * @param originalFile
     *            target original file name.
     * @param maxBackups
     *            maximum number of back up files.
     */
    public static void removeOldBackups(final File originalFile, int maxBackups) {
        try {
            File[] bakFiles = originalFile.getParentFile()
                    .listFiles(f -> !f.isDirectory() && f.getName().startsWith(originalFile.getName())
                            && f.getName().endsWith(OConsts.BACKUP_EXTENSION));

            if (bakFiles != null && bakFiles.length > maxBackups) {
                Arrays.sort(bakFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                for (int i = maxBackups; i < bakFiles.length; i++) {
                    boolean ignored = bakFiles[i].delete();
                }
            }
        } catch (Exception e) {
            // we don't care
        }
    }

    /**
     * Create file backup with datetime suffix.
     * 
     * @param original
     *            a file to copy as backup file.
     * @return Backup file.
     */
    public static File backupFile(File original) {
        File backup = new File(original.getParentFile(), getBackupFilename(original));
        try {
            FileUtils.copyFile(original, backup);
        } catch (IOException ex) {
            Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_CREATE_BACKUP_FILE", original.getName());
        }
        return backup;
    }

    /**
     * Generates a name for the file to be backuped, in the form of <code>[original_name].yyyyMMddHHmm.bak</code>.
     * 
     * @param original the file to be backuped
     * @return the name of the backuped file
     */
    public static String getBackupFilename(File original) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return String.format("%s.%s%s", original.getName(),
                dateFormat.format(new Date(original.lastModified())), OConsts.BACKUP_EXTENSION);
    }

    /**
     * Renames file, with checking errors and 3 seconds retry against external
     * programs (like antivirus or TortoiseSVN) locking.
     */
    public static void rename(File from, File to) throws IOException {
        if (!from.exists()) {
            throw new IOException("Source file to rename (" + from + ") doesn't exist");
        }
        if (to.exists()) {
            throw new IOException("Target file to rename (" + to + ") already exists");
        }
        long b = System.currentTimeMillis();
        while (!from.renameTo(to)) {
            long e = System.currentTimeMillis();
            if (e - b > RENAME_RETRY_TIMEOUT) {
                throw new IOException("Error renaming " + from + " to " + to);
            }
        }
    }

    /**
     * Copy file and create output directory if need. EOL will be converted into
     * target-specific or into platform-specific if target doesn't exist.
     */
    public static void copyFileWithEolConversion(File inFile, File outFile, Charset charset)
            throws IOException {
        File dir = outFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String eol = null;
        if (outFile.exists()) {
            // file exists - read EOL from file
            eol = getEOL(outFile, charset);
        }
        if (eol == null) {
            // file does not exist or EOL not detected - use system-dependent
            // value
            eol = System.lineSeparator();
        }
        try (BufferedReader in = Files.newBufferedReader(inFile.toPath(), charset)) {
            try (BufferedWriter out = Files.newBufferedWriter(outFile.toPath(), charset)) {
                String s;
                while ((s = in.readLine()) != null) {
                    // copy using known EOL
                    out.write(s);
                    out.write(eol);
                }
            }
        }
    }

    /**
     * Read a file to determine its end-of-line character(s).
     *
     * If neither '\n' nor '\r' are present in the file then it will return
     * null.
     *
     * @param file
     * @param charset
     * @return The EOL character(s) as a string, or null if not detectable
     * @throws IOException
     */
    public static String getEOL(File file, Charset charset) throws IOException {
        String r = null;
        try (BufferedReader in = Files.newBufferedReader(file.toPath(), charset)) {
            while (true) {
                int ch = in.read();
                if (ch < 0) {
                    break;
                }
                if (ch == '\n' || ch == '\r') {
                    r = Character.toString((char) ch);
                    int ch2 = in.read();
                    if (ch2 == '\n' || ch2 == '\r') {
                        r += Character.toString((char) ch2);
                    }
                    break;
                }
            }
        }
        return r;
    }

    /**
     * Find files in subdirectories.
     *
     * @param dir
     *            directory to start find
     * @param filter
     *            filter for found files
     * @return list of filtered found files
     */
    public static List<File> findFiles(final File dir, final FileFilter filter) {
        final List<File> result = new ArrayList<File>();
        Set<String> knownDirs = new HashSet<String>();
        findFiles(dir, filter, result, knownDirs);
        return result;
    }

    /**
     * Internal find method, which calls himself recursively.
     *
     * @param dir
     *            directory to start find
     * @param filter
     *            filter for found files
     * @param result
     *            list of filtered found files
     */
    private static void findFiles(final File dir, final FileFilter filter, final List<File> result,
            final Set<String> knownDirs) {
        String currDir;
        try {
            // check for recursive
            currDir = dir.getCanonicalPath();
            if (!knownDirs.add(currDir)) {
                return;
            }
        } catch (IOException ex) {
            Log.log(ex);
            return;
        }
        File[] list = dir.listFiles();
        if (list != null) {
            for (File f : list) {
                if (f.isDirectory()) {
                    findFiles(f, filter, result, knownDirs);
                } else {
                    if (filter.accept(f)) {
                        result.add(f);
                    }
                }
            }
        }
    }

    /**
     * Compute relative path of file.
     *
     * @param rootDir
     *            root directory
     * @param file
     *            file
     * @return
     */
    public static String computeRelativePath(File rootDir, File file) throws IOException {
        String rootAbs = rootDir.getAbsolutePath().replace('\\', '/') + '/';
        String fileAbs = file.getAbsolutePath().replace('\\', '/');
        if (Platform.isWindows) {
            if (!fileAbs.toUpperCase().startsWith(rootAbs.toUpperCase())) {
                throw new IOException("File '" + file + "' is not under dir '" + rootDir + "'");
            }
        } else {
            if (!fileAbs.startsWith(rootAbs)) {
                throw new IOException("File '" + file + "' is not under dir '" + rootDir + "'");
            }
        }
        return fileAbs.substring(rootAbs.length());
    }

    /**
     * Check if file is in specified path.
     */
    public static boolean isInPath(File path, File tmxFile) {
        try {
            computeRelativePath(path, tmxFile);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Expand tilde(~) in first character in path string.
     * 
     * @param path
     *            path string
     * @return expanded path string
     */
    public static String expandTildeHomeDir(final String path) {
        if (path.startsWith("~+/") || path.equals("~+")) {
            Path currentRelativePath = Paths.get("");
            String pwd = currentRelativePath.toAbsolutePath().toString();
            return path.replaceFirst("^~+", Matcher.quoteReplacement(pwd));
        } else if (path.startsWith("~-/") || path.equals("~-")) {
            String oldpwd = System.getenv("OLDPWD");
            if (oldpwd != null) {
                return path.replaceFirst("^~-", Matcher.quoteReplacement(oldpwd));
            }
        } else if (path.startsWith("~/") || path.equals("~")) {
            String homedir = FileUtils.getUserDirectoryPath();
            return path.replaceFirst("^~", Matcher.quoteReplacement(homedir));
        }
        // when all condition not passed, don't touch
        return path;
    }

    public interface ICollisionCallback {
        boolean isCanceled();

        boolean shouldReplace(File file, int thisFile, int totalFiles);
    }

    /**
     * Copy a collection of files to a destination. Recursively copies contents
     * of directories while preserving relative paths. Provide an
     * {@link ICollisionCallback} to determine what to do with files with
     * conflicting names; they will be overwritten if the callback is null.
     * 
     * @param destination
     *            Directory to copy to
     * @param toCopy
     *            Files to copy
     * @param onCollision
     *            Callback that determines what to do in case files with the
     *            same name already exist
     * @throws IOException
     */
    public static void copyFilesTo(File destination, File[] toCopy, ICollisionCallback onCollision)
            throws IOException {
        if (destination.exists() && !destination.isDirectory()) {
            throw new IOException("Copy-to destination exists and is not a directory.");
        }
        Map<File, File> collisions = copyFilesTo(destination, toCopy, (File) null);
        if (collisions.isEmpty()) {
            return;
        }
        List<File> toReplace = new ArrayList<File>();
        List<File> toDelete = new ArrayList<File>();
        int count = 0;
        for (Entry<File, File> e : collisions.entrySet()) {
            if (onCollision != null && onCollision.isCanceled()) {
                break;
            }
            if (onCollision == null || onCollision.shouldReplace(e.getValue(), count, collisions.size())) {
                toReplace.add(e.getKey());
                toDelete.add(e.getValue());
            }
            count++;
        }
        if (onCollision == null || !onCollision.isCanceled()) {
            for (File file : toDelete) {
                FileUtils.forceDelete(file);
            }
            copyFilesTo(destination, toReplace.toArray(new File[toReplace.size()]), (File) null);
        }
    }

    private static Map<File, File> copyFilesTo(File destination, File[] toCopy, File root)
            throws IOException {
        Map<File, File> collisions = new LinkedHashMap<File, File>();
        for (File file : toCopy) {
            if (destination.getPath().startsWith(file.getPath())) {
                // Trying to copy something into its own subtree
                continue;
            }
            File thisRoot = root == null ? file.getParentFile() : root;
            String filePath = file.getPath();
            String relPath = filePath.substring(thisRoot.getPath().length(), filePath.length());
            File dest = new File(destination, relPath);
            if (file.equals(dest)) {
                // Trying to copy file to itself. Skip.
                continue;
            }
            if (dest.exists()) {
                collisions.put(file, dest);
                continue;
            }
            if (file.isDirectory()) {
                copyFilesTo(destination, file.listFiles(), thisRoot);
            } else {
                FileUtils.copyFile(file, dest);
            }
        }
        return collisions;
    }

    private static final Pattern RE_ABSOLUTE_WINDOWS = Pattern.compile("[A-Za-z]\\:(/.*)");
    private static final Pattern RE_ABSOLUTE_LINUX = Pattern.compile("/.*");

    /**
     * Checks if path starts with possible root on the Linux, MacOS, Windows.
     */
    public static boolean isRelative(String path) {
        path = path.replace('\\', '/');
        return !RE_ABSOLUTE_LINUX.matcher(path).matches() && !RE_ABSOLUTE_WINDOWS.matcher(path).matches();
    }

    /**
     * Converts Windows absolute path into current system's absolute path. It
     * required for conversion like 'C:\zzz' into '/zzz' for be real absolute in
     * Linux.
     */
    public static String absoluteForSystem(String path) {
        path = path.replace('\\', '/');
        Matcher m = RE_ABSOLUTE_WINDOWS.matcher(path);
        if (m.matches()) {
            if (!Platform.isWindows) {
                // Windows' absolute file on non-Windows system
                return m.group(1);
            }
        }
        return path;
    }

    /**
     * Returns a list of all files under the root directory by absolute path.
     *
     * @throws IOException
     */
    public static List<File> buildFileList(File rootDir, boolean recursive) throws IOException {
        int depth = recursive ? Integer.MAX_VALUE : 0;
        try (Stream<Path> stream = Files.find(rootDir.toPath(), depth, (p, attr) -> p.toFile().isFile(),
                FileVisitOption.FOLLOW_LINKS)) {
            return stream.map(Path::toFile).sorted(StreamUtil.localeComparator(File::getPath))
                    .collect(Collectors.toList());
        }
    }

    public static List<String> buildRelativeFilesList(File rootDir, List<String> includes,
            List<String> excludes) throws IOException {
        Path root = rootDir.toPath();
        Pattern[] includeMasks = FileUtil.compileFileMasks(includes);
        Pattern[] excludeMasks = FileUtil.compileFileMasks(excludes);
        BiPredicate<Path, BasicFileAttributes> pred = (p, attr) -> attr.isRegularFile()
                && FileUtil.checkFileInclude(root.relativize(p).toString(), includeMasks, excludeMasks);
        int maxDepth = Integer.MAX_VALUE;
        List<String> result = new ArrayList<>();
        try (Stream<Path> stream = Files.find(root, maxDepth, pred, FileVisitOption.FOLLOW_LINKS)) {
            try {
                stream.forEachOrdered(p -> result.add(root.relativize(p).toString().replace('\\', '/')));
            } catch (UncheckedIOException e) {
                IOException ioe = e.getCause();
                if (ioe instanceof FileSystemLoopException) {
                    // source file folder has a looped symbolic links
                    String p = ((FileSystemLoopException) ioe).getFile();
                    Core.getMainWindow().displayWarningRB("TF_LOAD_WARN_SOURCE_LOOP_EXCEPTION",null, p);
                    Log.logWarningRB("TF_LOAD_WARN_SOURCE_LOOP_EXCEPTION", p);
                } else if (ioe instanceof AccessDeniedException) {
                    String p = ((AccessDeniedException) ioe).getFile();
                    Core.getMainWindow().displayWarningRB("TF_LOAD_WARN_FILE_ACCESS", p);
                    Log.logWarningRB("TF_LOAD_WARN_FILE_ACCESS", p);
                } else {
                    throw ioe; // propagate to caller
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    public static boolean checkFileInclude(String filePath, Pattern[] includes, Pattern[] excludes) {
        String normalized = filePath.replace('\\', '/');
        String checkPath = normalized.startsWith("/") ? normalized : '/' + normalized;
        boolean included = Stream.of(includes).map(p -> p.matcher(checkPath)).anyMatch(Matcher::matches);
        boolean excluded = false;
        if (!included) {
            excluded = Stream.of(excludes).map(p -> p.matcher(checkPath)).anyMatch(Matcher::matches);
        }
        return included || !excluded;
    }

    public static Pattern[] compileFileMasks(List<String> masks) {
        if (masks == null) {
            return FileUtil.NO_PATTERNS;
        }
        return masks.stream().map(FileUtil::compileFileMask).toArray(Pattern[]::new);
    }

    private static final Pattern[] NO_PATTERNS = new Pattern[0];

    static Pattern compileFileMask(String mask) {
        StringBuilder m = new StringBuilder();
        // "Relative" masks can match at any directory level
        if (!mask.startsWith("/")) {
            mask = "**/" + mask;
        }
        // Masks ending with a slash match everything in subtree
        if (mask.endsWith("/")) {
            mask += "**";
        }
        for (int cp, i = 0; i < mask.length(); i += Character.charCount(cp)) {
            cp = mask.codePointAt(i);
            if (cp >= 'A' && cp <= 'Z') {
                m.appendCodePoint(cp);
            } else if (cp >= 'a' && cp <= 'z') {
                m.appendCodePoint(cp);
            } else if (cp >= '0' && cp <= '9') {
                m.appendCodePoint(cp);
            } else if (cp == '/') {
                if (mask.regionMatches(i, "/**/", 0, 4)) {
                    // The sequence /**/ matches *zero* or more levels
                    m.append("(?:/|/.*/)");
                    i += 3;
                } else if (mask.regionMatches(i, "/**", 0, 3)) {
                    // The sequence /** matches *zero* or more levels
                    m.append("(?:|/.*)");
                    i += 2;
                } else {
                    m.appendCodePoint(cp);
                }
            } else if (cp == '?') {
                // ? matches anything but a directory separator
                m.append("[^/]");
            } else if (cp == '*') {
                if (mask.regionMatches(i, "**/", 0, 3)) {
                    // The sequence **/ matches *zero* or more levels
                    m.append("(?:|.*/)");
                    i += 2;
                } else if (mask.regionMatches(i, "**", 0, 2)) {
                    // **
                    m.append(".*");
                    i++;
                } else {
                    // *
                    m.append("[^/]*");
                }
            } else {
                m.append('\\').appendCodePoint(cp);
            }
        }
        return Pattern.compile(m.toString());
    }

    /**
     * Given a list of paths, return a list of filenames (a la
     * {@code File.getName()}) plus the minimum number of parent path segments
     * required to make each filename unique within the result list. E.g.
     * <ul>
     * <li>{@code [foo/bar.txt, foo/baz.txt] -> [bar.txt, baz.txt]}
     * <li>{@code [foo/bar/baz.txt, foo/fop/baz.txt] -> [bar/baz.txt, fop/baz.txt]}
     * <li>{@code [foo/bar/baz/fop.txt, foo/buz/baz/fop.txt] -> [bar/baz/fop.txt, buz/baz/fop.txt]}
     * <li>{@code [foo.txt, foo.txt] -> [foo.txt, foo.txt]} (actual duplicates
     * are unmodified)
     * </ul>
     * Note that paths will be normalized (indirections removed, trailing and
     * duplicate separators removed, separators become {@code /}).
     *
     * @param paths
     *            A list of paths
     * @return A list of minimal unique paths
     */
    public static List<String> getUniqueNames(List<String> paths) {
        // Normalize
        List<String> fullPaths = new ArrayList<>(paths);
        fullPaths.replaceAll(
                p -> Optional.ofNullable(FilenameUtils.normalizeNoEndSeparator(p, true)).orElse(""));
        // Create working array with first attempt (one segment)
        List<String> working = new ArrayList<>(fullPaths);
        working.replaceAll(p -> StringUtil.getTailSegments(p, '/', 1));
        // Early out for normalizable singleton list
        if (working.size() == 1 && !working.get(0).isEmpty()) {
            return working;
        }
        // Note number of segments retained for each item
        int[] segments = new int[fullPaths.size()];
        Arrays.fill(segments, 1);
        while (true) {
            boolean didTrim = false;
            // Calculate counts to allow finding duplicates
            int[] counts = new int[working.size()];
            Arrays.setAll(counts, i -> Collections.frequency(working, working.get(i)));
            for (int i = 0; i < counts.length; i++) {
                if (counts[i] > 1) {
                    // Re-trim the duplicate with one extra segment
                    String curr = working.get(i);
                    String trimmed = StringUtil.getTailSegments(fullPaths.get(i), '/', ++segments[i]);
                    // Re-trimmed value only valid if distinct from previous
                    // value
                    if (!curr.equals(trimmed)) {
                        working.set(i, trimmed);
                        didTrim = true;
                    }
                }
            }
            if (!didTrim) {
                // Did no valid work on this iteration, so stop
                break;
            }
        }
        // Restore any un-normalizable paths
        for (int i = 0; i < working.size(); i++) {
            if (working.get(i).isEmpty()) {
                working.set(i, paths.get(i));
            }
        }
        return working;
    }

    /**
     * Comparator to sort the tm/ folder alphabetically, but always put
     * tm/enforce and tm/auto results before other similar % matches.
     */
    public static class TmFileComparator implements Comparator<String> {
        private static final String AUTO_PREFIX = OConsts.AUTO_TM + "/";
        private static final String ENFORCE_PREFIX = OConsts.AUTO_ENFORCE_TM + "/";

        public TmFileComparator(File tmRoot) {
            this.tmRoot = tmRoot;
        }

        private final File tmRoot;

        @Override
        public int compare(String n1, String n2) {
            String r1, r2;
            try {
                r1 = FileUtil.computeRelativePath(tmRoot, new File(n1));
                r2 = FileUtil.computeRelativePath(tmRoot, new File(n2));
            } catch (IOException e) {
                return n1.compareTo(n2);
            }
            int c;
            if (r1.startsWith(ENFORCE_PREFIX) && r2.startsWith(ENFORCE_PREFIX)) {
                c = n1.compareTo(n2);
            } else if (r1.startsWith(ENFORCE_PREFIX)) {
                c = -1;
            } else if (r2.startsWith(ENFORCE_PREFIX)) {
                c = 1;
            } else if (r1.startsWith(AUTO_PREFIX) && r2.startsWith(AUTO_PREFIX)) {
                c = n1.compareTo(n2);
            } else if (r1.startsWith(AUTO_PREFIX)) {
                c = -1;
            } else if (r2.startsWith(AUTO_PREFIX)) {
                c = 1;
            } else {
                c = n1.compareTo(n2);
            }
            return c;
        }
    }
}
