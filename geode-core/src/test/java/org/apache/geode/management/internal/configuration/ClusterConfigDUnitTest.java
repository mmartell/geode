/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.management.internal.configuration;

import org.apache.geode.test.dunit.internal.JUnit4DistributedTestCase;
import org.apache.geode.test.dunit.rules.LocatorServerStartupRule;
import org.apache.geode.test.dunit.rules.ServerStarterRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Properties;

public class ClusterConfigDUnitTest extends JUnit4DistributedTestCase {
  @Rule
  public LocatorServerStartupRule lsRule = new LocatorServerStartupRule();

  @Test
  public void test() throws Exception {
    Properties locatorProps = new Properties();
    //locatorProps.setProperty(LOAD_CLUSTER_CONFIGURATION_FROM_DIR, "true");
    lsRule.getLocatorVM(0, locatorProps);

    ServerStarterRule server = new ServerStarterRule(new Properties());
    server.startServer(lsRule.getPort(0));
  }
}
