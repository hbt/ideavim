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

package com.maddyhome.idea.vim.action.tab;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.maddyhome.idea.vim.hbtlabs.lib.ToDoCommonAction;
import org.apache.log4j.Level;

import java.util.ArrayList;

public class PreviousTodoAction extends ToDoCommonAction {

  @Override
  public void actionPerformed(AnActionEvent e) {
    log.debug("init prev");

    Project project = e.getProject();
    ArrayList todosMap = todosMap = buildList(project);

    if (todosMap.size() == 0) {
      return;
    }

    // get last item
    int nextIndex = getLastItem(e);
    nextIndex--;

    // jump to next location
    if (nextIndex < 0) {
      nextIndex = todosMap.size() - 1;
    }

      viewTodos(todosMap);
       
    jump(e, todosMap, nextIndex);
  }
}
