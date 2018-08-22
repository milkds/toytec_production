package toytec;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "toytec_item_options")
public class ToyOption {

    @Id
    @Column(name = "OPTION_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int optionID;

    @Column(name = "ITEM_ID")
    private int itemID;

    @Column(name = "OPTION_GROUP")
    private String optionGroup;

    @Column(name = "OPTION_NAME")
    private String optionName;

    @Column(name = "OPTION_PRICE")
    private BigDecimal price;

    @Override
    public String toString() {
        return "ToyOption{" +
                "optionID=" + optionID +
                ", itemID=" + itemID +
                ", optionGroup='" + optionGroup + '\'' +
                ", optionName='" + optionName + '\'' +
                ", price=" + price +
                '}';
    }

    public int getOptionID() {
        return optionID;
    }

    public void setOptionID(int optionID) {
        this.optionID = optionID;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public String getOptionGroup() {
        return optionGroup;
    }

    public void setOptionGroup(String optionGroup) {
        this.optionGroup = optionGroup;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
