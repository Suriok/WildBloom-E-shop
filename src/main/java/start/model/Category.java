package start.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @OrderBy("name ASC")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Product> producty = new ArrayList<>();

    public Category() {}

    public Long getcategoryId() { return categoryId; }
    public String getname() { return name; }
    public void setname(String name) { this.name = name; }
    public List<Product> getproducty() { return producty; }
}
