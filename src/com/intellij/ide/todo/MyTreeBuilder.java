package com.intellij.ide.todo;

import com.intellij.ide.util.scopeChooser.ScopeChooserCombo;
import com.intellij.openapi.project.Project;

import javax.swing.*;

public class MyTreeBuilder extends ScopeBasedTodosTreeBuilder {

  public MyTreeBuilder(JTree tree, Project project, ScopeChooserCombo scopes) {
    super(tree, project, scopes);
  }

//    @Override
//    void setTodoFilter(TodoFilter filter) {
//        super.setTodoFilter(filter);
//    }

  public void setTodoFilter2(TodoFilter filter) {
    this.setTodoFilter(filter);
  }
}
