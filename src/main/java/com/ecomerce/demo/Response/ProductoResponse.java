package com.ecomerce.demo.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoResponse {
    
    private long id;
    private String titulo;
    private Double precioTotal;
    private Double precioDescuento;
    private String descripcion;
    private int stock;
    private Double descuento;
    private Boolean estadoDescuento;
    private String imagenUrl; 
    private Set<CategoriaProdResponse> categorias;
}
