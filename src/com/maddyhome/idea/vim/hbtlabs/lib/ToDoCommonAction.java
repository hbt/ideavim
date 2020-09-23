package com.maddyhome.idea.vim.hbtlabs.lib;

import com.intellij.ide.todo.AllTodosTreeBuilder;
import com.intellij.ide.todo.MyTreeBuilder;
import com.intellij.ide.todo.SmartTodoItemPointer;
import com.intellij.ide.todo.TodoTreeStructure;
import com.intellij.ide.todo.nodes.TodoItemNode;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.ide.util.scopeChooser.ScopeChooserCombo;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.TodoAttributesUtil;
import com.intellij.psi.search.TodoItem;
import com.intellij.psi.search.TodoPattern;
import com.intellij.ui.treeStructure.Tree;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.*;

public abstract class ToDoCommonAction extends AnAction {

  //    protected static final Logger LOG = Logger.getInstance(TodoPanel.class);
  public static Logger log = MyLogger.getInstance();
  //  public Logger log = new Log4jFactory(new File("/tmp/idea.log"), "sa").getLoggerInstance("cat");
  public static HashMap<Project, Integer> last = new HashMap();
  /**
   * @deprecated
   */
  static HashMap<String, ArrayList<TodoItemNode>> cache = new HashMap<>();
  private Project project;

  public void viewTodos(ArrayList todosMap) {

    todosMap.forEach((todo) -> {

      Object[] nextItem = (Object[]) todo;
      VirtualFile file = (VirtualFile) nextItem[0];
      Integer lineNumber = (Integer) nextItem[1];
      log.debug(file + " " + lineNumber);
    });
  }

