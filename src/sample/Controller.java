package sample;

import com.monitorjbl.xlsx.StreamingReader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;

public class Controller {

    public Button convertTxt;
    @FXML
    private Label totalRow;
    @FXML
    private Label labelNameUpload;

    Stage primaryStage;

    File file;

//    public Controller(Stage primaryStage) {
//        this.primaryStage = primaryStage;
//    }

    public void handleClick(MouseEvent mouseEvent) {
        System.out.println(mouseEvent.getX());
        System.out.println(mouseEvent.getY());
    }

    public void pressButton(ActionEvent actionEvent) {
        System.out.println("nghiant");
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel xlsx", "*.xlsx"));
        file = fileChooser.showOpenDialog(null);
        if (file != null) {
            labelNameUpload.setText(file.getAbsolutePath());
        }
    }

    public void saveFileConvert(ActionEvent actionEvent) throws IOException {
        IOUtils.setByteArrayMaxOverride(500000000);
        StringBuilder sb = new StringBuilder();
        InputStream fileInput = new FileInputStream(file);
        Workbook workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(fileInput);

        long dateStart = System.currentTimeMillis();
        System.out.println("start " + dateStart);
        Sheet sheet = workbook.getSheetAt(0);
        int totalRowSheet = sheet.getLastRowNum();
        totalRow.setText(String.valueOf(totalRowSheet));
        try {
            Iterator<Row> iterator = sheet.iterator();
            while (iterator.hasNext()) {
                Row nextRow = iterator.next();
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

//            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
//            fileChooser.getExtensionFilters().add(extFilter);

            File fileTxt = fileChooser.showSaveDialog(primaryStage);
            if (fileTxt != null) {
                try {
                    saveTextToFile(sb.toString(), fileTxt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void saveTextToFile(String content, File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            writer.println(content);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
