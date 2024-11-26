package com.ecomerce.demo.Services;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecomerce.demo.Clases.Categorias;
import com.ecomerce.demo.Clases.Producto;
import com.ecomerce.demo.Clases.Usuarios;
import com.ecomerce.demo.Repositorys.CategoriasRepository;
import com.ecomerce.demo.Repositorys.ProductoRepository;
import com.ecomerce.demo.Request.ModProdRequest;
import com.ecomerce.demo.Request.ProductoRequest;
import com.ecomerce.demo.Response.CategoriaProdResponse;
import com.ecomerce.demo.Response.ProductoResponse;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private CategoriasRepository categoriasRepository;

    public List<ProductoResponse> obtenerProductosPorUsuario() {
        List<Producto> productos = productoRepository.findAllWithStock();
        return productos.stream()
                .map(this::mapearAProductoResponse) 
                .collect(Collectors.toList());
    }

    public List<ProductoResponse> ProductosCreados(){
        Usuarios usuario = authenticationService.obtenerUsuarioAutenticado();
        List<Producto> productos = productoRepository.findAllByAdminId(usuario.getId());
        return productos.stream()
                .map(this::mapearAProductoResponse) 
                .collect(Collectors.toList());
    }

    public ProductoResponse obtenerProducto(long productId){
        Producto producto = productoRepository.findById(productId);
        return mapearAProductoResponse(producto);
    }

    public ProductoResponse crearProducto(ProductoRequest productoRequest) throws IOException {
        Producto producto = new Producto();
        producto.setTitulo(productoRequest.getTitulo());
        producto.setPrecio(productoRequest.getPrecio());
        producto.setDescripcion(productoRequest.getDescripcion());
        producto.setStock(productoRequest.getStock());
        if (productoRequest.getEstadoDescuento()){
            producto.setDescuento(productoRequest.getDescuento());
        } else {
            producto.setDescuento(0.00);
        }
        producto.setEstadoDescuento(productoRequest.getEstadoDescuento());
        if (productoRequest.getImagenBase64() != null && !productoRequest.getImagenBase64().isEmpty()) {
            String base64Data = productoRequest.getImagenBase64().split(",")[1]; 
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
            Path directorioImagenes = Paths.get("src/main/java/com/ecomerce/demo/static/imagenes");
            String rutaAbsoluta = directorioImagenes.toFile().getAbsolutePath();
            String nombreArchivo = UUID.randomUUID() + ".jpg"; 
            Path rutaCompleta = Paths.get(rutaAbsoluta + "//" + nombreArchivo);
            Files.write(rutaCompleta, decodedBytes);
            producto.setImagenUrl(nombreArchivo);
        }
        Usuarios usuario = authenticationService.obtenerUsuarioAutenticado();
        producto.setUsuario(usuario);
        Set<Categorias> categorias = new HashSet<>();
        for (long categoriaId : productoRequest.getCategoriasIds()) {
            Categorias categoria = categoriasRepository.findById(categoriaId);
            categorias.add(categoria);
        }
        producto.setCategorias(categorias);
        productoRepository.save(producto);
        return mapearAProductoResponse(producto);
    }

    public ProductoResponse actualizarProducto(long id, ModProdRequest productoRequest) {
        Producto producto = productoRepository.findById(id);
        producto.setTitulo(productoRequest.getTitulo());
        producto.setPrecio(productoRequest.getPrecio());
        producto.setDescripcion(productoRequest.getDescripcion());
        producto.setStock(productoRequest.getStock());
        producto.setDescuento(productoRequest.getDescuento());
        producto.setEstadoDescuento(productoRequest.getEstadoDescuento()); 
        productoRepository.save(producto);
        return mapearAProductoResponse(producto);
    }

    public void eliminarProducto(long id) {
        Producto producto = productoRepository.findById(id);
        productoRepository.delete(producto);
    }

    public void subirImagen(long id, String imagen) throws IOException{
        Producto producto = productoRepository.findById(id);
        if (imagen != null && !imagen.isEmpty()) {
            if (producto.getImagenUrl() != null) {
                Path directorioImagenes = Paths.get("src/main/java/com/ecomerce/demo/static/imagenes");
                String rutaAbsoluta = directorioImagenes.toFile().getAbsolutePath();
                Path rutaAntigua = Paths.get(rutaAbsoluta + "//" + producto.getImagenUrl());
                if (Files.exists(rutaAntigua)) {
                    Files.delete(rutaAntigua);
                }
            }
    
            String base64Data = imagen.contains(",") ? imagen.split(",")[1] : imagen;
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
            Path directorioImagenes2 = Paths.get("src/main/java/com/ecomerce/demo/static/imagenes");
            if (!Files.exists(directorioImagenes2)) {
                Files.createDirectories(directorioImagenes2);
            }
            String nombreArchivo = UUID.randomUUID() + ".jpg";
            Path rutaCompleta = directorioImagenes2.resolve(nombreArchivo);
            Files.write(rutaCompleta, decodedBytes);
            producto.setImagenUrl(nombreArchivo);
            productoRepository.save(producto);
        }
    }

    public void eliminarImagen(long id) {
        Producto producto = productoRepository.findById(id);
        producto.setImagenUrl(null);; 
        productoRepository.save(producto);
    }

    public ProductoResponse agregarCategoria(long productoId, long categoriaId) {
        Producto producto = productoRepository.findById(productoId);
        Categorias categoria = categoriasRepository.findById(categoriaId);
        producto.getCategorias().add(categoria);
        productoRepository.save(producto);
        return mapearAProductoResponse(producto);
    }

    public ProductoResponse quitarCategoria(long productoId, long categoriaId) {
        Producto producto = productoRepository.findById(productoId);
        Categorias categoria = categoriasRepository.findById(categoriaId);
        if (producto.getCategorias().contains(categoria)) {
            producto.getCategorias().remove(categoria); 
            productoRepository.save(producto); 
        } else {
            throw new IllegalStateException("La categoría no está asociada al producto");
        }
        return mapearAProductoResponse(producto);
    }

    private ProductoResponse mapearAProductoResponse(Producto producto) {
        Set<CategoriaProdResponse> categorias = new HashSet<>();
        List<Long> categoriasId = productoRepository.findCategoriaIdsByProductoId(producto.getId());
        for (long id: categoriasId){
            Categorias categoria = categoriasRepository.findById(id);
            CategoriaProdResponse auxiliar = new CategoriaProdResponse();
            auxiliar.setId(categoria.getId());
            auxiliar.setNombre(categoria.getNombre());
            categorias.add(auxiliar);
        }
        String ImagenUrl = null;
        if (producto.getImagenUrl() != null) {
            try {
                Path rutaImagen = Paths.get("src/main/java/com/ecomerce/demo/static/imagenes/" + producto.getImagenUrl());
                byte[] imagenBytes = Files.readAllBytes(rutaImagen);
                String imagenBase64 = Base64.getEncoder().encodeToString(imagenBytes);
                ImagenUrl = "data:image/jpeg;base64," + imagenBase64;
            } catch (IOException e) {
                throw new RuntimeException("Error al leer la imagen: " + e.getMessage());
            }
        }
        return new ProductoResponse(
            producto.getId(),
            producto.getTitulo(),
            producto.getPrecio(),
            producto.PrecioDescuento(),
            producto.getDescripcion(),
            producto.getStock(),
            producto.getDescuento(),
            producto.getEstadoDescuento(),
            ImagenUrl,
            categorias
        );
    }

}