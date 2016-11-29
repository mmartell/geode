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

package org.apache.geode.security;

import static org.apache.geode.distributed.ConfigurationProperties.SECURITY_MANAGER;
import static org.apache.geode.distributed.ConfigurationProperties.SECURITY_POST_PROCESSOR;
import static org.apache.geode.security.AbstractSecureServerDUnitTest.createClientProperties;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.query.SelectResults;
import org.apache.geode.test.dunit.Host;
import org.apache.geode.test.dunit.VM;
import org.apache.geode.test.dunit.internal.JUnit4DistributedTestCase;
import org.apache.geode.test.dunit.rules.ServerStarterRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Properties;

public class FieldQueryPostProcessorDUnitTest extends JUnit4DistributedTestCase {
  private static VM client;

  @ClassRule
  public static ServerStarterRule serverStarter = new ServerStarterRule(new Properties(){{
    setProperty(SECURITY_MANAGER, SimpleTestSecurityManager.class.getName());
    setProperty(SECURITY_POST_PROCESSOR, InnerPostProcessor.class.getName());
  }});

  @BeforeClass
  public static void before(){
    Region region =
        serverStarter.cache.createRegionFactory(RegionShortcut.REPLICATE).create("customer");
    region.put("1", new Customer(1, "user", "123-456-7890"));
    region.put("2", new Customer(2, "admin", "122-455-7890"));

    Host host = Host.getHost(0);
    client = host.getVM(0);
  }
  @Test
  public void testQueryFieldAccess(){
    int serverPort = serverStarter.server.getPort();
    client.invoke(() -> {
      ClientCacheFactory cf = new ClientCacheFactory(createClientProperties("data", "data"));
      ClientCache cache = cf.setPoolSubscriptionEnabled(true)
          .addPoolServer("localhost", serverPort)
          .create();
      Region region = cache.createClientRegionFactory(ClientRegionShortcut.PROXY).create("customer");

      // post process for query
      String query = "select c.ssn from /customer c";
      SelectResults result = region.query(query);
      result.size();
    });
  }


  public static class InnerPostProcessor implements PostProcessor{

    private int count = 0;
    @Override
    public Object processRegionValue(Object principal, String regionName, Object key,
                                     Object value) {
      count++;
      return value;
    }

    public int getCount(){
      return count;
    }
  }

  public static class Customer {
    private String ssn;
    private String name;
    private int id;

    public Customer(int id, String name, String ssn) {
      this.id = id;
      this.ssn = ssn;
      this.name = name;
    }

    public String getSsn() {
      return ssn;
    }

    public void setSsn(String ssn) {
      this.ssn = ssn;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }
  }

}
