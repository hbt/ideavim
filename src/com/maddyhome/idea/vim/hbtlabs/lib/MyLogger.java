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


import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.IOException;

/**
 * @author nik
 */
public class MyLogger {

  public static Logger getInstance() {
    Logger log = Logger.getLogger("hbt-vimidea");
    try {
      log.addAppender(new FileAppender(new PatternLayout("%d %F:%L - %m%n")
        , "/tmp/vimidea.log"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    log.setLevel(Level.DEBUG);
    return log;
  }
  
  public static void debug(Object msg) {
    Logger log = Logger.getLogger("hbt-vimidea");
    try {
      log.addAppender(new FileAppender(new PatternLayout("%d %F:%L - %m%n")
        , "/tmp/vimidea.log"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    log.setLevel(Level.DEBUG);
    log.debug(msg);
  }
}