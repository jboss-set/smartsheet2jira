/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2024, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.set.smartsheet2jira.syncfixversion;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import org.jboss.set.aphrodite.issue.trackers.jira.auth.BearerHttpAuthenticationHandler;

import java.net.URI;
import java.net.URISyntaxException;

public class ListFixVersions {
    public static void main(final String[] args) throws URISyntaxException {
        final String token = args[0];
        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final URI uri = new URI("https://issues.redhat.com");
        final JiraRestClient client = factory.createWithAuthenticationHandler(uri, new BearerHttpAuthenticationHandler(token));
        for (Version version : client.getProjectClient().getProject("JBEAP").claim().getVersions()) {
            System.out.println(version.getName() + " - " + version.getReleaseDate());
        }
    }
}
