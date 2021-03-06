/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.server.cluster.plugin.kubernetes;

import org.apache.skywalking.oap.server.cluster.plugin.kubernetes.fixture.PlainWatch;
import org.apache.skywalking.oap.server.core.cluster.RemoteInstance;
import org.apache.skywalking.oap.server.core.remote.client.Address;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class KubernetesCoordinatorTest {

    private KubernetesCoordinator coordinator;

    @Test
    public void assertAdded() throws InterruptedException {
        PlainWatch watch = PlainWatch.create(2, "ADDED", "1", "10.0.0.1", "ADDED", "2", "10.0.0.2");
        coordinator = new KubernetesCoordinator(watch, () -> "1");
        coordinator.registerRemote(new RemoteInstance(new Address("0.0.0.0", 8454, true)));
        watch.await();
        assertThat(coordinator.queryRemoteNodes().size(), is(2));
        assertThat(coordinator.queryRemoteNodes().stream().filter(instance -> instance.getAddress().isSelf()).findFirst().get().getAddress().getHost(), is("10.0.0.1"));
    }

    @Test
    public void assertModified() throws InterruptedException {
        PlainWatch watch = PlainWatch.create(3, "ADDED", "1", "10.0.0.1", "ADDED", "2", "10.0.0.2", "MODIFIED", "1", "10.0.0.3");
        coordinator = new KubernetesCoordinator(watch, () -> "1");
        coordinator.registerRemote(new RemoteInstance(new Address("0.0.0.0", 8454, true)));
        watch.await();
        assertThat(coordinator.queryRemoteNodes().size(), is(2));
        assertThat(coordinator.queryRemoteNodes().stream().filter(instance -> instance.getAddress().isSelf()).findFirst().get().getAddress().getHost(), is("10.0.0.3"));
    }

    @Test
    public void assertDeleted() throws InterruptedException {
        PlainWatch watch = PlainWatch.create(3, "ADDED", "1", "10.0.0.1", "ADDED", "2", "10.0.0.2", "DELETED", "2", "10.0.0.2");
        coordinator = new KubernetesCoordinator(watch, () -> "1");
        coordinator.registerRemote(new RemoteInstance(new Address("0.0.0.0", 8454, true)));
        watch.await();
        assertThat(coordinator.queryRemoteNodes().size(), is(1));
        assertThat(coordinator.queryRemoteNodes().stream().filter(instance -> instance.getAddress().isSelf()).findFirst().get().getAddress().getHost(), is("10.0.0.1"));
    }

    @Test
    public void assertError() throws InterruptedException {
        PlainWatch watch = PlainWatch.create(3, "ADDED", "1", "10.0.0.1", "ERROR", "X", "10.0.0.2", "ADDED", "2", "10.0.0.2");
        coordinator = new KubernetesCoordinator(watch, () -> "1");
        coordinator.registerRemote(new RemoteInstance(new Address("0.0.0.0", 8454, true)));
        watch.await();
        assertThat(coordinator.queryRemoteNodes().size(), is(2));
        assertThat(coordinator.queryRemoteNodes().stream().filter(instance -> instance.getAddress().isSelf()).findFirst().get().getAddress().getHost(), is("10.0.0.1"));
    }
}