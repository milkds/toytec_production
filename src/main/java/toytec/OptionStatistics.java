package toytec;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class OptionStatistics {

    private Instant start;
    private Instant finish;

    public OptionStatistics() {
        start = Instant.now();
    }

    public String buildReport(List<OptionChangeKeeper> changeList) {
        finish = Instant.now();
        StringBuilder report = new StringBuilder();
        changeList.forEach(keepr-> appendItem(keepr, report));
        appendVisualSeparator(report);
        appendTime(report);

        return report.toString();
    }

    private void appendTime(StringBuilder report) {
        report.append("Parse started at: ");
        report.append(Statistics.formatTime(start));
        report.append(System.lineSeparator());
        report.append("Parse finished at: ");
        report.append(Statistics.formatTime(finish));
        report.append(System.lineSeparator());
        report.append("Total Elapsed time in minutes: ");
        report.append(Duration.between(start, finish).toMinutes());
        report.append(System.lineSeparator());
    }

    private static void appendItem(OptionChangeKeeper keepr, StringBuilder report) {
        ToyItem item = keepr.getItemWithChanges();
        appendVisualSeparator(report);
        appendItemDetails(item, report);
        appendOptions(keepr.getAddedOptions(), report, "ADDED");
        appendOptions(keepr.getDeletedOptions(), report, "DELETED");
        appendOptionChanges(keepr, report);
    }

    private static void appendOptionChanges(OptionChangeKeeper keepr, StringBuilder report) {
        Map<ToyOption,String> redTextMap = keepr.getRedTextMap();
        Map<ToyOption,BigDecimal> priceMap = keepr.getPriceMap();
        if  (redTextMap.size()==0&&priceMap.size()==0){
            return;
        }
        if (redTextMap.size()>0){
            appendRedTextChanges(redTextMap, report);
        }
        if (priceMap.size()>0){
            appendPriceChanges(priceMap, report);
        }

    }

    private static void appendPriceChanges(Map<ToyOption, BigDecimal> priceMap, StringBuilder report) {
        //appending title
        report.append(System.lineSeparator());
        report.append("CHANGED PRICE:");
        report.append(System.lineSeparator());

        //sorting options by group
        Map<String, List<ToyOption>> sortedOptionsMap = sortOptions(new ArrayList<>(priceMap.keySet())); //k = options group name

        //appending changes by groups
        sortedOptionsMap.forEach((k,v)->{
            report.append(k);
            report.append(System.lineSeparator());
            v.forEach(option->{
                report.append(option.getOptionName());
                report.append(System.lineSeparator());
                report.append("OLD VALUE: ");
                BigDecimal oldPrice = option.getPrice();
                if (oldPrice==null){
                   oldPrice = new BigDecimal(0);
                }
                report.append(oldPrice.doubleValue());
                report.append("$");
                report.append(System.lineSeparator());
                report.append("NEW VALUE: ");
                BigDecimal newPrice = priceMap.get(option); //new price is always not null - will be 0, if not present
                report.append(newPrice.doubleValue());
                report.append("$");
                report.append(System.lineSeparator());
            });
        });
    }

    private static void appendRedTextChanges(Map<ToyOption, String> redTextMap, StringBuilder report) {
        //appending title
        report.append(System.lineSeparator());
        report.append("CHANGED RED TEXT:");
        report.append(System.lineSeparator());

        //sorting options by group
        Map<String, List<ToyOption>> sortedOptionsMap = sortOptions(new ArrayList<>(redTextMap.keySet())); //k = options group name

        //appending changes by groups
        sortedOptionsMap.forEach((k,v)->{
            report.append(k);
            report.append(System.lineSeparator());
            v.forEach(option->{
                report.append(option.getOptionName());
                report.append(System.lineSeparator());
                report.append("OLD VALUE: ");
                String oldRedText = option.getRedText();
                if (oldRedText==null||oldRedText.length()==0){
                    oldRedText = "NO RED TEXT";
                }
                report.append(oldRedText);
                report.append(System.lineSeparator());
                report.append("NEW VALUE: ");
                String newRedText = redTextMap.get(option);
                if (newRedText.length()==0){
                    newRedText = "NO RED TEXT";
                }
                report.append(newRedText);
                report.append(System.lineSeparator());
            });
        });
    }

    private static void appendOptions(List<ToyOption> options, StringBuilder report, String title) {
        if (options.size()>0){
            report.append(System.lineSeparator());
            report.append(title);
            report.append(" OPTIONS:");
            report.append(System.lineSeparator());
            Map<String, List<ToyOption>> optionMap = sortOptions(options); //k = OptionGroup name, v = list of options for this group
            optionMap.forEach((k,v)->{
                report.append(k);
                v.forEach(option->{
                    report.append(System.lineSeparator());
                    report.append(option.getOptionName());
                    String redText = option.getRedText();
                    if (redText!=null&&redText.length()>0){
                        report.append("  ");
                        report.append(redText);
                    }
                    report.append(System.lineSeparator());
                    BigDecimal price = option.getPrice();
                    if (price!=null&&price.doubleValue()>0){
                        report.append("Price: ");
                        report.append(price.doubleValue());
                        report.append("$");
                        report.append(System.lineSeparator());
                    }
                });
            });
        }
    }

    private static Map<String, List<ToyOption>> sortOptions(List<ToyOption> addedOptions) {
        Map<String, List<ToyOption>> optionMap = new HashMap<>();
        addedOptions.forEach(option->{
            String opGroup = option.getOptionGroup();
            if (optionMap.containsKey(opGroup)){
                optionMap.get(opGroup).add(option);
            }
            else {
                List<ToyOption> groupList = new ArrayList<>();
                groupList.add(option);
                optionMap.put(opGroup, groupList);
            }
        });

        return optionMap;
    }

    private static void appendItemDetails(ToyItem item, StringBuilder report) {
        report.append(System.lineSeparator());
        report.append(item.getSku());
        report.append(System.lineSeparator());
        report.append(getCanadaSitelink(item.getSku()));
        report.append(System.lineSeparator());
        report.append(item.getItemName());
        report.append(System.lineSeparator());

        report.append(item.getItemLink());
        report.append(System.lineSeparator());
    }

    private static String getCanadaSitelink(String sku) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://www.bilsteinlifts.com/?post_type=product&s=");
        sb.append(sku);
        sb.append("&asp_active=1&p_asid=1&p_asp_data=Y3VycmVudF9wYWdlX2lkPTk3Mjg4Jndvb19jdXJyZW5jeT1VU0QmcXRyYW5zbGF0ZV9sYW5nPTAmZmlsdGVyc19jaGFuZ2VkPTAmZmlsdGVyc19pbml0aWFsPTEmYXNwX2dlbiU1QiU1RD10aXRsZSZhc3BfZ2VuJTVCJTVEPWNvbnRlbnQmYXNwX2dlbiU1QiU1RD1leGNlcnB0JmN1c3RvbXNldCU1QiU1RD1wcm9kdWN0X3ZhcmlhdGlvbiZjdXN0b21zZXQlNUIlNUQ9cHJvZHVjdA==");

        return sb.toString();
    }

    private static void appendVisualSeparator(StringBuilder report) {
        report.append(System.lineSeparator());
        report.append("------------------------------");
        report.append(System.lineSeparator());
    }

    public Instant getStart() {
        return start;
    }
    public void setStart(Instant start) {
        this.start = start;
    }
    public Instant getFinish() {
        return finish;
    }
    public void setFinish(Instant finish) {
        this.finish = finish;
    }
}
