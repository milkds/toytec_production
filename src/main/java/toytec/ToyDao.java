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
        /*Transaction transaction = null;
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
        }*/
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
       /* Transaction transaction = null;
        try {
            transaction = session.getTransaction();
            transaction.begin();

            int id = (Integer)session.save(toyItem);
            toyItem.setItemID(id);
            List<ToyOption> options = toyItem.getOptions();
            for (ToyOption option: options){
               option.setItemID(id);
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
        }*/
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
}
