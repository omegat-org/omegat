/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021-2022 Hiroshi Miura, Thomas Wolf and others.
               This is ported from EGit (Apache-2.0)
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
package org.omegat.core.team2.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.GpgConfig;
import org.eclipse.jgit.lib.GpgSignature;
import org.eclipse.jgit.lib.GpgSignatureVerifier;
import org.eclipse.jgit.lib.GpgSignatureVerifierFactory;
import org.eclipse.jgit.lib.GpgSigner;
import org.eclipse.jgit.lib.ObjectBuilder;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.StringUtils;
import org.eclipse.jgit.util.SystemReader;
import org.eclipse.jgit.util.TemporaryBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;

public class GITExternalGpgSigner extends GpgSigner {

    private static final Logger LOGGER = LoggerFactory.getLogger(GITExternalGpgSigner.class);

    // A GPG environment variable name. We remove this environment variable when calling gpg.
    private static final String PINENTRY_USER_DATA = "PINENTRY_USER_DATA";

    // For sanity checking the returned signature.
    private static final byte[] SIGNATURE_START = "-----BEGIN PGP SIGNATURE-----".getBytes(StandardCharsets.US_ASCII);
    private static final PathScanner FROM_PATH = new PathScanner();

    // error message keys
    private static final String ExternalGpgSigner_processInterrupted = "GPG_EXTERNAL_SIGNER_PROCESS_INTERRUPTED";
    private static final String ExternalGpgSigner_processFailed = "GPG_EXTERNAL_SIGNER_PROCESS_FAILED";
    private static final String ExternalGpgSigner_bufferError = "GPG_EXTERNAL_SIGNER_BUFFER_ERROR";
    private static final String ExternalGpgSigner_environmentError = "GPG_EXTERNAL_SIGNER_ENVIRONMENT_ERROR";
    private static final String ExternalGpgSigner_noKeyFound = "GPG_EXTERNAL_SIGNER_NO_KEY_FOUND";
    private static final String ExternalGpgSigner_skipNotAccessiblePath =
            "GPG_EXTERNAL_SIGNER_SKIP_NOT_ACCESSIBLE_PATH";
    private static final String ExternalGpgSigner_cannotSearch = "GPG_EXTERNAL_SIGNER_CANNOT_SEARCH";
    private static final String ExternalGpgSigner_signingCanceled = "GPG_EXTERNAL_SIGNER_SIGNING_CANCELED";
    private static final String ExternalGpgSigner_noSignature = "GPG_EXTERNAL_SIGNER_NO_SIGNATURE";
    private static final String ExternalGpgSigner_gpgNotFound = "GPG_EXTERNAL_SIGNER_GPG_NOT_FOUND";

    private interface ResultHandler {
        void accept(TemporaryBuffer b) throws IOException, CanceledException;
    }

    private static void runProcess(final ProcessBuilder process, final InputStream in,
                                   final ResultHandler stdout, final ResultHandler stderr)
            throws IOException, CanceledException {
        String command = String.join(" ", process.command());
        FS.ExecutionResult result = null;
        int code = 0;
        try {
            result = FS.DETECTED.execute(process, in);
            code = result.getRc();
            if (code != 0) {
                if (stderr != null) {
                    stderr.accept(result.getStderr());
                }
                throw new IOException(
                        MessageFormat.format(
                                OStrings.getString(ExternalGpgSigner_processFailed),
                                command, code + ": "
                                        + toString(result.getStderr())));
            }
            stdout.accept(result.getStdout());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(MessageFormat
                    .format(OStrings.getString(ExternalGpgSigner_processInterrupted),
                            command),
                    e);
        } catch (IOException e) {
            if (code != 0) {
                throw e;
            }
            if (result != null) {
                throw new IOException(
                        MessageFormat.format(
                                OStrings.getString(ExternalGpgSigner_processFailed),
                                command, toString(result.getStderr())),
                        e);
            }
            throw new IOException(
                    MessageFormat.format(
                            OStrings.getString(ExternalGpgSigner_processFailed),
                            command, e.getLocalizedMessage()),
                    e);
        } finally {
            if (result != null) {
                if (result.getStderr() != null) {
                    result.getStderr().destroy();
                }
                if (result.getStdout() != null) {
                    result.getStdout().destroy();
                }
            }
        }
    }

