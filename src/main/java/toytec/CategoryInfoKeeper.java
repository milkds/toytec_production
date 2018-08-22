package toytec;

import java.util.List;

public class CategoryInfoKeeper {
    private List<String> itemLinksFromCategory;
    private List<String> itemLinksFromDB;

    private String categoryName;
    private String categoryLink;
    private String subCategoryName;
    private String subCategoryLink;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setCategoryLink(String categoryLink) {
        this.categoryLink = categoryLink;
    }

    public String getCategoryLink() {
        return categoryLink;
    }

    public List<String> getItemLinksFromCategory() {
        return itemLinksFromCategory;
    }

    public void setItemLinksFromCategory(List<String> itemLinksFromCategory) {
        this.itemLinksFromCategory = itemLinksFromCategory;
    }

    public List<String> getItemLinksFromDB() {
        return itemLinksFromDB;
    }

    public void setItemLinksFromDB(List<String> itemLinksFromDB) {
        this.itemLinksFromDB = itemLinksFromDB;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getSubCategoryLink() {
        return subCategoryLink;
    }

    public void setSubCategoryLink(String subCategoryLink) {
        this.subCategoryLink = subCategoryLink;
    }
}
