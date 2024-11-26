package com.ecomerce.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecomerce.demo.Request.ImagenRequest;
import com.ecomerce.demo.Request.ModProdRequest;
import com.ecomerce.demo.Request.ProductoRequest;
import com.ecomerce.demo.Response.ProductoResponse;
import com.ecomerce.demo.Services.ProductoService;

import java.io.IOException;
import java.util.List;


@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("productosAdmin")
public class ProductoAdminController {
    @Autowired
    private ProductoService productoService;

    @PostMapping
    public ResponseEntity<ProductoResponse> crearProducto(@RequestBody ProductoRequest productoRequest) throws IOException{
        ProductoResponse producto = productoService.crearProducto(productoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }

    @GetMapping("/Todos")
    public ResponseEntity<List<ProductoResponse>> productosSubidos() {
        return ResponseEntity.ok(productoService.ProductosCreados());
    }

    @GetMapping("/PorProducto")
    public ResponseEntity<ProductoResponse> obtenerProductoPorId(@RequestParam("id") long id) {
        ProductoResponse producto = productoService.obtenerProducto(id);
        return ResponseEntity.ok(producto);
    }
    
    @PutMapping("/actualizar")
    public ResponseEntity<ProductoResponse> actualizarProducto(@RequestParam long id, @RequestBody ModProdRequest productoRequest) {
        ProductoResponse producto = productoService.actualizarProducto(id, productoRequest);
        return ResponseEntity.ok(producto);
    }

    @DeleteMapping("/borrar")
    public ResponseEntity<Void> eliminarProducto(@RequestParam long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/subir/imagen")
    public ResponseEntity<String> subirImagen(@RequestParam long id, @RequestBody ImagenRequest imagen2) throws IOException{
        try {
            productoService.subirImagen(id, imagen2.getImagen());
            return ResponseEntity.ok("Imagen subida correctamente");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir la imagen: " + e.getMessage());
        }
    }

    @DeleteMapping("/borrar/imagen")
    public ResponseEntity<Void> eliminarImagen(@RequestParam long id) {
        productoService.eliminarImagen(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/AgregarCat")
    public ResponseEntity<ProductoResponse> agregarCategoria2(@RequestParam long productId, @RequestParam long categoriaId){
        ProductoResponse producto = productoService.agregarCategoria(productId, categoriaId);
        return ResponseEntity.ok(producto);
    }

    @DeleteMapping("/BorrarCat")
    public ResponseEntity<ProductoResponse> borrarCategoria(@RequestParam long productId, @RequestParam long categoriaId){
        ProductoResponse producto = productoService.quitarCategoria(productId, categoriaId);
        return ResponseEntity.ok(producto);
    }
}
