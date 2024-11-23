package com.ecomerce.demo.Request;

import java.util.Set;
import lombok.Data;

@Data
public class ProductoRequest {
    private String titulo;
    private Double precio;
    private String descripcion;
    private int stock;
    private Double descuento;
    private Boolean estadoDescuento;
    private Set<Long> categoriasIds;
}
