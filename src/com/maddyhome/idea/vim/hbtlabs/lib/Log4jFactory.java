/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2020 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.maddyhome.idea.vim.hbtlabs.lib;


import com.intellij.openapi.diagnostic.Log4jBasedLogger;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author nik
 */
public class Log4jFactory implements com.intellij.openapi.diagnostic.Logger.Factory {
  private final RollingFileAppender myAppender;
  private final List<String> myCategoriesWithDebugLevel;

  public Log4jFactory(File logFile, String categoriesWithDebugLevel) throws IOException {
    myCategoriesWithDebugLevel = categoriesWithDebugLevel.isEmpty() ? Collections.<String>emptyList() : Arrays.asList(categoriesWithDebugLevel.split(","));
    PatternLayout pattern = new PatternLayout("%d [%7r] %6p - %30.30c - %m\n");
    myAppender = new RollingFileAppender(pattern, logFile.getAbsolutePath());
    myAppender.setMaxFileSize("20MB");
    myAppender.setMaxBackupIndex(10);
  }

  @NotNull
  @Override
  public com.intellij.openapi.diagnostic.Logger getLoggerInstance(@NotNull String category) {
    final Logger logger = Logger.getLogger(category);
    logger.addAppender(myAppender);
    logger.setLevel(isDebugLevel(category) ? Level.DEBUG : Level.INFO);
    return new Log4jBasedLogger(logger);
  }

  private boolean isDebugLevel(String category) {
    for (String debug : myCategoriesWithDebugLevel) {
      if (category.startsWith(debug)) {
        return true;
      }
    }
    return false;
  }
}