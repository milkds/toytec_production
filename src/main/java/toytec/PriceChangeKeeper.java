package toytec;

import java.math.BigDecimal;

//this class is bean for statistics
public class PriceChangeKeeper {

    private ToyItem updatedItem;
    private BigDecimal oldPriceFrom;
    private BigDecimal oldPriceTo;

    public ToyItem getUpdatedItem() {
        return updatedItem;
    }

    public void setUpdatedItem(ToyItem updatedItem) {
        this.updatedItem = updatedItem;
    }

    public BigDecimal getOldPriceFrom() {
        return oldPriceFrom;
    }

    public void setOldPriceFrom(BigDecimal oldPriceFrom) {
        this.oldPriceFrom = oldPriceFrom;
    }

    public BigDecimal getOldPriceTo() {
        return oldPriceTo;
    }

    public void setOldPriceTo(BigDecimal oldPriceTo) {
        this.oldPriceTo = oldPriceTo;
    }
}
