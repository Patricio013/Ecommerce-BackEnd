package com.ecomerce.demo.Services;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecomerce.demo.Clases.Categorias;
import com.ecomerce.demo.Clases.Producto;
import com.ecomerce.demo.Clases.Usuarios;
import com.ecomerce.demo.Repositorys.CategoriasRepository;
import com.ecomerce.demo.Repositorys.ProductoRepository;
import com.ecomerce.demo.Request.ProductoRequest;
import com.ecomerce.demo.Response.CategoriaProdResponse;
import com.ecomerce.demo.Response.ProductoResponse;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public ProductoResponse crearProducto(ProductoRequest productoRequest, MultipartFile imagen) throws IOException {
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
        if (!imagen.isEmpty()) {
            Path directorioImagenes = Paths.get("demo//src/imagenes");
            String rutaAbsoluta = directorioImagenes.toFile().getAbsolutePath();
            byte[] bytesIMG = imagen.getBytes();
            Path rutaCompleta = Paths.get(rutaAbsoluta + "//" + imagen.getOriginalFilename());
            Files.write(rutaCompleta, bytesIMG);
            producto.setImagenUrl(imagen.getOriginalFilename());
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

    public ProductoResponse actualizarProducto(long id, ProductoRequest productoRequest) {
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

    public void subirImagen(long id, MultipartFile imagen) throws IOException{
        Producto producto = productoRepository.findById(id);
        String nombreArchivo = System.currentTimeMillis() + "_" + imagen.getOriginalFilename();
        Path rutaArchivo = Paths.get("uploads/" + nombreArchivo);
        Files.createDirectories(rutaArchivo.getParent());
        Files.write(rutaArchivo, imagen.getBytes());
        String urlImagen = "http://localhost:4002/uploads/" + nombreArchivo;
        producto.setImagenUrl(urlImagen);
        productoRepository.save(producto);
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
        return new ProductoResponse(
            producto.getId(),
            producto.getTitulo(),
            producto.getPrecio(),
            producto.PrecioDescuento(),
            producto.getDescripcion(),
            producto.getStock(),
            producto.getDescuento(),
            producto.getEstadoDescuento(),
            producto.getImagenUrl(),
            categorias
        );
    }

}