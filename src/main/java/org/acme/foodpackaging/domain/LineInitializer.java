package org.acme.foodpackaging.domain;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.acme.foodpackaging.domain.ProductType;

public class LineInitializer {
    public static List<Line> createLines() {
        List<Line> lines = new ArrayList<>();

        // Line 1
        lines.add(new Line("L1", "Линия 1", createProductionRates(
                Map.of(
                        ProductType.PLUSH, 200,
                        ProductType.CACTUS, 200,
                        ProductType.CLASSIC, 200
                )
        )));

        // Line 4
        lines.add(new Line("L4", "Линия 4", createProductionRates(
                Map.of(
                        ProductType.PLUSH, 220,
                        ProductType.ROD, 198,
                        ProductType.CLASSIC, 220
                )
        )));

        // Line 5 и 6
        for (int i = 5; i <= 6; i++) {
            lines.add(new Line("L" + i, "Линия " + i, createProductionRates(
                    Map.of(
                            ProductType.ROD, 198,
                            ProductType.CLASSIC, 220
                    )
            )));
        }

        return lines;
    }

    private static Map<ProductType, Integer> createProductionRates(Map<ProductType, Integer> rates) {
        EnumMap<ProductType, Integer> fullRates = new EnumMap<>(ProductType.class);
        // Устанавливаем 0 для отсутствующих типов
        for (ProductType type : ProductType.values()) {
            fullRates.put(type, rates.getOrDefault(type, 0));
        }
        return fullRates;
    }
}
