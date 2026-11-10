package com.fashion.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public record VariantRequest(
    @JsonAlias("product_id") Integer productId,
    String sku,
    String size,
    String color,
    BigDecimal price,
    @JsonAlias("sale_price") BigDecimal salePrice,
    Integer stock
) {
    public Map<String,Object> toMap() {
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("productId",productId);
        result.put("sku",sku);
        result.put("size",size);
        result.put("color",color);
        result.put("price",price);
        result.put("salePrice",salePrice);
        result.put("stock",stock);
        return result;
    }
}
