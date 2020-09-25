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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveCaretOffsetAction extends AnAction {

  @Override
  public void actionPerformed(AnActionEvent e) {
    DataContext dataContext = e.getDataContext();
    Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
    VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
    if (editor != null && file != null) {

      int offset = editor.getCaretModel().getCurrentCaret().getOffset();
      String filepath = file.getCanonicalPath();
      int line = editor.getCaretModel().getCurrentCaret().getLogicalPosition().line;
      int column = editor.getCaretModel().getCurrentCaret().getLogicalPosition().column;

      try {
        FileWriter fw = new FileWriter(new File("/tmp/ideavim-caret-position.txt"));
        fw.write(filepath + "\n" + offset + "\n" + line + "\n" + column);
        fw.close();
      } catch (IOException ioException) {
        ioException.printStackTrace();
      }
    }

  }
}
