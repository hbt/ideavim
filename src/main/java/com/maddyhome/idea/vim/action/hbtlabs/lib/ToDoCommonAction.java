package com.maddyhome.idea.vim.action.hbtlabs.lib;

import com.intellij.ide.todo.AllTodosTreeBuilder;
import com.intellij.ide.todo.SmartTodoItemPointer;
import com.intellij.ide.todo.TodoTreeStructure;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.PsiTodoSearchHelper;
import com.intellij.psi.search.TodoAttributesUtil;
import com.intellij.psi.search.TodoItem;
import com.intellij.psi.search.TodoPattern;
import com.intellij.ui.treeStructure.Tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;


public abstract class ToDoCommonAction extends AnAction {

  private static final com.intellij.openapi.diagnostic.Logger logger = Logger.getInstance(ToDoCommonAction.class.getName());
  public static HashMap<Project, Integer> last = new HashMap();

  public void viewTodos(ArrayList todosMap) {
    todosMap.forEach((todo) -> {

      Object[] nextItem = (Object[]) todo;
      VirtualFile file = (VirtualFile) nextItem[0];
      Integer lineNumber = (Integer) nextItem[1];
    });
  }

  public void jump(AnActionEvent e, ArrayList todosMap, int nextIndex) {
    Object[] nextItem = (Object[]) todosMap.get(nextIndex);
    VirtualFile file = (VirtualFile) nextItem[0];
    Integer offset = (Integer) nextItem[1];


    Navigatable navigatable = PsiNavigationSupport.getInstance().createNavigatable(e.getProject(), file, offset);
    navigatable.navigate(true);

    last.put(e.getProject(), nextIndex);
  }

  public int getLastItem(AnActionEvent e) {
    // Note(hbt) this uses the index. i.e if new todos are added, it might jump around. consider enhancing and using a hash of the virtual file + line number
    int nextIndex = 0;
    if (last.containsKey(e.getProject())) {
      nextIndex = last.get(e.getProject());
    }
    return nextIndex;
  }

  public ArrayList buildList(Project project) {
    ArrayList ret = new ArrayList();
    // TODO(hbt) ENHANCE add check when pattern is not in IDE  and print error . Otherwise, it fails silently and returns nothing
    String pattern = "\\b.*todo\\b.*hbt\\b.*NEXT\\b.*";

    ArrayList<SmartTodoItemPointer> mytodos = getSmartTodoItemPointers(project, pattern);
    ArrayList<SmartTodoItemPointer> nextTodos = filterNextTodosFromMyTodos(pattern, mytodos);
    ArrayList<SmartTodoItemPointer> sortedTodos = sortTodoPointers(nextTodos);

//    log.debug("finalized list");
//    for (SmartTodoItemPointer sp : sortedTodos) {
//      log.debug(getTodoText(sp));
//    }

    ret = createSortedNextTodos(sortedTodos);


    return ret;
  }

  String getTodoText(SmartTodoItemPointer todo) {
    String text = todo.getTodoItem().getFile().getText();
    int startOffset = todo.getTodoItem().getTextRange().getStartOffset();
    String strtodo = text.substring(startOffset, todo.getTodoItem().getTextRange().getEndOffset());
    return strtodo;
  }

  private ArrayList<SmartTodoItemPointer> sortTodoPointers(ArrayList<SmartTodoItemPointer> todos) {
    ArrayList<SmartTodoItemPointer> ret = new ArrayList<>();

    TreeMap<String, SmartTodoItemPointer> numberTodo = new TreeMap();

    for (int i = 0; i < todos.size(); i++) {
      SmartTodoItemPointer todo = todos.get(i);


      Version number = extractVersion(todo);
      numberTodo.put(number.get(), todo);
    }

    String[] keys = numberTodo.keySet().toArray(new String[0]);
    ArrayList<Version> versions = new ArrayList<>();
    for (String key : keys) {
      versions.add(new Version(key));
    }
    Collections.sort(versions);

    for (int i = 0; i < versions.size(); i++) {
      Version key = versions.get(i);
      SmartTodoItemPointer stip = numberTodo.get(key.get());
      ret.add(stip);
    }


    return ret;
  }

