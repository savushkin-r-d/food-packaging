package org.acme.foodpackaging.domain;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class DurationProvider {

    private final Map<LineProductKey, Duration> durationMap = new HashMap<>();

    public DurationProvider() {
        put("1", ProductType.CLASSIC, 200);
        put("2", ProductType.CLASSIC, 196);
        put("3", ProductType.CLASSIC, 206);
        put("4", ProductType.CLASSIC, 220);
        put("5", ProductType.CLASSIC, 220);
        put("6", ProductType.CLASSIC, 220);

        put("1", ProductType.CACTUS, 200);
        put("2", ProductType.CACTUS, 196);
        put("3", ProductType.CACTUS, 206);

        put("4", ProductType.ROD, 198);
        put("5", ProductType.ROD, 198);
        put("6", ProductType.ROD, 198);

        put("1", ProductType.PLUSH, 200);
    }

    private void put(String lineId, ProductType productType, int speedPerHour) {
        durationMap.put(new LineProductKey(lineId, productType), Duration.ofMinutes(60L * 1000 / speedPerHour));
    }

    public Duration getDuration(Line line, ProductType productType, int quantity) {
        Duration perUnit = durationMap.get(new LineProductKey(line.getId(), productType));
        return (perUnit == null) ? null : perUnit.multipliedBy(quantity);
    }

    public Duration calculateDuration(Line line, Product product, int quantity) {
        if (line == null || product == null) return null;
        return getDuration(line, product.getType(), quantity);
    }

    public boolean isLineSupported(ProductType type, String lineId) {
        return durationMap.containsKey(new LineProductKey(lineId, type));
    }

}
