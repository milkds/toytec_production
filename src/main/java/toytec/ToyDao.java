package toytec;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projection;

import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ToyDao {
    public static List<String> getItemLinksFromCategory(String category, Session session) {
        List<String> itemLinksFromCategory = new ArrayList<>();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ToyItem> crQ = builder.createQuery(ToyItem.class);
        Root<ToyItem> root = crQ.from(ToyItem.class);
        crQ.where(builder.and(
                builder.equal(root.get("itemCategory"), category),
                builder.equal(root.get("itemStatus"),"ACTIVE")))
                .select(root.get("itemLink"));

        Query q = session.createQuery(crQ);
        itemLinksFromCategory = q.getResultList();


        return itemLinksFromCategory;
    }

    public static Session getSession() {
        return HibernateUtil.getSessionFactory().openSession();
    }

    public static List<ToyItem> getAllItems(Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ToyItem> crQ = builder.createQuery(ToyItem.class);
        Root<ToyItem> root = crQ.from(ToyItem.class);
        crQ.select(root);
        Query q = session.createQuery(crQ);

        return q.getResultList();
    }

    public static void updateItem(Session session, ToyItem item) {
        //updates
        Transaction transaction = null;
        try {
            transaction = session.getTransaction();
            transaction.begin();
            session.update(item);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.out.println("couldn't save item to db "+ item.getItemName());
            e.printStackTrace();
         //   System.exit(0);
        }
    }

    public static ToyItem getItemByWebLink(String link, Session session, String categoryName) {
        ToyItem item = null;

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ToyItem> crQ = builder.createQuery(ToyItem.class);
        Root<ToyItem> root = crQ.from(ToyItem.class);
        crQ.where(builder.and(builder.equal(root.get("itemLink"), link),
                              builder.equal(root.get("itemCategory"), categoryName)));
        Query q = session.createQuery(crQ);
        item = (ToyItem)q.getSingleResult();

        return item;
    }

    public static void addNewItem(ToyItem toyItem, Session session) {
        Transaction transaction = null;
        try {
            transaction = session.getTransaction();
            transaction.begin();

            int id = (Integer)session.save(toyItem);
            toyItem.setItemID(id);
            List<ToyOption> options = toyItem.getOptions();
            for (ToyOption option: options){
                option.setItem(toyItem);
                session.persist(option);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.out.println("couldn't save car to db "+ toyItem.getItemName());
            e.printStackTrace();
           // System.exit(0);
        }
    }

    public static List<String> getItemLinksFromSubCategory(String subCategoryName, Session session) {
        List<String> itemLinksFromCategory = new ArrayList<>();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ToyItem> crQ = builder.createQuery(ToyItem.class);
        Root<ToyItem> root = crQ.from(ToyItem.class);
        crQ.where(builder.and(builder.equal(root.get("itemSubCategory"), subCategoryName),
                              builder.equal(root.get("itemStatus"),"ACTIVE"))).select(root.get("itemLink"));

        Query q = session.createQuery(crQ);
        itemLinksFromCategory = q.getResultList();


        return itemLinksFromCategory;
    }

    public static void updateItemWithOptions(Session session, ToyItem item) {
        Transaction transaction = null;
        try {
            transaction = session.getTransaction();
            transaction.begin();
            deleteOldOptions(session, item);
            List<ToyOption> options = item.getOptions();
            for (ToyOption option: options){
                option.setItem(item);
                session.persist(option);
            }
            session.update(item);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            System.out.println("couldn't save item to db "+ item.getItemName());
            e.printStackTrace();
        }
    }

    private static void deleteOldOptions(Session session, ToyItem item) {
        List<ToyOption> options = getOptionsByItem(session, item);
        for (ToyOption option: options){
            session.delete(option);
            System.out.println("option NO "+option.getOptionID() + " deleted");
        }
    }

    private static List<ToyOption> getOptionsByItem(Session session, ToyItem item) {
        List<ToyOption> options = null;
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ToyOption> crQ = builder.createQuery(ToyOption.class);
        Root<ToyOption> root = crQ.from(ToyOption.class);
        crQ.where(builder.equal(root.get("item"), item));
        Query q = session.createQuery(crQ);
        options = q.getResultList();

        return options;
    }

    public static List<String> getItemsWithOptionsLinkList() {
        List<String> links = new ArrayList<>();
      //  links.add("https://www.toyteclifts.com/ks30lc-king-shocks-stage-3-race-kit-2008-land-cruiser-200-series.html");
        links.add("https://www.toyteclifts.com/ttbosstac-2005-toytec-boss-suspension-system-for-05-tacoma.html");

        return links;
    }

    public static List<ToyItem> getItemsByWebLink(String itemLink, Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ToyItem> crQ = builder.createQuery(ToyItem.class);
        Root<ToyItem> root = crQ.from(ToyItem.class);
        crQ.where(builder.and(builder.equal(root.get("itemLink"), itemLink),
                              builder.equal(root.get("itemStatus"), "ACTIVE")));
        Query q = session.createQuery(crQ);

        return q.getResultList();
    }
}