    private static String toString(final TemporaryBuffer b) {
        if (b != null) {
            try {
                return new String(b.toByteArray(4000),
                        Charset.defaultCharset());
            } catch (IOException ex) {
                Log.deco(LOGGER.atError()).setCause(ex).setMessageRB(ExternalGpgSigner_bufferError).log();
            }
        }
        return "";
    }

    /**
     * Signs the specified commit.
     *
     * <p>
     * Implementors should obtain the payload for signing from the specified
     * commit via {@link CommitBuilder#build()} and create a proper
     * {@link GpgSignature}. The generated signature must be set on the
     * specified {@code commit} (see
     * {@link CommitBuilder#setGpgSignature(GpgSignature)}).
     * </p>
     * <p>
     * Any existing signature on the commit must be discarded prior obtaining
     * the payload via {@link CommitBuilder#build()}.
     * </p>
     *
     * @param commit              the commit to sign (must not be <code>null</code> and must be
     *                            complete to allow proper calculation of payload)
     * @param gpgSigningKey       the signing key to locate (passed as is to the GPG signing
     *                            tool as is; eg., value of <code>user.signingkey</code>)
     * @param committer           the signing identity (to help with key lookup in case signing
     *                            key is not specified)
     * @param credentialsProvider provider to use when querying for signing key credentials (eg.
     *                            passphrase)
     * @throws CanceledException when signing was canceled (eg., user aborted when entering
     *                           passphrase)
     */
    @Override
    public void sign(final CommitBuilder commit, final String gpgSigningKey,
                     final PersonIdent committer,
                     final CredentialsProvider credentialsProvider) throws CanceledException {
        signObject(commit, gpgSigningKey, committer, credentialsProvider);
    }

    private void signObject(final ObjectBuilder object, final String gpgSigningKey,
                            final PersonIdent committer, final CredentialsProvider credentialsProvider)
            throws CanceledException {
        // Ignore the CredentialsProvider. We let GPG handle all this.
        try {
            String keySpec = gpgSigningKey;
            if (StringUtils.isEmptyOrNull(keySpec)) {
                // fallback
                if (committer == null) {
                    throw new CanceledException("Cannot determine signature key");
                }
                keySpec = '<' + committer.getEmailAddress() + '>';
            }
            // git config gpg.program
            // Use this custom program instead of "gpg" found on $PATH when making or verifying a PGP signature.
            GpgConfig config = new GpgConfig(new Config());
            String program = config.getProgram();
            object.setGpgSignature(new GpgSignature(signWithGpg(object.build(), keySpec, program)));
        } catch (IOException e) {
            throw new JGitInternalException(e.getMessage(), e);
        }
    }

    /**
     * Indicates if a signing key is available for the specified committer
     * and/or signing key.
     *
     * @param gpgSigningKey       the signing key to locate (passed as is to the GPG signing
     *                            tool as is; eg., value of <code>user.signingkey</code>)
     * @param committer           the signing identity (to help with key lookup in case signing
     *                            key is not specified)
     * @param credentialsProvider provider to use when querying for signing key credentials (eg.
     *                            passphrase)
     * @return <code>true</code> if a signing key is available,
     * <code>false</code> otherwise
     * @throws CanceledException when signing was canceled (eg., user aborted when entering
     *                           passphrase)
     */
    @Override
    public boolean canLocateSigningKey(final String gpgSigningKey, final PersonIdent committer,
                                       final CredentialsProvider credentialsProvider) throws CanceledException {
        // Ignore the CredentialsProvider. We let GPG handle all this.
        String program = FROM_PATH.getGpg();
        if (StringUtils.isEmptyOrNull(program)) {
            return false;
        }
        String keySpec = gpgSigningKey;
        if (StringUtils.isEmptyOrNull(keySpec)) {
            keySpec = '<' + committer.getEmailAddress() + '>';
        }
        ProcessBuilder process = new ProcessBuilder();
        // For the output format, see
        // https://github.com/gpg/gnupg/blob/master/doc/DETAILS
        process.command(program, "--locate-keys", //$NON-NLS-1$
                "--with-colons", //$NON-NLS-1$
                "--batch", //$NON-NLS-1$
                "--no-tty", //$NON-NLS-1$
                keySpec);
        gpgEnvironment(process);
        try {
            boolean[] result = { false };
            runProcess(process, null, b -> {
                try (BufferedReader r = new BufferedReader(
                        new InputStreamReader(b.openInputStream(),
                                StandardCharsets.UTF_8))) {
                    // --with-colons always writes UTF-8
                    boolean keyFound = false;
                    String line;
                    while ((line = r.readLine()) != null) {
                        if (line.startsWith("pub:") //$NON-NLS-1$
                                || line.startsWith("sub:")) { //$NON-NLS-1$
                            String[] fields = line.split(":"); //$NON-NLS-1$
                            if (fields.length > 11
                                    && fields[11].indexOf('s') >= 0) {
                                // It's a signing key.
                                keyFound = true;
                                break;
                            }
                        }
                    }
                    result[0] = keyFound;
                }
            }, null);
            if (!result[0]) {
                if (!StringUtils.isEmptyOrNull(gpgSigningKey)) {
                    Log.deco(LOGGER.atWarn()).setMessageRB(ExternalGpgSigner_noKeyFound)
                            .addArgument(gpgSigningKey).log();
                }
            }
            return result[0];
        } catch (IOException e) {
            Log.log(e);
            return false;
        }
    }

