/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.management.internal.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.geode.management.internal.cli.i18n.CliStrings;
import org.apache.geode.test.junit.categories.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.shell.core.Completion;
import org.springframework.shell.event.ParseResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Category(IntegrationTest.class)
public class GfshParserIntegrationTest {
  private GfshParser parser;
  private List<Completion> candidates;
  private String buffer;
  private int cursor;

  @Before
  public void setUp() throws Exception {
    CommandManager.clearInstance();
    this.parser = GfshParser.getInstance();
    this.candidates = new ArrayList<>();
  }

  @After
  public void tearDown() {
    CommandManager.clearInstance();
  }

  private Map<String, String> params(String input, String commandName, String commandMethod) {
    ParseResult parseResult = parser.parse(input);

    GfshParseResult gfshParseResult = (GfshParseResult) parseResult;
    Map<String, String> params = gfshParseResult.getParamValueStrings();
    for (String param : params.keySet()) {
      System.out.println(param + "=" + params.get(param));
    }

    assertThat(gfshParseResult.getMethod().getName()).isEqualTo(commandMethod);
    assertThat(gfshParseResult.getUserInput()).isEqualTo(input.trim());

    return params;
  }

  @Test
  public void getSimpleParserInputTest() {
    buffer = "start locator  --J=\"-Dgemfire.http-service-port=8080\" --name=loc1";
    assertEquals("start locator --J \"-Dgemfire.http-service-port=8080\" --name loc1",
        GfshParser.convertToSimpleParserInput(buffer));

    buffer = "start locator --J=-Dgemfire.http-service-port=8080 --name=loc1 --J=-Ddummythinghere";
    assertEquals(
        "start locator --J \"-Dgemfire.http-service-port=8080,-Ddummythinghere\" --name loc1",
        GfshParser.convertToSimpleParserInput(buffer));

    buffer = "start locator --";
    assertEquals("start locator --", GfshParser.convertToSimpleParserInput(buffer));

    buffer =
        "start locator --J=-Dgemfire.http-service-port=8080 --name=loc1 --J=-Ddummythinghere --";
    assertEquals(
        "start locator --J \"-Dgemfire.http-service-port=8080,-Ddummythinghere\" --name loc1 --",
        GfshParser.convertToSimpleParserInput(buffer));

    buffer = "start server --name=name1 --locators=localhost --J=-Dfoo=bar";
    assertEquals("start server --name name1 --locators localhost --J \"-Dfoo=bar\"",
        GfshParser.convertToSimpleParserInput(buffer));
  }

  @Test
  public void optionStartsWithHyphenWithoutQuotes() throws Exception {
    String input =
        "rebalance --exclude-region=/GemfireDataCommandsDUnitTestRegion2 --simulate=true --time-out=-1";
    Map<String, String> params = params(input, "rebalance", "rebalance");

    assertThat(params.get("exclude-region")).isEqualTo("/GemfireDataCommandsDUnitTestRegion2");
    assertThat(params.get("simulate")).isEqualTo("true");
    assertThat(params.get("time-out")).isEqualTo("-1");
  }

  @Test
  public void optionStartsWithHyphenWithQuotes() throws Exception {
    String input =
        "rebalance --exclude-region=/GemfireDataCommandsDUnitTestRegion2 --simulate=true --time-out=\"-1\"";
    Map<String, String> params = params(input, "rebalance", "rebalance");

    assertThat(params.get("exclude-region")).isEqualTo("/GemfireDataCommandsDUnitTestRegion2");
    assertThat(params.get("simulate")).isEqualTo("true");
    assertThat(params.get("time-out")).isEqualTo("-1");
  }

  @Test
  public void optionContainingHyphen() throws Exception {
    String input = "rebalance --exclude-region=/The-Region --simulate=true";
    Map<String, String> params = params(input, "rebalance", "rebalance");

    assertThat(params.get("exclude-region")).isEqualTo("/The-Region");
    assertThat(params.get("simulate")).isEqualTo("true");
  }

  @Test
  public void optionContainingUnderscore() throws Exception {
    String input = "rebalance --exclude-region=/The_region --simulate=true";
    Map<String, String> params = params(input, "rebalance", "rebalance");

    assertThat(params.get("exclude-region")).isEqualTo("/The_region");
    assertThat(params.get("simulate")).isEqualTo("true");
  }

