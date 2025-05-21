package org.acme.foodpackaging.bootstrap;

import org.acme.foodpackaging.domain.*;

import java.sql.*;
import java.time.*;
import java.util.*;

public class ImportOrderData {


    private static final int LINE_COUNT = 6;
    private static final int DEFAULT_PRIORITY = 0;

    private static final Map<String, Boolean> IS_ALLERGEN = Map.of(
            "4810268043727", true,
            "4810268043475", true,
            "4810268054969", true,
            "4810268056826", true
    );

    public PackagingSchedule scheduleInitializer(String date){

        final LocalDate START_DATE = LocalDate.parse(date);
        final LocalDateTime START_DATE_TIME = LocalDateTime.of(START_DATE, LocalTime.MIDNIGHT);
        final LocalDate END_DATE = START_DATE.plusDays(1);
        final LocalDateTime END_DATE_TIME = LocalDateTime.of(END_DATE, LocalTime.MIDNIGHT);

        PackagingSchedule solution = new PackagingSchedule();

        solution.setWorkCalendar(new WorkCalendar(START_DATE, END_DATE));

        // Инициализация линий
        List<Line> lines = LineInitializer.createLines();

        // Загрузка продуктов и заданий из БД
        List<Product> products = new ArrayList<>();
        List<Job> jobs = new ArrayList<>();

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
                        String ean13 = resultSet.getString("EAN13");
                        String snm = resultSet.getString("SNM");
                        String name = resultSet.getString("NAME");

                        Product product = createProduct(ean13, name);
                        products.add(product);

                        // Создание задания
                        Job job = createJob(
                                ean13,
                                product,
                                quantity,
                                DEFAULT_PRIORITY,
                                START_DATE_TIME
                        );
                        jobs.add(job);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage()); // Выводим стек ошибок для отладки
        }

        solution.setLines(lines);
        solution.setProducts(products);
        solution.setJobs(jobs);
        return solution;
    }

    private Product createProduct(String id, String name) {
        ProductType type = determineProductType(name);
        return new Product(id, name, type, IS_ALLERGEN.getOrDefault(id, false));
    }

    private ProductType determineProductType(String productName) {
        String lowerName = productName.toLowerCase();

        if (containsAll(lowerName, "творобушки", "флоупак")) return ProductType.ROD;
        if (containsAll(lowerName, "топ", "флоупак")) return ProductType.ROD;
        if (containsAll(lowerName, "фольга")) return ProductType.PLUSH;
        if (containsAll(lowerName, "кактус")) return ProductType.CACTUS;
        return ProductType.CLASSIC;
    }

    private boolean containsAll(String text, String... keywords) {
        for (String kw : keywords) {
            if (!text.contains(kw)) return false;
        }
        return true;
    }

    private Job createJob(String id, Product product, int quantity, int priority, LocalDateTime startDate) {
        return new Job(
                id,
                product.getName() + " #" + id,
                product,
                quantity,
                startDate,
                startDate.plusHours(4), // Идеальное время завершения
                startDate.plusHours(8), // Максимальное время завершения
                priority,
                false
        );

    }
}