    private byte[] signWithGpg(byte[] data, String keySpec, String gpgProgram)
            throws IOException, CanceledException {
        // Sign an object with an external GPG executable. GPG handles
        // passphrase entry, including gpg-agent and native keychain
        // integration.
        String program = gpgProgram;
        if (StringUtils.isEmptyOrNull(program)) {
            program = FROM_PATH.getGpg();
            if (StringUtils.isEmptyOrNull(program)) {
                throw new IOException(OStrings.getString(ExternalGpgSigner_gpgNotFound));
            }
        }
        ProcessBuilder process = new ProcessBuilder();
        process.command(program,
                // Detached signature, sign, armor, user
                "-bsau", //$NON-NLS-1$
                keySpec,
                // No extra output
                "--batch", //$NON-NLS-1$
                "--no-tty", //$NON-NLS-1$
                // Write extra status messages to stderr
                "--status-fd", //$NON-NLS-1$
                "2", //$NON-NLS-1$
                // Force output of the signature to stdout
                "--output", //$NON-NLS-1$
                "-"); //$NON-NLS-1$
        gpgEnvironment(process);
        try (ByteArrayInputStream dataIn = new ByteArrayInputStream(data)) {
            class Holder {
                byte[] rawData;
            }
            Holder result = new Holder();
            runProcess(process, dataIn, b -> {
                // Sanity check: do we have a signature?
                GpgSignatureVerifierFactory factory = GpgSignatureVerifierFactory
                        .getDefault();
                boolean isValid;
                if (factory == null) {
                    byte[] fromGpg = b.toByteArray(SIGNATURE_START.length);
                    isValid = Arrays.equals(fromGpg, SIGNATURE_START);
                    if (isValid) {
                        result.rawData = b.toByteArray();
                    }
                } else {
                    byte[] fromGpg = b.toByteArray();
                    GpgSignatureVerifier verifier = factory.getVerifier();
                    try {
                        GpgSignatureVerifier.SignatureVerification verification = verifier
                                .verify(data, fromGpg);
                        isValid = verification != null
                                && verification.getVerified();
                        if (isValid) {
                            result.rawData = fromGpg;
                        }
                    } catch (JGitInternalException e) {
                        throw new IOException(e.getLocalizedMessage(), e);
                    } finally {
                        verifier.clear();
                    }
                }
                if (!isValid) {
                    throw new IOException(MessageFormat.format(
                            OStrings.getString(ExternalGpgSigner_noSignature),
                            toString(b)));
                }
            }, e -> {
                // Error handling: parse stderr to figure out whether we have a
                // cancellation. Unfortunately, GPG does record cancellation not
                // via a [GNUPG:] stable status but by printing "gpg: signing
                // failed: Operation cancelled". Since we don't know whether
                // this string is stable (or may even be localized), we check
                // for a "[GNUPG:] PINENTRY_LAUNCHED" line followed by the next
                // [GNUPG:] line being "[GNUPG:] FAILURE sign".
                //
                // The [GNUPG:] strings are part of GPG's public API. See
                // https://github.com/gpg/gnupg/blob/master/doc/DETAILS
                try (BufferedReader r = new BufferedReader(
                        new InputStreamReader(e.openInputStream(),
                                StandardCharsets.UTF_8))) {
                    String line;
                    boolean pinentry = false;
                    while ((line = r.readLine()) != null) {
                        if (!pinentry && line
                                .startsWith("[GNUPG:] PINENTRY_LAUNCHED")) {
                            pinentry = true;
                        } else if (pinentry) {
                            if (line.startsWith("[GNUPG:] FAILURE sign")) {
                                throw new CanceledException(OStrings.getString(ExternalGpgSigner_signingCanceled));
                            }
                            if (line.startsWith("[GNUPG:]")) {
                                pinentry = false;
                            }
                        }
                    }
                } catch (IOException ex) {
                    // Swallow it here; runProcess will raise one anyway.
                }
            });
            return result.rawData;
        }
    }

