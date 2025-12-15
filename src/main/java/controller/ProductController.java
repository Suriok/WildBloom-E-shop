package controller;

import dao.ProductDao;
import model.Product;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductDao productDao;

    public ProductController(ProductDao productDao) {
        this.productDao = productDao;
    }

    @GetMapping
    public List<Product> getAll() {
        return productDao.findAll();
    }

    @GetMapping("/search")
    public List<Product> search(@RequestParam(required = false) String name,
                                @RequestParam(required = false) BigDecimal minPrice,
                                @RequestParam(required = false) BigDecimal maxPrice) {
        return productDao.searchProducts(name, minPrice, maxPrice);
    }
}