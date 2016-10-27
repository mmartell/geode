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
package org.apache.geode.management.internal.cli.help.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.geode.management.cli.CliMetaData;
import org.apache.geode.management.internal.cli.GfshParser;
import org.apache.geode.management.internal.cli.help.format.HelpBlock;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HelpUtils {
  public static final String EXE_PREFIX_FOR_EXTERNAL_HELP =
      org.apache.geode.management.internal.cli.shell.Gfsh.GFSH_APP_NAME + " ";
  public static final String HELP__COMMAND_AVAILABLE = "Available";
  public static final String HELP__COMMAND_NOTAVAILABLE = "Not Available";

  private static final String NAME_NAME = "NAME";
  private static final String SYNONYMS_NAME = "SYNONYMS";
  private static final String SYNOPSIS_NAME = "SYNOPSIS";
  private static final String SYNTAX_NAME = "SYNTAX";
  private static final String ARGUMENTS_NAME = "ARGUMENTS";
  private static final String OPTIONS_NAME = "PARAMETERS";
  private static final String IS_AVAILABLE_NAME = "IS AVAILABLE";
  private static final String MODES = "MODES";

  private static final String REQUIRED_SUB_NAME = "Required: ";
  private static final String DEFAULTVALUE_SUB_NAME = "Default value: ";
  private static final String SYNONYMS_SUB_NAME = "Synonyms: ";
  private static final String SPECIFIEDDEFAULTVALUE_SUB_NAME =
      "Default (if the parameter is specified without value): ";
  private static final String UNSPECIFIEDDEFAULTVALUE_VALUE_SUB_NAME =
      "Default (if the parameter is not specified): ";

  private static final String VALUE_FIELD = "value";
  private static final String TRUE_TOKEN = "true";
  private static final String FALSE_TOKEN = "false";


  public static CliOption getCliOption(Annotation[][] parameterAnnotations, int index) {
    Annotation[] annotations = parameterAnnotations[index];
    return getAnnotation(annotations, CliOption.class);
  }

  public static <T> T getAnnotation(Annotation[] annotations, Class<?> klass) {
    for (Annotation annotation : annotations) {
      if (klass.isAssignableFrom(annotation.getClass())) {
        return (T) annotation;
      }
    }
    return null;
  }

  public static HelpBlock getHelp(Method method) {
    CliCommand commandTarget = method.getDeclaredAnnotation(CliCommand.class);
    String commandName = commandTarget.value()[0];
    HelpBlock root = new HelpBlock();
    // First we will have the block for NAME of the command
    HelpBlock name = new HelpBlock(NAME_NAME);
    name.addChild(new HelpBlock(commandName));
    root.addChild(name);

    // Now add synonyms if any
    String[] allNames = commandTarget.value();
    if (allNames.length > 1) {
      HelpBlock synonyms = new HelpBlock(SYNONYMS_NAME);
      for (int i = 1; i < allNames.length; i++) {
        synonyms.addChild(new HelpBlock(allNames[i]));
      }
      root.addChild(synonyms);
    }

    // Now comes the turn to display synopsis if any
    if (!StringUtils.isBlank(commandTarget.help())) {
      HelpBlock synopsis = new HelpBlock(SYNOPSIS_NAME);
      synopsis.addChild(new HelpBlock(commandTarget.help()));
      root.addChild(synopsis);
    }

    // build a list of required options and optional options
    Annotation[][] annotations = method.getParameterAnnotations();
    Class<?>[] parameterTypes = method.getParameterTypes();
    // Now display the syntax for the command
    StringBuffer buffer = new StringBuffer();
    buffer.append(commandName);
    for (int i = 0; i < annotations.length; i++) {
      appendOption(buffer, annotations[i], parameterTypes[i]);
    }

    HelpBlock syntax = new HelpBlock(SYNTAX_NAME);
    syntax.addChild(new HelpBlock(buffer.toString()));
    root.addChild(syntax);

    // Detailed description of Options
    if (annotations.length > 0) {
      HelpBlock options = new HelpBlock(OPTIONS_NAME);
      for (int i = 0; i < annotations.length; i++) {
        CliOption cliOption = getCliOption(annotations, i);
        HelpBlock optionNode = new HelpBlock(getPrimaryKey(cliOption));
        String help = cliOption.help();
        optionNode.addChild(new HelpBlock((!StringUtils.isBlank(help) ? help : "")));
        if (getSynonyms(cliOption).size() > 0) {
          StringBuilder builder = new StringBuilder();
          for (String string : getSynonyms(cliOption)) {
            if (builder.length() > 0) {
              builder.append(",");
            }
            builder.append(string);
          }
          optionNode.addChild(new HelpBlock(SYNONYMS_SUB_NAME + builder.toString()));
        }
        optionNode.addChild(new HelpBlock(
            REQUIRED_SUB_NAME + ((cliOption.mandatory()) ? TRUE_TOKEN : FALSE_TOKEN)));
        if (!isNullOrBlank(cliOption.specifiedDefaultValue())) {
          optionNode.addChild(
              new HelpBlock(SPECIFIEDDEFAULTVALUE_SUB_NAME + cliOption.specifiedDefaultValue()));
        }
        if (!isNullOrBlank(cliOption.unspecifiedDefaultValue())) {
          optionNode.addChild(new HelpBlock(
              UNSPECIFIEDDEFAULTVALUE_VALUE_SUB_NAME + cliOption.unspecifiedDefaultValue()));
        }
        options.addChild(optionNode);
      }
      root.addChild(options);
    }
    return root;
  }

  /**
   * this checks if the option is mandatory, and if not, wraps [] around it.
   */
  private static void appendOption(StringBuffer buffer, Annotation[] annotations,
      Class<?> optionType) {
    CliOption cliOption = getAnnotation(annotations, CliOption.class);
    CliMetaData cliMetaData = getAnnotation(annotations, CliMetaData.class);
    String optionString = getOptionString(cliOption, cliMetaData, optionType);
    if (cliOption.mandatory()) {
      buffer.append(" ").append(optionString);
    } else {
      buffer.append(" [").append(optionString).append("]");
    }
  }

  /**
   * this builds the following format of strings: key (as in sh and help) --key=value --key(=value)?
   * (if has specifiedDefaultValue) --key=value(,value)* (if the value is a list)
   *
   * @return option string
   */
  private static String getOptionString(CliOption cliOption, CliMetaData cliMetaData,
      Class<?> optionType) {
    String key0 = cliOption.key()[0];
    if ("".equals(key0)) {
      return (cliOption.key()[1]);
    }
    StringBuffer buffer = new StringBuffer();
    buffer.append(GfshParser.LONG_OPTION_SPECIFIER).append(key0);

    boolean hasSpecifiedDefault = !isNullOrBlank(cliOption.specifiedDefaultValue());

    if (hasSpecifiedDefault) {
      buffer.append("(");
    }

    buffer.append(GfshParser.OPTION_VALUE_SPECIFIER).append(VALUE_FIELD);

    if (hasSpecifiedDefault) {
      buffer.append(")?");
    }

    if (isCollectionOrArrayType(optionType)) {
      String separator = ",";
      if (cliMetaData != null) {
        separator = cliMetaData.valueSeparator();
      }
      buffer.append("(").append(separator).append(VALUE_FIELD).append(")*");
    }

    return buffer.toString();
  }

  private static boolean isCollectionOrArrayType(Class<?> typeToCheck) {
    return typeToCheck != null
        && (typeToCheck.isArray() || Collection.class.isAssignableFrom(typeToCheck));
  }

  public static String getPrimaryKey(CliOption option) {
    String[] keys = option.key();
    if (keys.length == 0) {
      throw new RuntimeException("Invalid option keys");
    } else if ("".equals(keys[0])) {
      return keys[1];
    } else {
      return keys[0];
    }
  }

  public static List<String> getSynonyms(CliOption option) {
    List<String> synonyms = new ArrayList<>();
    String[] keys = option.key();
    if (keys.length < 2)
      return synonyms;
    // if the primary key is empty (like sh and help command), then there should be no synonyms.
    if ("".equals(keys[0]))
      return synonyms;

    for (int i = 1; i < keys.length; i++) {
      synonyms.add(keys[i]);
    }
    return synonyms;
  }

  public static boolean isNullOrBlank(String value) {
    return StringUtils.isBlank(value) || CliMetaData.ANNOTATION_NULL_VALUE.equals(value);
  }

}