  @Test
  public void oneJOptionWithQuotes() throws Exception {
    String input = "start locator  --J=\"-Dgemfire.http-service-port=8080\" --name=loc1";
    Map<String, String> params = params(input, "start locator", "startLocator");

    assertThat(params.get("name")).isEqualTo("loc1");
    assertThat(params.get("J")).isEqualTo("-Dgemfire.http-service-port=8080");
  }

  @Test
  public void oneJOptionWithSpaceInQuotes() throws Exception {
    String input = "start locator  --J=\"-Dgemfire.http-service-port= 8080\" --name=loc1";
    Map<String, String> params = params(input, "start locator", "startLocator");

    assertThat(params.get("name")).isEqualTo("loc1");
    assertThat(params.get("J")).isEqualTo("-Dgemfire.http-service-port= 8080");
  }

  @Test
  public void oneJOption() throws Exception {
    String input = "start locator --J=-Dgemfire.http-service-port=8080 --name=loc1";
    Map<String, String> params = params(input, "start locator", "startLocator");

    assertThat(params.get("name")).isEqualTo("loc1");
    assertThat(params.get("J")).isEqualTo("-Dgemfire.http-service-port=8080");
  }

  @Test
  public void twoJOptions() throws Exception {
    String input =
        "start locator --J=-Dgemfire.http-service-port=8080 --name=loc1 --J=-Ddummythinghere";
    Map<String, String> params = params(input, "start locator", "startLocator");

    assertThat(params.get("name")).isEqualTo("loc1");
    assertThat(params.get("J")).isEqualTo("-Dgemfire.http-service-port=8080,-Ddummythinghere");
  }

  @Test
  public void twoJOptionsOneWithQuotesOneWithout() throws Exception {
    String input =
        "start locator --J=\"-Dgemfire.http-service-port=8080\" --name=loc1 --J=-Ddummythinghere";
    Map<String, String> params = params(input, "start locator", "startLocator");

    assertThat(params.get("name")).isEqualTo("loc1");
    assertThat(params.get("J")).isEqualTo("-Dgemfire.http-service-port=8080,-Ddummythinghere");
  }

  @Test
  public void oneJOptionWithQuotesAndLotsOfSpaces() throws Exception {
    String input =
        "start locator       --J=\"-Dgemfire.http-service-port=8080\"      --name=loc1         ";
    Map<String, String> params = params(input, "start locator", "startLocator");

    assertThat(params.get("name")).isEqualTo("loc1");
    assertThat(params.get("J")).isEqualTo("-Dgemfire.http-service-port=8080");
  }

  @Test
  public void testCompleteWithRequiredOption() throws Exception {
    candidates = new ArrayList<>();
    buffer = "start server";
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals(1, candidates.size());
    assertEquals("start server --name", getCompleted(buffer, cursor, candidates.get(0)));
  }

  @Test
  public void testCompleteWithRequiredOption1() throws Exception {
    candidates = new ArrayList<>();
    buffer = "start server ";
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals(1, candidates.size());
    assertEquals("start server --name", getCompleted(buffer, cursor, candidates.get(0)));
  }

  @Test
  public void testCompleteCommand() throws Exception {
    buffer = "start ser";
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals(1, candidates.size());
    assertEquals("start server", getCompleted(buffer, cursor, candidates.get(0)));
  }

  @Test
  public void testCompleteCommand2() throws Exception {
    buffer = "start server --name=jinmei --loc";
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals(3, candidates.size());
    assertTrue(candidates.contains(new Completion("--locators")));
  }

  @Test
  public void testComplete1() throws Exception {
    buffer = "start ";
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals(0, cursor);
    assertEquals(8, candidates.size());
    assertTrue(candidates.contains(new Completion("start server")));
  }

  @Test
  public void testComplete2() throws Exception {
    buffer = "start";
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals(0, cursor);
    assertEquals(8, candidates.size());
    assertTrue(candidates.contains(new Completion("start server")));
  }

  @Test
  public void testComplete8() throws Exception {
    buffer = "start server --name=name1 --se";
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals("start server --name=name1 ".length(), cursor);
    assertEquals(3, candidates.size());
    assertTrue(candidates.contains(new Completion("--server-port")));
  }

