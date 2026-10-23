package canteen;

import javafx.collections.ObservableList;
import java.util.List;
import java.util.Optional;

public final class MenuService {
    private final MenuDao menuDao = new MenuDao();
    private final MenuModel menuModel = MenuModel.getInstance();

    public ObservableList<MenuItem> getAllItems() {
        return menuModel.getItems();
    }

    public List<MenuItem> getAllItems(boolean includeUnavailable) {
        return menuDao.getAllItems(includeUnavailable);
    }

    public List<MenuItem> getItemsByCategory(String category) {
        return menuDao.getItemsByCategory(category);
    }

    public List<String> getCategories() {
        return menuDao.getCategories();
    }

    public Optional<MenuItem> findById(int itemId) {
        return menuDao.findById(itemId);
    }

    public CartItem createCartItem(int itemId, int quantity) {
        return findById(itemId)
            .map(item -> new CartItem(item.getId(), item.getName(), quantity, item.getPrice()))
            .orElse(null);
    }

    public boolean addItem(String name, double price, String category) {
        if (name == null || name.trim().isEmpty()) return false;
        if (price < 0) return false;
        boolean ok = menuDao.addItem(name.trim(), price, category != null ? category.trim() : "General");
        if (ok) {
            menuModel.refresh();
        }
        return ok;
    }

    public boolean updateItem(int itemId, String name, double price, String category, boolean available) {
        if (name == null || name.trim().isEmpty()) return false;
        if (price < 0) return false;
        boolean ok = menuDao.updateItem(itemId, name.trim(), price, category != null ? category.trim() : "General", available);
        if (ok) {
            menuModel.refresh();
        }
        return ok;
    }

    public boolean updatePrice(int itemId, double newPrice) {
        if (newPrice < 0) return false;
        boolean ok = menuDao.updatePrice(itemId, newPrice);
        if (ok) {
            menuModel.refresh();
        }
        return ok;
    }

    public boolean setAvailability(int itemId, boolean available) {
        boolean ok = menuDao.setAvailability(itemId, available);
        if (ok) {
            menuModel.refresh();
        }
        return ok;
    }

    public boolean deleteItem(int itemId) {
        boolean ok = menuDao.deleteItem(itemId);
        if (ok) {
            menuModel.refresh();
        }
        return ok;
    }

    public void refresh() {
        menuModel.refresh();
    }
}