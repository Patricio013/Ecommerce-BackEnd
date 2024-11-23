package com.ecomerce.demo.Config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MultipartFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String contentType = httpRequest.getContentType();
            if (contentType != null && contentType.contains("multipart/form-data") && contentType.contains("charset=UTF-8")) {
                // Eliminar charset=UTF-8 del encabezado Content-Type
                String fixedContentType = contentType.replace(";charset=UTF-8", "");
                request.setAttribute("Content-Type", fixedContentType);
            }
        }
        chain.doFilter(request, response);
    }
}