  @Test
  public void testComplete8WithExtraSpace() throws Exception {
    buffer = "start server --name=name1  --se";
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals("start server --name=name1  ".length(), cursor);
    assertEquals(3, candidates.size());
    assertTrue(candidates.contains(new Completion("--server-port")));
  }

  @Test
  public void testComplete3() throws Exception {
    buffer = "start server --name=name1 --";
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals(26, cursor);
    assertEquals(50, candidates.size());
    assertTrue(candidates.contains(new Completion("--properties-file")));
  }

  @Test
  public void testComplete4() throws Exception {
    buffer = "start server --name=name1 ";
    // if there is no more required options, the parser won't display more options unless you typed
    // --
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals("start server --name=name1 ".length(), cursor);
    assertEquals(50, candidates.size());
    assertTrue(candidates.contains(new Completion("--properties-file")));

  }

  @Test
  public void testCompleteJ4() throws Exception {
    buffer = "start server --name=name1 --J=";
    // if there is no more required options, the parser won't display more options unless you typed
    // --
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals("start server --name=name1 --J=".length(), cursor);
    assertEquals(0, candidates.size());
  }

  @Test
  public void testComplete5() throws Exception {
    buffer = "start server --name=name1";
    // if there is no more required options, the parser won't display more options unless you typed
    // --
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals(buffer.length(), cursor);
    assertEquals(50, candidates.size());
    assertTrue(candidates.contains(new Completion("--properties-file")));
  }

  @Test
  public void testComplete6() throws Exception {
    buffer = "start server --name=name1 --J";
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals(buffer.length(), cursor);
    assertEquals(0, candidates.size());
  }

  @Test
  public void testComplete9() throws Exception {
    buffer = "start server --name=name1 --J=-Dfoo.bar --";
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals(49, candidates.size());
  }

  @Test
  public void testComplete10() throws Exception {
    buffer = "start server --name=name1 --J=-Dme=her --J=-Dfoo=bar --l";
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals("start server --name=name1 --J=-Dme=her --J=-Dfoo=bar ".length(), cursor);
    assertEquals(4, candidates.size());
    assertTrue(candidates.contains(new Completion("--locators")));
  }

  @Test
  public void testMultiJComplete() throws Exception {
    buffer = "start server --name=name1 --J=-Dtest=test1 --J=-Dfoo=bar";
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals(buffer.length(), cursor);
    assertEquals(49, candidates.size());
    assertTrue(candidates.contains(new Completion("--properties-file")));
  }

  @Test
  public void testMultiJComplete2() throws Exception {
    buffer = "start server --J=-Dtest=test1 --J=-Dfoo=bar --name=name1";
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals(buffer.length(), cursor);
    assertEquals(49, candidates.size());
    assertTrue(candidates.contains(new Completion("--properties-file")));
  }

  @Test
  public void testJComplete3() throws Exception {
    buffer = "start server --name=name1 --locators=localhost --J=-Dfoo=bar";
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals(buffer.length(), cursor);
    assertEquals(48, candidates.size());
  }

  @Test
  public void testJComplete4() throws Exception {
    buffer = "start server --name=name1 --locators=localhost  --J=-Dfoo=bar --";
    cursor = parser.completeAdvanced(buffer, 0, candidates);
    assertEquals("start server --name=name1 --locators=localhost  --J=-Dfoo=bar ".length(), cursor);
    assertEquals(48, candidates.size());
  }


  @Test
  public void testObtainHelp() {
    String command = CliStrings.START_PULSE;
    String helpString = "NAME\n" + "start pulse\n" + "SYNOPSIS\n"
        + "Open a new window in the default Web browser with the URL for the Pulse application.\n"
        + "SYNTAX\n" + "start pulse [--url=value]\n" + "PARAMETERS\n" + "url\n"
        + "URL of the Pulse Web application.\n" + "Required: false\n"
        + "Default (if the parameter is not specified): http://localhost:7070/pulse\n";
    assertEquals(helpString, parser.getHelp(command));
  }

  @Test
  public void testGetHelp() {
    parser.obtainHelp(CliStrings.ALTER_DISK_STORE);
  }

  private String getCompleted(String buffer, int cursor, Completion completed) {
    return buffer.substring(0, cursor) + completed.getValue();
  }

}
