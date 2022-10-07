package sample;

import com.monitorjbl.xlsx.StreamingReader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.*;

import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;


public class Controller {

    private static final Logger logger = LogManager.getLogger(Controller.class);

    @FXML
    public Button convertTxt;
    @FXML
    private Label totalRow;
    @FXML
    private Label labelNameUpload;
    @FXML
    private Button btnUpload;

    Stage primaryStage;

    File file;

    public void pressButton(ActionEvent actionEvent) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Excel (.xlsx)", "*.xlsx"),
                    new FileChooser.ExtensionFilter("Excel (.xls)", "*.xls"));
            file = fileChooser.showOpenDialog(null);
            if (file != null) {
                labelNameUpload.setText(file.getAbsolutePath());
                convertTxt.setDisable(false);
            } else {
                convertTxt.setDisable(true);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void saveFileConvert(ActionEvent actionEvent) throws IOException {
        IOUtils.setByteArrayMaxOverride(500000000);
        StringBuilder sb = new StringBuilder();
        String excelFilePath = file.toString();
        FileInputStream fileInput = new FileInputStream(file);
        Workbook workbook = null;
        if (excelFilePath.endsWith("xlsx")) {
            workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(fileInput);
        } else if (excelFilePath.endsWith("xls")) {
            workbook = (Workbook) new HSSFWorkbook(fileInput);
        }

        long dateStart = System.currentTimeMillis();
        System.out.println("start " + dateStart);
        Sheet sheet = workbook.getSheetAt(0);
        int totalRowSheet = sheet.getLastRowNum();
        totalRow.setText(String.valueOf(totalRowSheet));
        List<Integer> lstIdxMapCheck = new ArrayList<>();
        try {
            Iterator<Row> iterator = sheet.iterator();
            while (iterator.hasNext()) {
                Row nextRow = iterator.next();
                int rowNum = nextRow.getRowNum();
                if (rowNum < 7) {
                    continue;
                }
                if (rowNum == 7){
                    getHeader(nextRow, lstIdxMapCheck);
                    continue;
                }

                if (processDataContent(nextRow, sb, lstIdxMapCheck)) {
                    return;
                }
            }
            workbook.close();
            fileInput.close();
            long endDate = System.currentTimeMillis();
            System.out.println("end " + endDate);
            Float total = Float.valueOf(endDate - dateStart) / 1000;
            System.out.println("total " + total + " giây");
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("TXT files (.txt)", ".txt"));
            fileChooser.setTitle("Save your files");
            File fileTxt = fileChooser.showSaveDialog(primaryStage);
            if (fileTxt != null) {
                try {
                    saveTextToFile(sb.toString(), fileTxt);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void saveTextToFile(String content, File file) {
        Writer out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), "UTF-8"));
            out.write(content);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                logger.error(e.getMessage(),e);
            }
        }
    }

    /**
     * @param row
     * @param sb
     * @param lst
     * @return true => data error
     */
    private boolean processDataContent(Row row, StringBuilder sb, List<Integer> lst) {
        Iterator<Cell> cellIterator = row.cellIterator();
        int countCheck = 0;
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            if (lst.contains(countCheck)) {
                if (processCheckCell(row, countCheck)) {
                    showErrorMessenger();
                    return true;
                }
            }
            switch (cell.getCellType()) {
                case STRING:
                    sb.append(cell.getStringCellValue()).append("|");
                    break;
                case NUMERIC:
                    String convertString = convertDoubleToString(String.valueOf(cell.getNumericCellValue()));
                    sb.append(convertString).append("|");
                    break;
                default:
                    sb.append(cell.getStringCellValue()).append("|");
            }
            countCheck++;
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("\n");
        return false;
    }

    /**
     * @param row
     * @param idx
     * @return true => is null
     */
    private boolean processCheckCell(Row row, int idx) {
        Cell cellCheck = row.getCell(idx);
        boolean flag = false;
        switch (cellCheck.getCellType()) {
            case STRING:
                if (Objects.isNull(cellCheck.getStringCellValue()) || cellCheck.getStringCellValue().isEmpty()) {
                    flag = true;
                }
                break;
            case NUMERIC:
                if (Objects.isNull(cellCheck.getNumericCellValue())) {
                    flag = true;
                }
                break;
            case BLANK:
                flag = true;
                break;
        }
        return flag;
    }


    private void getHeader(Row row, List<Integer> lst) {
        int countCellHeader = 0;
        lst.clear();
        for (Cell cell : row) {
            if (cell.getCellType() == CellType.STRING) {
                String dtCell = cell.getStringCellValue();
                if (dtCell.endsWith("(*)")) {
                    lst.add(countCellHeader);
                }
            }
            countCellHeader++;
        }
    }

    private void showErrorMessenger() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText("Dữ liệu bị lỗi. Vui lòng kiểm tra lại!");
        alert.showAndWait();
    }

    private static String convertDoubleToString(String value){
        List<String> stringList = Arrays.asList(value.split("\\."));
        if (stringList.get(1).length() > 1){
            return value;
        }
        if (Integer.parseInt(stringList.get(1))>0){
            return value;
        }
        return stringList.get(0);
    }

}