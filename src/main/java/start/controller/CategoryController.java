package start.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import start.dao.CategoryDao;
import start.model.Category;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryDao categoryDao;

    public CategoryController(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @GetMapping
    public List<Category> getAll() {
        return categoryDao.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public Category create(@RequestBody Category category) {
        categoryDao.persist(category);
        return category;
    }
}