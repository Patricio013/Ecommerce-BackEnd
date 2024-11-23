package com.ecomerce.demo.Services;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecomerce.demo.Clases.Categorias;
import com.ecomerce.demo.Clases.Producto;
import com.ecomerce.demo.Exceptions.CategoriasDuplicateException;
import com.ecomerce.demo.Repositorys.CategoriasRepository;
import com.ecomerce.demo.Repositorys.ProductoRepository;
import com.ecomerce.demo.Response.CategoriaProdResponse;
import com.ecomerce.demo.Response.CategoriaResponse;
import com.ecomerce.demo.Response.ProductoResponse;

@Service
public class CategoriasService {
    
    @Autowired
    private CategoriasRepository categoriasRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public List<CategoriaResponse> obtenerTodas() {
        List<Categorias> categorias = categoriasRepository.findAll();
        return categoriaResponses(categorias);
    }

    public List<ProductoResponse> filtrado(List<Long> nombres) {
        long id = nombres.get(0);
        Set<Long> resultadoInterseccion = new HashSet<>(categoriasRepository.findProductoIdsByCategoriaId(id));
        for (int i = 1; i < nombres.size(); i++) {
            Set<Long> RecetasActuales = new HashSet<>(categoriasRepository.findProductoIdsByCategoriaId(nombres.get(i)));
            resultadoInterseccion.retainAll(RecetasActuales);
        }
        Set<Producto> recetas = new HashSet<>();
        for (long id2: resultadoInterseccion){
            Producto re = productoRepository.findById(id2);
            recetas.add(re);
        }
        return mapearAProductoResponse(recetas);
    }


    public Categorias createCategoria(String nombre) throws CategoriasDuplicateException{
        List<Categorias> categorias = categoriasRepository.findByNombre(nombre);
        if (categorias.isEmpty())
            return categoriasRepository.save(new Categorias(nombre));
        throw new CategoriasDuplicateException();
    }

    private List<CategoriaResponse> categoriaResponses(List<Categorias> categorias) {
        List<CategoriaResponse> catRes = new ArrayList<>();
        for (int i = 0; i < categorias.size(); i++) {
            Categorias cat = categorias.get(i);
            CategoriaResponse auxiliar = new CategoriaResponse();
            auxiliar.setId(cat.getId());
            auxiliar.setNombre(cat.getNombre());
            Set<Producto> receta = new HashSet<>();
            List<Long> recetasId = categoriasRepository.findProductoIdsByCategoriaId(cat.getId());
            for (long id: recetasId){
                Producto recetas = productoRepository.findById(id);
                receta.add(recetas);
            }
            auxiliar.setCatalogo(mapearAProductoResponse(receta));
            catRes.add(auxiliar);
        }
        return catRes;
    }

    private List<ProductoResponse> mapearAProductoResponse(Set<Producto> productos) {
        List<ProductoResponse> auxiliar = new ArrayList<>();
        for (Producto producto : productos) {
            ProductoResponse productoResponse = new ProductoResponse();
            productoResponse.setId(producto.getId());
            productoResponse.setTitulo(producto.getTitulo());
            productoResponse.setPrecioTotal(producto.getPrecio());
            productoResponse.setPrecioDescuento(producto.PrecioDescuento());
            productoResponse.setDescripcion(producto.getDescripcion());
            productoResponse.setStock(producto.getStock());
            productoResponse.setEstadoDescuento(producto.getEstadoDescuento());
            productoResponse.setDescuento(producto.getDescuento());
            productoResponse.setImagenUrl(producto.getImagenUrl());
            Set<CategoriaProdResponse> categorias = new HashSet<>();
            List<Long> categoriasId = productoRepository.findCategoriaIdsByProductoId(producto.getId());
            for (long id: categoriasId){
                Categorias categoria = categoriasRepository.findById(id);
                CategoriaProdResponse auxiliar2 = new CategoriaProdResponse();
                auxiliar2.setId(categoria.getId());
                auxiliar2.setNombre(categoria.getNombre());
                categorias.add(auxiliar2);
            }
            productoResponse.setCategorias(categorias);
            auxiliar.add(productoResponse);
        }
        return auxiliar;
    }
}