package org.acme.foodpackaging.bootstrap;

import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.Product;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExporter {

    public ExcelExporter(String date, List<Job> jobs) {
       importDataFromDB(date, jobs);
    }

   private String  formatTime(Duration duration){
       long totalMinutes = duration.toMinutes();
       long hours = totalMinutes / 60;
       long minutes = totalMinutes % 60;
       return String.format("%02d:%02d", hours, minutes);
    }
    private void importDataFromDB(String date, List<Job> jobs) {
        String url = "jdbc:sqlserver://10.30.0.108;databaseName=prommark;integratedSecurity=true;encrypt=true;trustServerCertificate=true";
        String sqlQuery = "SELECT TOP (1000) [KMC], [NP], [DTS], [NKOLE], [MRPL], [DTE], [LINEID], [STP_AVT] " +
                "FROM [prommark].[dbo].[PM_ASSCC] " +
                "WHERE KMC LIKE ? AND DTF = ? ORDER BY NP";
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement ps = connection.prepareStatement(sqlQuery);
             Workbook workbook = new XSSFWorkbook()) {

            ps.setString(1, "0307060162%");
            ps.setString(2, date + "T00:00:00");

            Sheet sheet = workbook.createSheet("Data");
            // Заголовок
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("NP");
            headerRow.createCell(1).setCellValue("KMC");
            headerRow.createCell(2).setCellValue("Количество (факт)");
            headerRow.createCell(3).setCellValue("Количество (планировщик)");
            headerRow.createCell(4).setCellValue("Время старта выполнения (факт)");
            headerRow.createCell(5).setCellValue("Время завершения фасовки (факт)");
            headerRow.createCell(6).setCellValue("Продолжительность фасовки (факт)");
            headerRow.createCell(7).setCellValue("Продолжительность фасовки (планировщик)");

            try (ResultSet rs = ps.executeQuery()) {
                // Данные
                int rowIdx = 1;
                while (rs.next()) {
                    String kmc = rs.getString("KMC");
                    String np = rs.getString("NP");
                    String nkole = rs.getString("NKOLE");
                    LocalDateTime dts = rs.getTimestamp("DTS").toLocalDateTime();
                    LocalDateTime dte = rs.getTimestamp("DTE").toLocalDateTime();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                    Duration jobDuration = Duration.between(dts, dte);

                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(np);
                    row.createCell(1).setCellValue(kmc);
                    row.createCell(2).setCellValue(nkole);
                    row.createCell(4).setCellValue(dts.format(formatter));
                    row.createCell(5).setCellValue(dte.format(formatter));
                    row.createCell(6).setCellValue(formatTime(jobDuration));
                    for(Job job : jobs){
                        if(job.getNp().equals(np)){
                            row.createCell(3).setCellValue(job.getQuantity());
                            row.createCell(7).setCellValue(formatTime(job.getDuration()));
                        }
                    }

                }
                // Автоширина колонок
                for (int i = 0; i < 7; i++) {
                    sheet.autoSizeColumn(i);
                }
                // Сохранение Excel-файла
                try (FileOutputStream fos = new FileOutputStream("src/main/resources/excelExport/" + date + ".xlsx")) {
                    workbook.write(fos);
                    System.out.println("✅ Данные успешно экспортированы в Excel:" + "src/main/resources/excelExport/" + date + ".xlsx");
                }
            }

        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

}
