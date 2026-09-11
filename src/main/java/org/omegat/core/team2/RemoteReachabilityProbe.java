/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.core.team2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import gen.core.project.RepositoryDefinition;

/**
 * Quick reachability check for the remote repositories of a team project: a
 * plain TCP connect with a short timeout per remote host, name resolution
 * capped by the same timeout. The auto-save thread uses it, outside the
 * project lock, to decide whether an offline project should attempt to
 * restore team synchronization; the full version control machinery is only
 * started once the hosts answer, so a dead network can never stall editing.
 * The probe connects directly: in an environment that reaches the remote
 * side only through a proxy it stays negative, and a manual save remains the
 * way to go back online there.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public final class RemoteReachabilityProbe {

    /** Connect timeout for the probe of one host. */
    static final int TIMEOUT_MS = 3000;

    /** Matches the scp-like git syntax with user, e.g. "git@host:path". */
    private static final Pattern SCP_LIKE_WITH_USER = Pattern
            .compile("^[^@/\\s]+@([A-Za-z0-9][A-Za-z0-9._\\-]+):.+");

    /**
     * Matches the scp-like git syntax without user, e.g. "host.tld:path". The
     * host needs a dot and the path must not look like one, so drive letters
     * and odd schemes stay out.
     */
    private static final Pattern SCP_LIKE_NO_USER = Pattern
            .compile("^([A-Za-z0-9][A-Za-z0-9_\\-]*(?:\\.[A-Za-z0-9._\\-]+)+):[^/\\\\].*");

    private RemoteReachabilityProbe() {
    }

    /**
     * Check with the default timeout, see {@link #canReachAll(List, int)}.
     */
    public static boolean canReachAll(List<RepositoryDefinition> repositories) {
        return canReachAll(repositories, TIMEOUT_MS);
    }

    /**
     * Check whether every remote host of the given repository definitions
     * accepts a TCP connection. URLs without a recognizable network host
     * (file URLs, local paths, unknown schemes) count as reachable: they
     * cannot hang on a dead network, and the sync itself is the authority on
     * whether they work.
     *
     * @param repositories
     *            repository definitions of the project
     * @param timeoutMs
     *            resolve and connect timeout per host
     * @return true when no probed host refused
     */
    public static boolean canReachAll(List<RepositoryDefinition> repositories, int timeoutMs) {
        return repositories.stream().map(RepositoryDefinition::getUrl).distinct()
                .allMatch(url -> canReach(url, timeoutMs));
    }

    static boolean canReach(@Nullable String url, int timeoutMs) {
        InetSocketAddress target = toSocketAddress(url);
        if (target == null) {
            return true;
        }
        InetSocketAddress resolved = resolve(target, timeoutMs);
        if (resolved == null || resolved.isUnresolved()) {
            return false;
        }
        try (Socket socket = new Socket()) {
            socket.connect(resolved, timeoutMs);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Resolve with a deadline: the platform resolver has no timeout of its
     * own and may block far longer than the probe should.
     */
    @Nullable
    private static InetSocketAddress resolve(InetSocketAddress target, int timeoutMs) {
        try {
            return CompletableFuture
                    .supplyAsync(() -> new InetSocketAddress(target.getHostString(), target.getPort()))
                    .get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException | TimeoutException ex) {
            return null;
        }
    }

    /**
     * Extract host and port to probe from a repository URL, null when the URL
     * carries no probeable network host. Pure parse: the returned address is
     * unresolved, name resolution happens only in the actual probe.
     */
    @Nullable
    static InetSocketAddress toSocketAddress(@Nullable String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String trimmed = url.trim();
        try {
            URI uri = new URI(trimmed);
            String scheme = uri.getScheme();
            if (scheme != null) {
                int schemePort = defaultPort(scheme);
                String host = uri.getHost();
                if (host != null) {
                    int port = uri.getPort() != -1 ? uri.getPort() : schemePort;
                    return port == -1 ? null : InetSocketAddress.createUnresolved(host, port);
                }
                // Hosts the URI class refuses (e.g. with underscores) are
                // still probeable through the raw authority.
                InetSocketAddress fromAuthority = fromAuthority(uri.getAuthority(), schemePort);
                if (fromAuthority != null) {
                    return fromAuthority;
                }
            }
        } catch (URISyntaxException ignored) {
            // not a URI - try the scp-like forms below
        }
        Matcher withUser = SCP_LIKE_WITH_USER.matcher(trimmed);
        if (withUser.matches()) {
            return InetSocketAddress.createUnresolved(withUser.group(1), 22);
        }
        Matcher noUser = SCP_LIKE_NO_USER.matcher(trimmed);
        if (noUser.matches()) {
            return InetSocketAddress.createUnresolved(noUser.group(1), 22);
        }
        return null;
    }

    @Nullable
    private static InetSocketAddress fromAuthority(@Nullable String authority, int defaultPort) {
        if (authority == null || authority.isEmpty() || defaultPort == -1 || authority.indexOf('[') >= 0) {
            return null;
        }
        String hostPort = authority.substring(authority.lastIndexOf('@') + 1);
        String host = hostPort;
        int port = defaultPort;
        int colon = hostPort.lastIndexOf(':');
        if (colon >= 0) {
            host = hostPort.substring(0, colon);
            try {
                port = Integer.parseInt(hostPort.substring(colon + 1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return host.isEmpty() ? null : InetSocketAddress.createUnresolved(host, port);
    }

    private static int defaultPort(String scheme) {
        switch (scheme.toLowerCase(Locale.ENGLISH)) {
        case "http":
            return 80;
        case "https":
            return 443;
        case "ssh":
        case "git+ssh":
        case "svn+ssh":
            return 22;
        case "git":
            return 9418;
        case "svn":
            return 3690;
        default:
            return -1;
        }
    }
}
