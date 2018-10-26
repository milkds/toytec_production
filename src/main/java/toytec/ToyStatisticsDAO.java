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
        crQ.where(builder.isNull(root.get("itemStatus")));
        Query q = session.createQuery(crQ);

        return (Long)q.getSingleResult();
    }

    public static long getItemsQuantityByCategory(Session session, String category) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> crQ = builder.createQuery(Long.class);
        Root<ToyItem> root = crQ.from(ToyItem.class);
        crQ.select(builder.count(root.get("itemID")));
        crQ.where(builder.and(builder.equal(root.get("itemCategory"),category),builder.isNull(root.get("itemStatus"))));
        Query q = session.createQuery(crQ);

        return (Long)q.getSingleResult();
    }
}