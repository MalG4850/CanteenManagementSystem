package canteen;

import java.util.List;

public final class CategoryService {
    private final CategoryDao dao = new CategoryDao();

    public List<String> getAllCategories() {
        return dao.getAllCategories();
    }

    public boolean addCategory(String name) {
        return dao.addCategory(name);
    }

    public boolean deleteCategory(String name) {
        return dao.deleteCategory(name);
    }
}