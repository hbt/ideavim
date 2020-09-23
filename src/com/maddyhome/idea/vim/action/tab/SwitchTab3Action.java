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

import com.intellij.ide.actions.Switcher;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.log4j.Level;
import org.jdom.Element;

import java.util.List;

public class SwitchTab3Action extends AnAction {
  private Logger logger = Logger.getInstance(SwitchTab3Action.class);

  @Override
  public void actionPerformed(AnActionEvent e) {
    logger.setLevel(Level.DEBUG);
    logger.debug("switch3");

    Project project = e.getProject();

    EditorHistoryManager ehm = EditorHistoryManager.getInstance(project);
    Element recentFilesState = ehm.getState();
    ehm.removeAllFiles();

    final FileEditorManagerImpl editorManager = (FileEditorManagerImpl) FileEditorManager.getInstance(project);
    for (Pair<VirtualFile, EditorWindow> pair : editorManager.getSelectionHistory()) {
      logger.debug(pair.first.getCanonicalPath());
      ehm.updateHistoryEntry(pair.first, false);
    }

    Switcher.createAndShowSwitcher(e, "switch", IdeActions.ACTION_RECENT_FILES, false, true);

    ehm.loadState(recentFilesState);


  }
}
