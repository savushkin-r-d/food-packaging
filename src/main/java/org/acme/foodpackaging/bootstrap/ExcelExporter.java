package org.acme.foodpackaging.bootstrap;

import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.Product;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;

public class ExcelExporter {

    String[][] data = {
            {"ID", "Имя", "Оценка"},
            {"1", "Алиса", "95"},
            {"2", "Боб", "88"},
            {"3", "Чарли", "92"}
    };

    public ExcelExporter() {
       importDataFromDB();
    }

    private void export(Workbook workbook, Sheet sheet) {
        for (int i = 0; i < data.length; i++) {
            Row row = sheet.createRow(i); // ✔️ правильный Row
            for (int j = 0; j < data[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(data[i][j]);
            }
        }

        for (int i = 0; i < data[0].length; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fos = new FileOutputStream("src/main/resources/report.xlsx")) {
            workbook.write(fos);
            workbook.close();
            System.out.println("Excel файл успешно создан: report.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void importDataFromDB() {
        String url = "jdbc:sqlserver://10.30.0.108;databaseName=prommark;integratedSecurity=true;encrypt=true;trustServerCertificate=true";
        String sqlQuery = "SELECT TOP (1000) [KMC], [NP], [DTS], [NKOLE], [MRPL], [DTE], [LINEID], [STP_AVT] " +
                "FROM [prommark].[dbo].[PM_ASSCC] " +
                "WHERE KMC LIKE ? AND DTF = ? ORDER BY DTE";
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement ps = connection.prepareStatement(sqlQuery);
             Workbook workbook = new XSSFWorkbook()) {

            ps.setString(1, "0307060162%");
            ps.setString(2, "2025-06-15T00:00:00");

            try (ResultSet rs = ps.executeQuery()) {

                Sheet sheet = workbook.createSheet("Data");
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                // Заголовки
                Row headerRow = sheet.createRow(0);
                for (int i = 1; i <= columnCount; i++) {
                    Cell cell = headerRow.createCell(i - 1);
                    cell.setCellValue(metaData.getColumnLabel(i));
                }

                // Данные
                int rowIdx = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(rowIdx++);
                    for (int i = 1; i <= columnCount; i++) {
                        Cell cell = row.createCell(i - 1);
                        cell.setCellValue(rs.getString(i));
                    }
                }

                // Автоширина колонок
                for (int i = 0; i < columnCount; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Сохранение Excel-файла
                try (FileOutputStream fos = new FileOutputStream("src/main/resources/export_from_db.xlsx")) {
                    workbook.write(fos);
                    System.out.println("✅ Данные успешно экспортированы в Excel: export_from_db.xlsx");
                }
            }

        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

}
