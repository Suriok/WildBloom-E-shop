package start.model;

import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import javax.annotation.processing.Generated;
import java.math.BigDecimal;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Product.class)
public abstract class Product_ {

    public static volatile SingularAttribute<Product, Long> productId;
    public static volatile SingularAttribute<Product, String> name;
    public static volatile SingularAttribute<Product, String> description;
    public static volatile SingularAttribute<Product, BigDecimal> price;
    public static volatile SingularAttribute<Product, Integer> in_stock;
    public static volatile SingularAttribute<Product, Boolean> availability;
    public static volatile SingularAttribute<Product, Category> category;

    public static final String PRODUCT_ID = "productId";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String PRICE = "price";
    public static final String IN_STOCK = "in_stock";
    public static final String AVAILABILITY = "availability";
    public static final String CATEGORY = "category";
}