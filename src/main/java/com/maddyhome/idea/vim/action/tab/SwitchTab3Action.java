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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.maddyhome.idea.vim.api.ExecutionContext;
import com.maddyhome.idea.vim.api.VimEditor;
import org.jdom.Element;

import static com.maddyhome.idea.vim.api.VimInjectorKt.injector;

public class SwitchTab3Action extends AnAction {
  private final Logger logger = Logger.getInstance(SwitchTab3Action.class);

  @Override
  public void actionPerformed(AnActionEvent e) {

    Project project = e.getProject();
    if(project == null) {
      return;
    }

    // Note(hbt) cant call Switcher.SwitcherPanel
    // so this is a hack to remove the recent files from the history, replace them with the active tabs, display the recent files switcher then put the recent files back
    
    EditorHistoryManager ehm = EditorHistoryManager.getInstance(project);
    Element recentFilesState = ehm.getState();
    ehm.removeAllFiles();


    final FileEditorManagerImpl editorManager = (FileEditorManagerImpl)FileEditorManager.getInstance(project);
    for (Pair<VirtualFile, EditorWindow> pair : editorManager.getSelectionHistory()) {
      logger.debug(pair.first.getCanonicalPath());
      ehm.updateHistoryEntry(pair.first, false);
    }


    if (injector.getEditorGroup().localEditors().toArray().length > 0) {
      ExecutionContext executionContext = injector.getExecutionContextManager()
        .onEditor((VimEditor)injector.getEditorGroup().localEditors().toArray()[0], null);
      injector.getActionExecutor().executeAction("RecentFiles", executionContext);
   
    }

    ehm.loadState(recentFilesState);


  }
}
