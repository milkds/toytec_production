package toytec;

import org.hibernate.Session;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class ToyStatisticsDAO {

    public static long getTotalItemsQty(Session session){
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> crQ = builder.createQuery(Long.class);
        Root<ToyItem> root = crQ.from(ToyItem.class);
        crQ.select(builder.count(root.get("itemID")));
        crQ.where(builder.equal(root.get("itemStatus"), "ACTIVE"));
        Query q = session.createQuery(crQ);

        return (Long)q.getSingleResult();
    }

    public static long getItemsQuantityByCategory(Session session, String category) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> crQ = builder.createQuery(Long.class);
        Root<ToyItem> root = crQ.from(ToyItem.class);
        crQ.select(builder.count(root.get("itemID")));
        crQ.where(builder.and(builder.equal(root.get("itemCategory"),category),
                              builder.equal(root.get("itemStatus"), "ACTIVE")));
        Query q = session.createQuery(crQ);

        return (Long)q.getSingleResult();
    }

    public static List<String> getCategoryNames(Session session) {
        List<String> categoryNames = new ArrayList<>();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ToyItem> crQ = builder.createQuery(ToyItem.class);
        Root<ToyItem> root = crQ.from(ToyItem.class);
        crQ.distinct(true).select(root.get("itemCategory"));

        Query q = session.createQuery(crQ);
        categoryNames = q.getResultList();


        return categoryNames;
    }
}
