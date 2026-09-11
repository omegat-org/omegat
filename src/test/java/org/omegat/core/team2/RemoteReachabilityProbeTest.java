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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;

import org.junit.Test;

import gen.core.project.RepositoryDefinition;

/**
 * Tests for the reachability probe used by the team sync auto-reconnect.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class RemoteReachabilityProbeTest {

    @Test
    public void testDefaultPorts() {
        assertEquals(address("github.com", 443),
                RemoteReachabilityProbe.toSocketAddress("https://github.com/omegat-org/omegat.git"));
        assertEquals(address("example.org", 80),
                RemoteReachabilityProbe.toSocketAddress("http://example.org/repo"));
        assertEquals(address("example.org", 22),
                RemoteReachabilityProbe.toSocketAddress("ssh://git@example.org/repo.git"));
        assertEquals(address("example.org", 22),
                RemoteReachabilityProbe.toSocketAddress("git+ssh://example.org/repo.git"));
        assertEquals(address("example.org", 22),
                RemoteReachabilityProbe.toSocketAddress("svn+ssh://example.org/repo"));
        assertEquals(address("example.org", 9418),
                RemoteReachabilityProbe.toSocketAddress("git://example.org/repo.git"));
        assertEquals(address("example.org", 3690),
                RemoteReachabilityProbe.toSocketAddress("svn://example.org/repo"));
    }

    @Test
    public void testExplicitPortWins() {
        assertEquals(address("example.org", 8443),
                RemoteReachabilityProbe.toSocketAddress("https://example.org:8443/repo"));
    }

    @Test
    public void testScpLikeSyntax() {
        assertEquals(address("github.com", 22),
                RemoteReachabilityProbe.toSocketAddress("git@github.com:omegat-org/omegat.git"));
        // absolute path and no user are also valid scp-like forms
        assertEquals(address("github.com", 22),
                RemoteReachabilityProbe.toSocketAddress("git@github.com:/srv/omegat.git"));
        assertEquals(address("example.org", 22),
                RemoteReachabilityProbe.toSocketAddress("example.org:repos/project.git"));
    }

    @Test
    public void testHostTheUriClassRefuses() {
        // underscores make URI.getHost() return null; the authority still
        // names a probeable host
        assertEquals(address("host_x", 80),
                RemoteReachabilityProbe.toSocketAddress("http://host_x/repo"));
        assertEquals(address("host_x", 8080),
                RemoteReachabilityProbe.toSocketAddress("https://user@host_x:8080/repo"));
    }

    @Test
    public void testIpv6Literal() {
        assertEquals(address("[::1]", 8443),
                RemoteReachabilityProbe.toSocketAddress("https://[::1]:8443/repo"));
    }

    @Test
    public void testUppercaseScheme() {
        assertEquals(address("example.org", 443),
                RemoteReachabilityProbe.toSocketAddress("HTTPS://example.org/repo"));
    }

    @Test
    public void testLocalUrlsHaveNoHost() {
        assertNull(RemoteReachabilityProbe.toSocketAddress("file:///repos/project.git"));
        assertNull(RemoteReachabilityProbe.toSocketAddress("/repos/project.git"));
        assertNull(RemoteReachabilityProbe.toSocketAddress("C:\\repos\\project"));
        assertNull(RemoteReachabilityProbe.toSocketAddress("C:/repos/project"));
        assertNull(RemoteReachabilityProbe.toSocketAddress(null));
        assertNull(RemoteReachabilityProbe.toSocketAddress("  "));
        // unknown scheme: not probeable, counts as reachable by design
        assertNull(RemoteReachabilityProbe.toSocketAddress("ftp://example.org/repo"));
    }

    @Test
    public void testLocalUrlsCountAsReachable() {
        assertTrue(RemoteReachabilityProbe.canReach("file:///repos/project.git", 100));
        assertTrue(RemoteReachabilityProbe.canReach(null, 100));
    }

    @Test
    public void testUnresolvableHost() {
        // .invalid is reserved (RFC 2606) and never resolves
        assertFalse(RemoteReachabilityProbe.canReach("https://no-such-host.invalid/repo", 100));
    }

    @Test
    public void testReachableAndRefused() throws Exception {
        // Window between close and probe: another process could rebind the
        // port; accepted as negligible for an ephemeral port.
        int port;
        try (ServerSocket server = new ServerSocket(0)) {
            port = server.getLocalPort();
            assertTrue(RemoteReachabilityProbe.canReach("http://127.0.0.1:" + port + "/repo", 2000));
        }
        // the socket is closed now: same port refuses
        assertFalse(RemoteReachabilityProbe.canReach("http://127.0.0.1:" + port + "/repo", 2000));
    }

    @Test
    public void testCanReachAllProbesEveryUrl() throws Exception {
        RepositoryDefinition local = new RepositoryDefinition();
        local.setUrl("file:///repos/project.git");
        RepositoryDefinition dead;
        try (ServerSocket server = new ServerSocket(0)) {
            dead = new RepositoryDefinition();
            dead.setUrl("http://127.0.0.1:" + server.getLocalPort() + "/repo");
            assertTrue(RemoteReachabilityProbe.canReachAll(List.of(local, dead), 2000));
        }
        assertFalse(RemoteReachabilityProbe.canReachAll(List.of(local, dead), 2000));
    }

    private static InetSocketAddress address(String host, int port) {
        return InetSocketAddress.createUnresolved(host, port);
    }
}