  public void jump(AnActionEvent e, ArrayList todosMap, int nextIndex) {
    Object[] nextItem = (Object[]) todosMap.get(nextIndex);
    VirtualFile file = (VirtualFile) nextItem[0];
    Integer offset = (Integer) nextItem[1];

    log.debug("jump to " + file + " " + offset);

    Navigatable navigatable = PsiNavigationSupport.getInstance().createNavigatable(e.getProject(), file, offset);
    navigatable.navigate(true);

    log.debug("INDEX: " + nextIndex);
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


  public void recursiveGet(Project p, AbstractTreeStructure structure, Object obj) {
    Object[] children = structure.getChildElements(obj);
    for (int i = 0; i < children.length; i++) {
      //add 
      if (children[i] instanceof TodoItemNode) {

        ArrayList<TodoItemNode> todoItemNodes = cache.get(p.getLocationHash());
        todoItemNodes.add((TodoItemNode) children[i]);
      }
      recursiveGet(p, structure, children[i]);
    }
  }

  protected MyTreeBuilder createTreeBuilder(JTree tree, Project project) {

    String preselect = PropertiesComponent.getInstance(project).getValue("TODO_SCOPE");
    ScopeChooserCombo myScopes = new ScopeChooserCombo(project, false, true, preselect);
    myScopes.setCurrentSelection(false);
    myScopes.setUsageView(false);

//        ScopeBasedTodosTreeBuilder builder = new ScopeBasedTodosTreeBuilder(tree, project, myScopes);
    MyTreeBuilder builder = new MyTreeBuilder(tree, project, myScopes);
    builder.init();
    return builder;

    // Note(hbt) fails because method is protected -- have to add the MyTreeBuilder class to the jar https://stackoverflow.com/questions/7076414/java-lang-illegalaccesserror-tried-to-access-method/7076538

//            DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());
//            JTree tree = new Tree(model);
//            MyTreeBuilder builder = this.createTreeBuilder(tree, project);
//            TodoFilter filter = new TodoFilter();
//            TodoPattern todoPattern = new TodoPattern("TodoAttributesUtil.createDefault()", TodoAttributesUtil.createDefault(), false);
//            filter.addTodoPattern(todoPattern);
//            builder.setTodoFilter2(filter);
//            builder.init();
  }

  public ArrayList buildList(Project project) {
    this.project = project;
    ArrayList ret = new ArrayList();
    // TODO(hbt) NEXT add check when pattern is not in IDE  and print error . Otherwise, it fails silently and returns nothing
    String pattern = "\\b.*todo\\b.*hbt\\b.*NEXT\\b.*";

    ArrayList<SmartTodoItemPointer> mytodos = getSmartTodoItemPointers(project, pattern);
    ArrayList<SmartTodoItemPointer> nextTodos = filterNextTodosFromMyTodos(pattern, mytodos);
    ArrayList<SmartTodoItemPointer> sortedTodos = sortTodoPointers(nextTodos);
    
    log.debug("finalized list");
    for(SmartTodoItemPointer sp: sortedTodos) {
      log.debug(getTodoText(sp));
    }

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
    ArrayList<SmartTodoItemPointer> stringTodos = new ArrayList<>();

    for (int i = 0; i < todos.size(); i++) {
      SmartTodoItemPointer todo = todos.get(i);
      String text = todo.getTodoItem().getFile().getText();
      int startOffset = todo.getTodoItem().getTextRange().getStartOffset();
      String strtodo = text.substring(startOffset, todo.getTodoItem().getTextRange().getEndOffset());
      for (String pt : this.getTodoPatternsList()) {
        strtodo = strtodo.replace(pt, "").trim();
      }

      if (todoHasNumber(strtodo)) {
        Version number = extractVersion(strtodo);
        numberTodo.put(number.get(), todo);
        log.debug(number);
      } else {
        stringTodos.add(todo);
      }
    }

    String[] keys = numberTodo.keySet().toArray(new String[0]);
    ArrayList<Version> versions = new ArrayList<>();
    for (String key : keys) {
      log.debug("new version: " + key);
      versions.add(new Version(key));
    }
    Collections.sort(versions);

    for (int i = 0; i < versions.size(); i++) {
      Version key = versions.get(i);
      SmartTodoItemPointer stip = numberTodo.get(key.get());
      ret.add(stip);
      log.debug(getTodoText(stip));
    }

    for (SmartTodoItemPointer stringTodo : stringTodos) {
      ret.add(stringTodo);
    }

    return ret;
  }

  private String[] getTodoPatternsList() {
    String[] todoPatterns = {"TODO(hbt) NEXT", "TODO[hbt] NEXT"};
    return todoPatterns;
  }

  private Version extractVersion(String strtodo) {
    Version ret = null;


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


    return ret;
  }

  private boolean todoHasNumber(String strtodo) {
    return extractVersion(strtodo) != null;
  }

  private ArrayList createSortedNextTodos(ArrayList<SmartTodoItemPointer> nextTodos) {
    ArrayList ret = new ArrayList();
    for (int i = 0; i < nextTodos.size(); i++) {
      SmartTodoItemPointer todo = nextTodos.get(i);
      // TODO(hbt) NEXT add the starter offset to be the first word instead of first character
      ret.add(new Object[]{todo.getTodoItem().getFile().getVirtualFile(), todo.getRangeMarker().getStartOffset()});
    }
    return ret;
  }

  private ArrayList<SmartTodoItemPointer> filterNextTodosFromMyTodos(String pattern, ArrayList<SmartTodoItemPointer> mytodos) {
    ArrayList<SmartTodoItemPointer> nextTodos = new ArrayList<>();
    mytodos.forEach((todo) -> {
      String patternString = todo.getTodoItem().getPattern().getPatternString();
      if (patternString.equals(pattern)) {
        nextTodos.add(todo);
      }
    });
    log.debug("Found " + nextTodos.size() + " matches to filter");
    return nextTodos;
  }

  private ArrayList<SmartTodoItemPointer> getSmartTodoItemPointers(Project project, String pattern) {
    ArrayList<SmartTodoItemPointer> todos = new ArrayList<SmartTodoItemPointer>();
    AllTodosTreeBuilder builder = new AllTodosTreeBuilder(new Tree(), project);
    builder.init();
    AbstractTreeStructure structure = builder.getTodoTreeStructure();
    PsiFile[] filesWithTodoItems = ((TodoTreeStructure) structure).getSearchHelper().findFilesWithTodoItems();

    for (PsiFile file : filesWithTodoItems) {

      TodoPattern todoPattern = new TodoPattern(pattern, TodoAttributesUtil.createDefault(), false);

      int todoItemsCount = ((TodoTreeStructure) structure).getSearchHelper().getTodoItemsCount(file, todoPattern);
      if (todoItemsCount > 0) {
        log.debug(file.getVirtualFile().getCanonicalPath());

        TodoItem[] todoItems = ((TodoTreeStructure) structure).getSearchHelper().findTodoItems(file);

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

  public ArrayList buildList2(Project project) {
    ArrayList todosMap = new ArrayList();
    String pattern = "\\b.*todo\\b.*hbt\\b.*NEXT\\b.*";
    String[] todoPatterns = {"TODO(hbt) NEXT", "TODO[hbt] NEXT"};


    ArrayList<SmartTodoItemPointer> todos = new ArrayList();

    {


    }
    log.debug("BEGIN build tree");
    {

      AllTodosTreeBuilder builder = new AllTodosTreeBuilder(new Tree(), project);
      builder.init();


      AbstractTreeStructure structure = builder.getTodoTreeStructure();

      PsiFile[] filesWithTodoItems = ((TodoTreeStructure) structure).getSearchHelper().findFilesWithTodoItems();
      for (PsiFile file : filesWithTodoItems) {

        TodoPattern todoPattern = new TodoPattern(pattern, TodoAttributesUtil.createDefault(), false);

//                TodoItem[] todoItems1 = ((TodoTreeStructure) structure).getSearchHelper().findTodoItems(file);
//                for (TodoItem pt : todoItems1) {
//                    Document document = PsiDocumentManager.getInstance(project).getDocument(file);
//                    SmartTodoItemPointer smartTodoItemPointer = new SmartTodoItemPointer(pt, document);
//                    todos.add(smartTodoItemPointer);
//                }

        int todoItemsCount = ((TodoTreeStructure) structure).getSearchHelper().getTodoItemsCount(file, todoPattern);
        if (todoItemsCount > 0) {
          log.debug(file.getVirtualFile().getCanonicalPath());

          TodoItem[] todoItems = ((TodoTreeStructure) structure).getSearchHelper().findTodoItems(file);

          for (TodoItem pt : todoItems) {
            if (pt.getPattern().getPatternString().equalsIgnoreCase(pattern)) {
              Document document = PsiDocumentManager.getInstance(project).getDocument(file);
              SmartTodoItemPointer smartTodoItemPointer = new SmartTodoItemPointer(pt, document);
              todos.add(smartTodoItemPointer);
            }
          }

        }

      }

    }

    log.debug("END build tree");
    log.debug("tree size" + todos.size());

    log.debug("looking for matches to filter");
    ArrayList<SmartTodoItemPointer> nextTodos = new ArrayList();
    {
      // filter
      todos.forEach((todo) -> {
        String patternString = todo.getTodoItem().getPattern().getPatternString();
        if (patternString.equals(pattern)) {
          nextTodos.add(todo);
        }
      });
      log.debug("Found " + nextTodos.size() + " matches to filter");
    }

    ArrayList<SmartTodoItemPointer> sortedTodos = new ArrayList();
    {
      // filter with/without number

      ArrayList numberedTodos = new ArrayList();
      ArrayList<SmartTodoItemPointer> strTodos = new ArrayList();
      int maxNbDots = 0;
      {

        for (int i = 0; i < nextTodos.size(); i++) {
          SmartTodoItemPointer todo = nextTodos.get(i);
          String text = todo.getTodoItem().getFile().getText();
          int startOffset = todo.getTodoItem().getTextRange().getStartOffset();
          String strtodo = text.substring(startOffset, todo.getTodoItem().getTextRange().getEndOffset());
          for (String pt : todoPatterns) {
            strtodo = strtodo.replace(pt, "").trim();
          }

          String[] parts = strtodo.split(" ");
          if (parts.length > 0) {
            String first = parts[0];
            int nbDots = first.split("\\.").length - 1;
            if (nbDots > maxNbDots) {
              maxNbDots = nbDots;
            }
            String bigNumber = first.replace(".", "").trim();
            int nb = -1;
            try {

              nb = Integer.parseInt(bigNumber);
            } catch (Exception ex) {
              log.debug("cannot parse " + bigNumber + " " + strtodo);
            }
            if (nb == -1) {
              strTodos.add(todo);
              log.debug("strtodo: " + strtodo);
            } else {
              numberedTodos.add(new Object[]{todo, nb, nbDots});
//                                nb = nb * 10^nbDots;
//                                log.debug("" + nb);
              log.debug("nb todo: " + strtodo);
            }
          }
        }


      }

      // sort list with numbers
      TreeMap<Integer, SmartTodoItemPointer> map = new TreeMap();
      {

        log.debug("" + maxNbDots);
        for (int i = 0; i < numberedTodos.size(); i++) {
          Object[] items = (Object[]) numberedTodos.get(i);
          SmartTodoItemPointer todo = (SmartTodoItemPointer) items[0];
          Integer nb = (Integer) items[1];
          Integer nbDots = (Integer) items[2];

          int posi = (int) (nb * Math.pow(10, maxNbDots - nbDots));
          log.debug("" + posi);

          map.put(posi, todo);
        }

        Iterator<Integer> iterator = map.navigableKeySet().iterator();
        while (iterator.hasNext()) {
          Integer next = iterator.next();
          SmartTodoItemPointer todo = map.get(next);

          String text = todo.getTodoItem().getFile().getText();
          int startOffset = todo.getTodoItem().getTextRange().getStartOffset();
          String strtodo = text.substring(startOffset, todo.getTodoItem().getTextRange().getEndOffset());
          log.debug("ordered" + strtodo);

          sortedTodos.add(todo);
        }

      }

      // add without numbers at the bottom
      {
        strTodos.forEach((todo) -> {
          sortedTodos.add(todo);
        });
      }
    }

    {
      // map list
      for (int i = 0; i < sortedTodos.size(); i++) {
        SmartTodoItemPointer todo = sortedTodos.get(i);
        int startOffset = todo.getTodoItem().getTextRange().getStartOffset();
        int lineStartOffset = todo.getDocument().getLineNumber(startOffset);
        todosMap.add(new Object[]{todo.getTodoItem().getFile().getVirtualFile(), todo.getRangeMarker().getStartOffset()});
      }
    }

    return todosMap;
  }

}