  private String[] getTodoPatternsList() {
    String[] todoPatterns = {"TODO(hbt) NEXT", "TODO[hbt] NEXT"};
    return todoPatterns;
  }

  private Version extractVersion(SmartTodoItemPointer todo) {
    Version ret = null;

    String text = todo.getTodoItem().getFile().getText();
    int startOffset = todo.getTodoItem().getTextRange().getStartOffset();
    String strtodo = text.substring(startOffset, todo.getTodoItem().getTextRange().getEndOffset());
    for (String pt : this.getTodoPatternsList()) {
      strtodo = strtodo.replace(pt, "").trim();
    }

    String[] parts = strtodo.trim().split(" ");
    if (parts.length > 0) {
      String first = parts[0];
      String bigNumber = first.trim().replace(".", "").trim();
      try {
        Integer.parseInt(bigNumber);
        ret = new Version(first.trim());
      } catch (Exception ex) {
      }
    }

    if (ret == null) {
      String fileid = Integer.toString(Math.abs(todo.getTodoItem().getFile().getVirtualFile().getCanonicalPath().hashCode()));
      String offset = Integer.toString(Math.abs(todo.getRangeMarker().getStartOffset()));
      ret = new Version("19999." + fileid + "." + offset);
    }


    return ret;
  }


  private ArrayList createSortedNextTodos(ArrayList<SmartTodoItemPointer> nextTodos) {
    ArrayList ret = new ArrayList();
    for (int i = 0; i < nextTodos.size(); i++) {
      SmartTodoItemPointer todo = nextTodos.get(i);
      ret.add(new Object[]{todo.getTodoItem().getFile().getVirtualFile(), calculateStartOffset(todo)});
    }
    return ret;
  }

  private Integer calculateStartOffset(SmartTodoItemPointer todo) {
    int startOffset = todo.getRangeMarker().getStartOffset();
    startOffset += getTodoPatternsList()[0].length() + 1;
    return startOffset;
  }

  private ArrayList<SmartTodoItemPointer> filterNextTodosFromMyTodos(String pattern, ArrayList<SmartTodoItemPointer> mytodos) {
    ArrayList<SmartTodoItemPointer> nextTodos = new ArrayList<>();
    mytodos.forEach((todo) -> {
      String patternString = todo.getTodoItem().getPattern().getPatternString();
      if (patternString.equals(pattern)) {
        nextTodos.add(todo);
      }
    });
    return nextTodos;
  }

  private ArrayList<SmartTodoItemPointer> getSmartTodoItemPointers(Project project, String pattern) {
    ArrayList<SmartTodoItemPointer> todos = new ArrayList<SmartTodoItemPointer>();
    AllTodosTreeBuilder builder = new AllTodosTreeBuilder(new Tree(), project);
    builder.init();
    AbstractTreeStructure structure = builder.getTodoTreeStructure();
    PsiTodoSearchHelper searchHelper = ((TodoTreeStructure)structure).getSearchHelper();

    // TODO(hbt) NEXT rm
    PsiFile[] filesWithTodoItems = searchHelper.findFilesWithTodoItems();

    ArrayList<PsiFile> psiFiles = new ArrayList<>();
    searchHelper.processFilesWithTodoItems(psiFile -> {
      // TODO(hbt) NEXT rm 
      logger.info(psiFile.getVirtualFile().getPath());
      psiFiles.add(psiFile);
      return true;
    });

    for (PsiFile file : psiFiles) {

      TodoPattern todoPattern = new TodoPattern(pattern, TodoAttributesUtil.createDefault(), false);

      int todoItemsCount = searchHelper.getTodoItemsCount(file, todoPattern);
      if (todoItemsCount > 0) {

        TodoItem[] todoItems = searchHelper.findTodoItems(file);

        for (TodoItem pt : todoItems) {
          if (pt.getPattern().getPatternString().equalsIgnoreCase(pattern)) {
            Document document = PsiDocumentManager.getInstance(project).getDocument(file);
            SmartTodoItemPointer smartTodoItemPointer = new SmartTodoItemPointer(pt, document);
            todos.add(smartTodoItemPointer);
          }
        }

      }

    }

    return todos;
  }
}
