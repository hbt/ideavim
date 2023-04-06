/*
 * Copyright 2003-2022 The IdeaVim authors
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE.txt file or at
 * https://opensource.org/licenses/MIT.
 */

package com.maddyhome.idea.vim

import com.intellij.ide.RecentProjectListActionProvider
import com.intellij.ide.RecentProjectsManager
import com.intellij.ide.ReopenProjectAction
import java.io.File

class VimHBTPlugin {
  companion object {
    fun initialize() {

      // Note(hbt) remove non-existing projects from recent projects list
      val instance = RecentProjectsManager.getInstance()
      val recentProjectActions = RecentProjectListActionProvider.getInstance().getActions(false, false)
      for (action in recentProjectActions) {
        if (action is ReopenProjectAction) {
          val item = action
          val file = File(item.projectPath)
          if (!file.exists()) {
            instance.removePath(item.projectPath)
          }
        }
      }
    }
  }

}
