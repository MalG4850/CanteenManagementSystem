package canteen;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public final class MenuModel {
    private static final MenuModel INSTANCE = new MenuModel();
    private final ObservableList<MenuItem> items = FXCollections.observableArrayList();
    private final MenuDao menuDao = new MenuDao();

    private MenuModel() {
        refresh();
    }

    public static MenuModel getInstance() {
        return INSTANCE;
    }

    public ObservableList<MenuItem> getItems() {
        return items;
    }

    public void refresh() {
        List<MenuItem> list = menuDao.getAllItems(false);
        items.setAll(list);
    }

    public void addItem(MenuItem item) {
        items.add(item);
    }

    public void removeItem(MenuItem item) {
        items.remove(item);
    }

    public void replaceItem(MenuItem oldItem, MenuItem newItem) {
        int idx = items.indexOf(oldItem);
        if (idx >= 0) items.set(idx, newItem);
    }
}