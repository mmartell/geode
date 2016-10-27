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
package org.apache.geode.management.internal.cli.help.format;

import org.apache.geode.internal.lang.StringUtils;
import org.apache.geode.management.internal.cli.GfshParser;
import org.apache.geode.management.internal.cli.shell.Gfsh;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class HelpBlock {
  String data = null;
  List<HelpBlock> children = new ArrayList<>();
  // indent level
  int level = -1;

  public HelpBlock() {}

  public HelpBlock(String data) {
    if (!StringUtils.isBlank(data)) {
      this.data = data;
      this.level = 0;
    }
  }

  public String getData() {
    return data;
  }

  public List<HelpBlock> getChildren() {
    return children;
  }

  public void addChild(HelpBlock helpBlock) {
    // before adding another block as the child, increment the indent level
    helpBlock.setLevel(level + 1);
    children.add(helpBlock);
  }

  // recursively set the indent level of the decendents
  public void setLevel(int level) {
    this.level = level;
    for (HelpBlock child : children) {
      child.setLevel(level + 1);
    }
  }

  public String toString() {
    StringBuffer builder = new StringBuffer();

    if (data != null) {
      builder.append(Gfsh.wrapText(data, level));
      builder.append(GfshParser.LINE_SEPARATOR);
    }
    for (HelpBlock child : children) {
      builder.append(child.toString());
    }
    return builder.toString();
  }
}
