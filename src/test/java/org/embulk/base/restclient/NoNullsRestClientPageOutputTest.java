/*
 * Copyright 2017 The Embulk project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.embulk.base.restclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.embulk.EmbulkTestRuntime;
import org.embulk.base.restclient.OutputTestPluginDelegate.PluginTask;
import org.embulk.config.ConfigSource;
import org.embulk.config.TaskReport;
import org.embulk.config.TaskSource;
import org.embulk.spi.OutputPlugin;
import org.embulk.spi.Page;
import org.embulk.spi.PageTestUtils;
import org.embulk.spi.Schema;
import org.embulk.spi.TransactionalPageOutput;
import org.embulk.util.config.ConfigMapperFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class NoNullsRestClientPageOutputTest {
    @BeforeClass
    public static void initializeConstant() {
    }

    @Rule
    public EmbulkTestRuntime runtime = new EmbulkTestRuntime();

    private OutputTestUtils utils;
    private NoNullsOutputTestPlugin plugin;
    private ConfigMapperFactory configMapperFactory;

    @Before
    public void createResources() throws Exception {
        configMapperFactory = ConfigMapperFactory.builder().addDefaultModules().build();

        utils = new OutputTestUtils(configMapperFactory);
        utils.initializeConstant();
        // PluginTask task = configMapperFactory.createConfigMapper().map(utils.configJson(), PluginTask.class);

        plugin = new NoNullsOutputTestPlugin(configMapperFactory);
    }

    @Test
    public void testSomething() {
    }

    @Test
    public void testOutputWithNullValues() throws Exception {
        final ConfigSource config = this.utils.configJson();
        final Schema schema = this.utils.jsonSchema();
        final PluginTask task = configMapperFactory.createConfigMapper().map(config, PluginTask.class);
        this.plugin.transaction(config, schema, 0, new OutputPlugin.Control() {
                @Override
                public List<TaskReport> run(final TaskSource taskSource) {
                    final ArrayList<TaskReport> newList = new ArrayList<>();
                    newList.add(configMapperFactory.newTaskReport());
                    return newList;
                }
            });
        final TransactionalPageOutput output = this.plugin.open(task.toTaskSource(), schema, 0);

        // id, long, timestamp, boolean, double, string
        final List<Page> pages =
                PageTestUtils.buildPage(runtime.getBufferAllocator(), schema, 1L, null, null, null, null, null);
        assertThat(pages.size(), is(1));
        for (final Page page : pages) {
            output.add(page);
        }

        output.finish();
        output.commit();

        final ArrayList<OutputTestRecordBuffer> buffers = OutputTestPluginDelegate.getBuffers();

        final String res = buffers.get(0).toString();

        assertThat(res, is("[{\"id\":1}]"));
    }

    @Test
    public void testOutputWithRegularValues() throws Exception {
        final ConfigSource config = this.utils.configJson();
        final Schema schema = this.utils.jsonSchema();
        final PluginTask task = configMapperFactory.createConfigMapper().map(config, PluginTask.class);
        this.plugin.transaction(config, schema, 0, new OutputPlugin.Control() {
                @Override
                public List<TaskReport> run(final TaskSource taskSource) {
                    final ArrayList<TaskReport> newList = new ArrayList<>();
                    newList.add(configMapperFactory.newTaskReport());
                    return newList;
                }
            });
        final TransactionalPageOutput output = this.plugin.open(task.toTaskSource(), schema, 0);

        // id, long, timestamp, boolean, double, string
        final List<Page> pages = PageTestUtils.buildPage(
                runtime.getBufferAllocator(),
                schema,
                2L,
                42L,
                getTimestampFromInstant(Instant.ofEpochSecond(1509738161)),
                true,
                123.45,
                "embulk");
        assertThat(pages.size(), is(1));
        for (final Page page : pages) {
            output.add(page);
        }

        output.finish();
        output.commit();

        final ArrayList<OutputTestRecordBuffer> buffers = OutputTestPluginDelegate.getBuffers();

        final String res = buffers.get(0).toString();

        assertThat(res, is(
                "[{\"id\":2,\"long\":42,\"timestamp\":\"2017-11-03T19:42:41.000+0000\",\"boolean\":true,\"double\":123.45,\"string\":\"embulk\"}]"));
    }

    @SuppressWarnings("deprecation")  // org.embulk.spi.time.Timestamp
    private static org.embulk.spi.time.Timestamp getTimestampFromInstant(final Instant instant) {
        return org.embulk.spi.time.Timestamp.ofInstant(instant);
    }
}