    private void gpgEnvironment(ProcessBuilder process) {
        try {
            Map<String, String> childEnv = process.environment();
            // The map is "typically case sensitive on all platforms", whatever
            // that means. "Typically"? Or really on all platforms? It would
            // make more sense if it were case-insensitive on Windows.
            //
            // Remove the PINENTRY_USER_DATA variable. On Linux, some people use
            // this sometimes in combination with a custom script configured as
            // the gpg-agent's pinentry-program to force pinentry-tty or
            // pinentry-curses to be used when in a shell, and a graphical
            // prompt otherwise. When Eclipse gets started from the shell, it
            // may inherit that environment variable, but when it calls gpg, it
            // needs a graphical pinentry. So remove this variable.
            //
            // If the variable is not set, a well-written custom pinentry script
            // should fall back to the default gpg pinentry which _is_ a
            // graphical one (pinentry-mac on Mac, pinentry-qt on Windows,
            // pinentry-gtk or pinentry-gnome or similar on Linux). Not sure if
            // this PINENTRY_USER_DATA method is still needed or used with
            // modern gpg; at least pinentry-gtk and pinentry-gnome should fall
            // back to prompting on the terminal if $DISPLAY of the calling
            // process is not set.
            String value = childEnv.get(PINENTRY_USER_DATA);
            if (!StringUtils.isEmptyOrNull(value)) {
                childEnv.remove(PINENTRY_USER_DATA);
            }
        } catch (SecurityException | UnsupportedOperationException
                | IllegalArgumentException ex) {
            Log.deco(LOGGER.atError()).setCause(ex).setMessageRB(ExternalGpgSigner_environmentError)
                    .log();
        }
    }

    private static class PathScanner {

        private String gpg;

        synchronized String getGpg() {
            if (gpg == null) {
                gpg = findGpg();
            }
            return gpg.isEmpty() ? null : gpg;
        }

        private static String findGpg() {
            SystemReader system = SystemReader.getInstance();
            String path = system.getenv("PATH"); //$NON-NLS-1$
            String exe = null;
            if (Platform.isMacOSX()) {
                // On Mac, $PATH is typically much shorter in programs launched
                // from the graphical UI than in the shell. Use the shell $PATH
                // first.
                String bash = searchPath(path, "bash"); //$NON-NLS-1$
                if (bash != null) {
                    ProcessBuilder process = new ProcessBuilder();
                    process.command(bash, "--login", "-c", "which gpg"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    process.directory(FS.DETECTED.userHome());
                    String[] result = { null };
                    try {
                        runProcess(process, null, b -> {
                            try (BufferedReader r = new BufferedReader(
                                    new InputStreamReader(b.openInputStream(),
                                            Charset.defaultCharset()))) {
                                result[0] = r.readLine();
                            }
                        }, null);
                    } catch (IOException | CanceledException ex) {
                        Log.deco(LOGGER.atError()).setMessageRB(ExternalGpgSigner_cannotSearch)
                                .setCause(ex).log();
                    }
                    exe = result[0];
                }
            }
            if (exe == null) {
                exe = searchPath(path, system.isWindows() ? "gpg.exe" : "gpg"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return exe == null ? "" : exe; //$NON-NLS-1$
        }

        private static String searchPath(String path, String name) {
            if (StringUtils.isEmptyOrNull(path)) {
                return null;
            }
            for (String p : path.split(File.pathSeparator)) {
                File exe = new File(p, name);
                try {
                    if (exe.isFile() && exe.canExecute()) {
                        return exe.getAbsolutePath();
                    }
                } catch (SecurityException e) {
                    Log.deco(LOGGER.atError()).setCause(e).setMessageRB(ExternalGpgSigner_skipNotAccessiblePath)
                            .addArgument(exe.getPath()).log();
                }
            }
            return null;
        }
    }
}
