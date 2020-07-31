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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.embulk.EmbulkTestRuntime;
import org.embulk.base.restclient.OutputTestPluginDelegate.PluginTask;
import org.embulk.config.ConfigSource;
import org.embulk.config.TaskReport;
import org.embulk.config.TaskSource;
import org.embulk.spi.Exec;
import org.embulk.spi.OutputPlugin;
import org.embulk.spi.Page;
import org.embulk.spi.PageTestUtils;
import org.embulk.spi.Schema;
import org.embulk.spi.TransactionalPageOutput;
import org.embulk.spi.time.Timestamp;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class RestClientPageOutputTest {
    @BeforeClass
    public static void initializeConstant() {
    }

    @Rule
    public EmbulkTestRuntime runtime = new EmbulkTestRuntime();

    private OutputTestUtils utils;
    private OutputTestPlugin plugin;

    @Before
    public void createResources() throws Exception {
        this.utils = new OutputTestUtils();
        this.utils.initializeConstant();
        // PluginTask task = utils.configJson().loadConfig(PluginTask.class);

        this.plugin = new OutputTestPlugin();
    }

    @Test
    public void testSomething() {
    }

    @Test
    public void testOutputWithNullValues() throws Exception {
        final ConfigSource config = this.utils.configJson();
        final Schema schema = this.utils.jsonSchema();
        final PluginTask task = config.loadConfig(PluginTask.class);
        this.plugin.transaction(config, schema, 0, new OutputPlugin.Control() {
                @Override
                public List<TaskReport> run(final TaskSource taskSource) {
                    return Lists.newArrayList(Exec.newTaskReport());
                }
            });
        final TransactionalPageOutput output = this.plugin.open(task.dump(), schema, 0);

        // id, long, timestamp, boolean, double, string
        final List<Page> pages = PageTestUtils.buildPage(
                runtime.getBufferAllocator(), schema, 1L, null, null, null, null, null);
        assertThat(pages.size(), is(1));
        for (final Page page : pages) {
            output.add(page);
        }

        output.finish();
        output.commit();

        final ArrayList<OutputTestRecordBuffer> buffers = OutputTestPluginDelegate.getBuffers();

        final String res = buffers.get(0).toString();

        assertThat(res, is(
                "[{\"id\":1,\"long\":null,\"timestamp\":null,\"boolean\":null,\"double\":null,\"string\":null}]"));
    }

    @Test
    public void testOutputWithRegularValues() throws Exception {
        final ConfigSource config = this.utils.configJson();
        final Schema schema = this.utils.jsonSchema();
        final PluginTask task = config.loadConfig(PluginTask.class);
        this.plugin.transaction(config, schema, 0, new OutputPlugin.Control() {
                @Override
                public List<TaskReport> run(final TaskSource taskSource) {
                    return Lists.newArrayList(Exec.newTaskReport());
                }
            });
        final TransactionalPageOutput output = plugin.open(task.dump(), schema, 0);

        // id, long, timestamp, boolean, double, string
        final List<Page> pages = PageTestUtils.buildPage(
                runtime.getBufferAllocator(), schema, 2L, 42L, Timestamp.ofEpochSecond(1509738161), true, 123.45, "embulk");
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
}
