package toytec;

import org.hibernate.Session;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptionMatcher {

    private String itemLink;

    public OptionMatcher(String itemLink) {
        this.itemLink = itemLink;
    }

    public OptionChangeKeeper matchOptions(List<ToyOption> optionList, Session session) {
        OptionChangeKeeper result = new OptionChangeKeeper();
        //list needed because we sometimes have items with same link, but from different categories
        List<ToyItem> itemsToCheck = ToyDao.getItemsByWebLink(itemLink, session);

        //getting options
        List<ToyOption> newOptions = new ArrayList<>(optionList);
        ToyItem firstItem = itemsToCheck.get(0); //at least one item with options will always be present.
        List<ToyOption> oldOptions = firstItem.getOptions();

        List<ToyOption> addedOptions = getAddedOptions(newOptions, oldOptions);
        List<ToyOption> deletedOptions = getDeletedOptions(newOptions, oldOptions);

        ComparisonHolder holder = compareOptionValues(newOptions, oldOptions);
        if (noChangesInOptions(addedOptions, deletedOptions, holder)){
            return result;
        }

        //if no changes detected - we will return zero keeper before this method
        saveChangesToDB(itemsToCheck, newOptions, session);
        result = buildMatchKeeper(firstItem, addedOptions, deletedOptions, holder);

        return result;
    }

    private OptionChangeKeeper buildMatchKeeper(ToyItem firstItem, List<ToyOption> addedOptions, List<ToyOption> deletedOptions, ComparisonHolder holder) {
        OptionChangeKeeper result = new OptionChangeKeeper();

        result.setHasChanges(true);
        result.setAddedOptions(addedOptions);
        result.setAddedOptions(deletedOptions);
        result.setPriceMap(holder.getPriceMap());
        result.setRedTextMap(holder.getRedTextMap());
        result.setItemWithChanges(firstItem);

        return result;
    }

    private boolean noChangesInOptions(List<ToyOption> addedOptions, List<ToyOption> deletedOptions, ComparisonHolder holder) {
        if (addedOptions.size()>0){
            return false;
        }
        if (deletedOptions.size()>0){
            return false;
        }
        if (holder.getRedTextMap().size()>0){
            return false;
        }
        return holder.getPriceMap().size() == 0;
    }

    private void saveChangesToDB(List<ToyItem> itemsToCheck, List<ToyOption> newOptions, Session session) {
        itemsToCheck.forEach(item->{
            item.setOptions(newOptions);
            ToyDao.updateItemWithOptions(session, item);
        });
    }

    private ComparisonHolder compareOptionValues(List<ToyOption> newOptions, List<ToyOption> oldOptions) {
        ComparisonHolder holder = new ComparisonHolder();
        Map<ToyOption, String> redTextMap = new HashMap<>();
        Map<ToyOption, BigDecimal> priceMap = new HashMap<>();
        oldOptions.forEach(oldOption->{
            ToyOption newOption = null;
            String optionGroup = oldOption.getOptionGroup();
            String optionName = oldOption.getOptionName();
            for (ToyOption iteratedOpt : newOptions) {
                if (iteratedOpt.getOptionGroup().equals(optionGroup) && iteratedOpt.getOptionName().equals(optionName)) {
                    newOption = iteratedOpt;
                    break;
                }
            }
            if (newOption!=null){
                //getting values to compare
                String newRedText = newOption.getRedText();
                String oldRedText = oldOption.getRedText();
                BigDecimal newPrice = newOption.getPrice();
                BigDecimal oldPrice = oldOption.getPrice();

                //checking for null values
                if (newRedText==null){
                    newRedText = "";
                }
                if (oldRedText==null){
                    oldRedText = "";
                }
                if (newPrice==null){
                    newPrice = new BigDecimal(0);
                }
                if (oldPrice==null){
                    oldPrice = new BigDecimal(0);
                }

                if (!newRedText.equals(oldRedText)){
                    redTextMap.put(oldOption, newRedText);
                }

                if (newPrice.compareTo(oldPrice)!=0){
                    priceMap.put(oldOption, newPrice);
                }

            }
        });

        holder.setPriceMap(priceMap);
        holder.setRedTextMap(redTextMap);

        return holder;
    }

    private List<ToyOption> getDeletedOptions(List<ToyOption> newOptions, List<ToyOption> oldOptions) {
        List<ToyOption> result = new ArrayList<>();
        oldOptions.forEach(oldOption->{
            boolean isDeleted = true;
            String oldOptGroup = oldOption.getOptionGroup();
            String oldOptText = oldOption.getOptionName();
            for (ToyOption newOption : newOptions) {
                if (newOption.getOptionGroup().equals(oldOptGroup) && newOption.getOptionName().equals(oldOptText)) {
                    isDeleted = false;
                    break;
                }
            }
            if (isDeleted){
                result.add(oldOption);
            }
        });

        return result;
    }

    private List<ToyOption> getAddedOptions(List<ToyOption> newOptions, List<ToyOption> oldOptions) {
        List<ToyOption> result = new ArrayList<>();
        newOptions.forEach(newOption->{
            boolean isNew = true;
            String newOptGroup = newOption.getOptionGroup();
            String newOptText = newOption.getOptionName();
            for (ToyOption oldOption : oldOptions) {
                if (oldOption.getOptionGroup().equals(newOptGroup) && oldOption.getOptionName().equals(newOptText)) {
                    isNew = false;
                    break;
                }
            }
            if (isNew){
                result.add(newOption);
            }
        });

        return result;
    }

    private class ComparisonHolder {
        //k = old Option, v = new Red Text
        Map<ToyOption, String> redTextMap;
        //k = old Option, v = new Price
        Map<ToyOption, BigDecimal> priceMap;

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
    }
}
