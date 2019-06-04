package toytec;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OptionChangeKeeper {
    private boolean hasChanges = false;
    private List<ToyOption> addedOptions;
    private List<ToyOption> deletedOptions;
    private Map<ToyOption, String> redTextMap;
    private Map<ToyOption, BigDecimal> priceMap;
    private ToyItem itemWithChanges;

    public boolean hasChanges() {
        return hasChanges;
    }
    public void setHasChanges(boolean hasChanges) {
        this.hasChanges = hasChanges;
    }
    public List<ToyOption> getAddedOptions() {
        return addedOptions;
    }
    public void setAddedOptions(List<ToyOption> addedOptions) {
        this.addedOptions = addedOptions;
    }
    public List<ToyOption> getDeletedOptions() {
        return deletedOptions;
    }
    public void setDeletedOptions(List<ToyOption> deletedOptions) {
        this.deletedOptions = deletedOptions;
    }
    public Map<ToyOption, String> getRedTextMap() {
        return redTextMap;
    }
    public void setRedTextMap(Map<ToyOption, String> redTextMap) {
        this.redTextMap = redTextMap;
    }
    public Map<ToyOption, BigDecimal> getPriceMap() {
        return priceMap;
    }
    public void setPriceMap(Map<ToyOption, BigDecimal> priceMap) {
        this.priceMap = priceMap;
    }
    public ToyItem getItemWithChanges() {
        return itemWithChanges;
    }
    public void setItemWithChanges(ToyItem itemWithChanges) {
        this.itemWithChanges = itemWithChanges;
    }
}
