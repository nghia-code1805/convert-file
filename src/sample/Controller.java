package sample;

import com.monitorjbl.xlsx.StreamingReader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.Iterator;

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
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"),
                    new FileChooser.ExtensionFilter("Excel (*.xls)", "*.xls"));
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
//            HSSFWorkbook hssfWorkbook = new HSSFWorkbook(fileInput);
        }

        long dateStart = System.currentTimeMillis();
        System.out.println("start " + dateStart);
        Sheet sheet = workbook.getSheetAt(0);
        int totalRowSheet = sheet.getLastRowNum();
        totalRow.setText(String.valueOf(totalRowSheet));
        try {
            Iterator<Row> iterator = sheet.iterator();
            while (iterator.hasNext()) {
                Row nextRow = iterator.next();
                int rowNum = nextRow.getRowNum();
                if (rowNum < 8) {
                    continue;
                }
                Cell cellLLUpload = nextRow.getCell(1);
                String convertLLUpload = String.valueOf(cellLLUpload.getNumericCellValue());
                if (convertLLUpload == null || convertLLUpload.equals("")) {
                    showErrorMessenger();
                    return;
                }
                Cell cellUser = nextRow.getCell(2);
                if (cellUser.getStringCellValue() == null || cellUser.getStringCellValue().equals("")) {
                    showErrorMessenger();
                    return;
                }
                Cell cellDatabase = nextRow.getCell(3);
                if (cellDatabase.getStringCellValue() == null || cellDatabase.getStringCellValue().equals("")) {
                    showErrorMessenger();
                    return;
                }
                Cell cellISDN = nextRow.getCell(5);
                String convertISDN = String.valueOf(cellISDN.getNumericCellValue());
                if (convertISDN == null || convertISDN.equals("")) {
                    showErrorMessenger();
                    return;
                }

                Iterator<Cell> cellIterator = nextRow.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch (cell.getCellType()) {
                        case STRING:
                            sb.append(cell.getStringCellValue() + "|");
                            break;
                        case NUMERIC:
                            sb.append(cell.getNumericCellValue() + "|");
                            break;
                        default:
                            sb.append(cell.getStringCellValue() + "|");
                    }
                }
                sb.append("\n");
            }
            workbook.close();
            fileInput.close();
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"));
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
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            writer.println(content);
            writer.close();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void showErrorMessenger() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText("Dữ liệu bị lỗi. Vui lòng kiểm tra lại!");
        alert.showAndWait();
    }
}
