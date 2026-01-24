package start.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
@NamedQuery(
        name = "Category.findByName",
        query = "SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name)"
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_seq_gen")
    @SequenceGenerator(name = "category_seq_gen", sequenceName = "category_id_seq", allocationSize = 1)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("name ASC")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Product> products = new ArrayList<>();

    public Category() {}

    public Long getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Product> getProducts() { return products; }
}
