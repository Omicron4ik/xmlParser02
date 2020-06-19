package ru.radionov;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class mdmCreating {

    public static void fillmdm (List<Device> list, String resultFilePath) throws IOException {

        properties properties = new properties();
        properties.loadConfig();

        XSSFWorkbook mdm = new XSSFWorkbook();
        XSSFSheet sheet = mdm.createSheet("DeviceMdm");

        int rownum = 0;
        Cell cell;
        Row row;

        row = sheet.createRow(rownum);

        cell = row.createCell(0, CellType.STRING);
        cell.setCellValue("№ п/п");

        cell = row.createCell(1, CellType.STRING);
        cell.setCellValue("Адаптер-измеритель*");

        cell = row.createCell(2, CellType.STRING);
        cell.setCellValue("МО*");

        cell = row.createCell(3, CellType.STRING);
        cell.setCellValue("Идентификатор*");

        cell = row.createCell(4, CellType.STRING);
        cell.setCellValue("Тип*");

        cell = row.createCell(5, CellType.STRING);
        cell.setCellValue("Наименование*");

        cell = row.createCell(6, CellType.STRING);
        cell.setCellValue("Адрес*");

        cell = row.createCell(7, CellType.STRING);
        cell.setCellValue("GeoJSON");

        //FillData
        for (Device device : list) {
            rownum++;
            row = sheet.createRow(rownum);

            cell = row.createCell(0, CellType.STRING);
            cell.setCellValue(rownum-1);
            cell = row.createCell(1, CellType.STRING);
            cell.setCellValue(properties.getAdapterName());
            cell = row.createCell(2, CellType.STRING);
            cell.setCellValue(properties.getOktmo());
            cell = row.createCell(3, CellType.STRING);
            cell.setCellValue(device.getId());
            cell = row.createCell(4, CellType.STRING);
            cell.setCellValue(properties.getDeviceType());
            cell = row.createCell(5, CellType.STRING);
            cell.setCellValue(device.getName());
            cell = row.createCell(6, CellType.STRING);
            cell.setCellValue(device.getAddress());
            cell = row.createCell(7, CellType.STRING);
            cell.setCellValue("{\"type\":\"Point\", \"coordinates\": [" + device.getLon() + "," + device.getLat() + "]}");
        }
        File file = new File(resultFilePath);
        file.getParentFile().mkdirs();
        if (file.exists()) file.delete();
        file.createNewFile();

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        mdm.write(fileOutputStream);
        fileOutputStream.close();
        System.out.println("file " + resultFilePath  + " created");
    }
}
