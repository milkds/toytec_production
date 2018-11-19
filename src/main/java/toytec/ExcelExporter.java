package toytec;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {

    private static final String FINAL_FILE_PATH = "src\\main\\resources\\from_db.xlsx";

    public static void saveToExcel(Session session) throws IOException, InvalidFormatException {
        Workbook workbook = writeDBtoExcel(session);
        FileOutputStream fileOut = new FileOutputStream(FINAL_FILE_PATH);
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        workbook.close();
    }

    public static File prepareReportForEmail(Session session){
        Workbook workbook = writeDBtoExcel(session);
        File file = null;
        try {
            file =  File.createTempFile("dbToExcel", ".xlsx");
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return file;
    }

    private static Workbook writeDBtoExcel(Session session) {
        List<ToyItem> items = ToyDao.getAllItems(session);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        //counter used for creating new rows in excel
        Integer counter = 1;
        setFirstRow(sheet);
        for (ToyItem item : items){
            counter = setCells(item,sheet,counter);
        }

        return workbook;
    }

    private static void setFirstRow(Sheet sheet) {
        Row row = sheet.createRow(0);

        Cell cell = row.createCell(0);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("OPTION_ID");

        cell = row.createCell(1);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("ITEM_ID");

        cell = row.createCell(2);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("SKU");

        cell = row.createCell(3);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("ITEM_CATEGORY");

        cell = row.createCell(4);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("ITEM_SUBCATEGORY");

        cell = row.createCell(5);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("ITEM_NAME");

        cell = row.createCell(6);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("PRICE_FROM");

        cell = row.createCell(7);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("PRICE_TO");

        cell = row.createCell(8);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("ITEM_DESC");

        cell = row.createCell(9);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("ITEM_MAIN_IMG");

        cell = row.createCell(10);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("ITEM_IMG_LINKS");

        cell = row.createCell(11);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("ITEM_INSTRUCTION_LINKS");

        cell = row.createCell(12);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("ITEM_LINK");

        cell = row.createCell(13);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("ITEM_CATEGORY_LINK");

        cell = row.createCell(14);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("ITEM_MAKE");

        cell = row.createCell(15);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("ITEM_STOCK_AVAILABILITY");

        cell = row.createCell(16);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("ITEM_STOCK_BACKORDER");

        cell = row.createCell(17);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("META_KEYWORDS");

        cell = row.createCell(18);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("META_DESCRIPTION");

        cell = row.createCell(19);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("OPTION_GROUP");

        cell = row.createCell(20);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("OPTION_NAME");

        cell = row.createCell(21);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("OPTION_PRICE");
    }

    private static Integer setCells(ToyItem item, Sheet sheet, Integer counter) {
        Row row = sheet.createRow(counter);
        counter++;

        Cell cell = row.createCell(1);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getItemID());

        cell = row.createCell(2);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getSku());

        cell = row.createCell(3);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getItemCategory());

        cell = row.createCell(4);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getItemSubCategory());

        cell = row.createCell(5);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getItemName());

        cell = row.createCell(6);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getPriceFrom().toString());

        cell = row.createCell(7);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getPriceTo().toString());

        cell = row.createCell(8);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getDesc());

        cell = row.createCell(9);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getMainImg());

        cell = row.createCell(10);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getImgLinks());

        cell = row.createCell(11);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getInstructions());

        cell = row.createCell(12);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getItemLink());

        cell = row.createCell(13);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getItemCategoryLink());

        cell = row.createCell(14);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getItemMake());

        cell = row.createCell(15);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getAvailability());

        cell = row.createCell(16);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getAvailability());

        cell = row.createCell(17);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getMetaKeywords());

        cell = row.createCell(18);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(item.getMetaDescription());

        List<ToyOption> options = item.getOptions();
        if (options!=null){
            for (ToyOption option: options){
                Row optionRow = sheet.createRow(counter);
                counter++;
                setOptionCells(optionRow,option);
            }
        }
        return counter;
    }

    private static void setOptionCells(Row row, ToyOption option) {
        Cell cell = row.createCell(0);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(option.getOptionID());

        cell = row.createCell(1);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(option.getItem().getItemID());

        cell = row.createCell(19);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(option.getOptionGroup());

        cell = row.createCell(20);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(option.getOptionName());

        if (option.getPrice()!=null){
            cell = row.createCell(21);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(option.getPrice().toString());
        }
    }
}
