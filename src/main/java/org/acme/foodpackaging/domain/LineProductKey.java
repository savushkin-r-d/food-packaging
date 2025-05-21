package org.acme.foodpackaging.domain;

import java.util.Objects;

public class LineProductKey {


    private final String lineId;
    private final ProductType productType;

    public LineProductKey(String lineId, ProductType productType) {
        this.lineId = lineId;
        this.productType = productType;
    }

    public String getLineId() {
        return lineId;
    }

    public ProductType getProductType() {
        return productType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineProductKey that)) return false;
        return Objects.equals(lineId, that.lineId) &&
                Objects.equals(productType, that.productType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineId, productType);
    }

    @Override
    public String toString() {
        return lineId + "-" + productType;
    }
}
