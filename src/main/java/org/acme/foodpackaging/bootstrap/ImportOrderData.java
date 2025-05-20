package org.acme.foodpackaging.bootstrap;

import org.acme.foodpackaging.domain.PackagingSchedule;
import org.acme.foodpackaging.domain.Product;
import org.acme.foodpackaging.domain.WorkCalendar;
import org.acme.foodpackaging.domain.ProductType*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.sql.*;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class ImportOrderData {
    private PackagingSchedule ScheduleInitializer(String date){

        private const int lineCount =6;
        List<Product> products = new ArrayList<>();
        final LocalDate START_DATE = LocalDate.parse(date);
        final LocalDateTime START_DATE_TIME = LocalDateTime.of(START_DATE, LocalTime.MIDNIGHT);
        final LocalDate END_DATE = START_DATE.plusDays(1);
        final LocalDateTime END_DATE_TIME = LocalDateTime.of(END_DATE, LocalTime.MIDNIGHT);

        PackagingSchedule solution = new PackagingSchedule();

        solution.setWorkCalendar(new WorkCalendar(START_DATE, END_DATE));

        List<String> tworobushkiKernel = List.of("творобушки", "флоупак", "40", "г");
        List<String> topKernel = List.of("топ", "флоупак", "40", "г");
        List<String> plush = List.of( "фольга", "45", "г");
        List<String> kaktus = List.of( "кактус", "флоупак", "40", "г");
        List<String> classic = List.of("флоупак", "40", "г");

        List<String> kernelList;
        List<String> classicList;
        List<String> plushList;
        List<String> cactusList;

        String url = "jdbc:sqlserver://10.164.30.246;databaseName=MES;integratedSecurity=true;encrypt=true;trustServerCertificate=true";

        String sqlQuery = "SELECT v.KSK, v.SNPZ, v.DTI, v.DTM, v.KMC, v.EMK, v.KOLMV, v.MASSA, v.KOLEV, v.NP, v.UX, "
                + "m.MASSA, m.EAN13, m.SNM, m.NAME "
                + "FROM [MES].[dbo].[BD_VZPMC] as v, NS_MC as m "
                + "WHERE (v.KMC = m.KMC) AND (v.DTI = ?) AND (v.KSK = ?) AND (m.MASSA < ?) "
                + "ORDER BY v.SNPZ";

        try {
            // Установка соединения
            try (Connection connection = DriverManager.getConnection(url);
                 PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

                // Установка параметров для SQL-запроса
                preparedStatement.setString(1, date + "T00:00:00");     // Параметр для v.DTI
                preparedStatement.setString(2, "0119030000");          // Параметр для v.KSK
                preparedStatement.setDouble(3, 0.1);                  // Параметр для m.MASSA

                // Выполнение запроса
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    // Обработка результата

                    while (resultSet.next()) {
                        String snpz = resultSet.getString("SNPZ");
                        String ksk = resultSet.getString("KSK");
                        String dti = resultSet.getString("DTI");
                        String dtm = resultSet.getString("DTM");
                        String kmc = resultSet.getString("KMC");
                        String emk = resultSet.getString("EMK");
                        int kolmv = resultSet.getInt("KOLMV");
                        String vb = resultSet.getString("MASSA"); // MASSA из таблицы BD_VZPMC
                        int quantity= resultSet.getInt("KOLEV");
                        String np = resultSet.getString("NP");
                        String priority = resultSet.getString("UX");
                        double massaM = resultSet.getDouble("MASSA"); // MASSA из таблицы NS_MC
                        String id = resultSet.getString("EAN13");
                        String snm = resultSet.getString("SNM");
                        String name = resultSet.getString("NAME");

                        ProductType type;
                        if(topKernel.stream().allMatch(name.toLowerCase()::contains) ||
                                tworobushkiKernel.stream().allMatch(name.toLowerCase()::contains)){
                            type = ProductType.ROD;
                        }
                        else if(plush.stream().allMatch(name.toLowerCase()::contains)){
                            type = ProductType.PLUSH;
                        }
                        else if(kaktus.stream().allMatch(name.toLowerCase()::contains)){
                            type = ProductType.CACTUS;
                        }
                        else {
                            type = ProductType.CLASSIC;
                        }
                        
                        List<Product> products = new ArrayList<>();
                        products.add(new Product(id,name, type));
                                

                    }
                    solution.setProducts(products);
                }
            }


        } catch (SQLException e) {
            System.err.println(e.getMessage()); // Выводим стек ошибок для отладки
        }
        return solution;
    }
   

    
    }
}
