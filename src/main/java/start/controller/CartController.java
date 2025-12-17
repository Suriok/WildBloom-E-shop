package start.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import start.model.Cart;
import start.model.CartItem;
import start.model.Product;
import start.service.CartService;
import start.service.UserService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final BigDecimal DPH_RATE = new BigDecimal("0.21");  // 21%
    private static final BigDecimal SHIPPING = new BigDecimal("50.00"); // doprava
    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getCart(Principal principal) {
        Long customerId = currentUserId(principal);
        Cart cart = cartService.getOrCreateCart(customerId);
        return ResponseEntity.ok(toDto(cart));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> addItem(Principal principal, @RequestBody AddItemRequest req) {
        try {
            Long customerId = currentUserId(principal);
            int amount = (req.amount == null ? 1 : req.amount);
            Cart cart = cartService.addItem(customerId, req.productId, amount);
            return ResponseEntity.ok(toDto(cart));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/items/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> updateAmount(Principal principal,
                                          @PathVariable Long productId,
                                          @RequestBody UpdateAmountRequest req) {
        try {
            Long customerId = currentUserId(principal);
            int amount = (req.amount == null ? 1 : req.amount);
            Cart cart = cartService.updateItemQuantity(customerId, productId, amount);
            return ResponseEntity.ok(toDto(cart));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> removeItem(Principal principal, @PathVariable Long productId) {
        try {
            Long customerId = currentUserId(principal);
            Cart cart = cartService.removeItem(customerId, productId);
            return ResponseEntity.ok(toDto(cart));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long currentUserId(Principal principal) {
        String email = principal.getName();
        return userService.getByEmail(email).getUserId();
    }

    private CartResponse toDto(Cart cart) {
        BigDecimal subtotal = cart.gettotalAmount() == null ? BigDecimal.ZERO : cart.gettotalAmount();

        BigDecimal vat = subtotal.multiply(DPH_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shipping = subtotal.compareTo(BigDecimal.ZERO) > 0 ? SHIPPING : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(vat).add(shipping).setScale(2, RoundingMode.HALF_UP);

        List<CartItemResponse> items = new ArrayList<>();
        int count = 0;

        for (CartItem it : cart.getitem()) {
            Product p = it.getproduct();
            BigDecimal price = p.getPrice() == null ? BigDecimal.ZERO : p.getPrice();
            int amount = it.getamount();
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(amount)).setScale(2, RoundingMode.HALF_UP);

            items.add(new CartItemResponse(
                    p.getproductId(),
                    p.getName(),
                    p.getDescription(),
                    price.setScale(2, RoundingMode.HALF_UP),
                    amount,
                    lineTotal
            ));
            count += amount;
        }

        return new CartResponse(cart.getcartId(), items, count, subtotal, vat, shipping, total);
    }

    // --- DTOs ---

    public static class AddItemRequest {
        public Long productId;
        public Integer amount;
    }

    public static class UpdateAmountRequest {
        public Integer amount;
    }

    public static class CartItemResponse {
        public Long productId;
        public String name;
        public String description;
        public BigDecimal price;
        public int amount;
        public BigDecimal lineTotal;

        public CartItemResponse(Long productId, String name, String description, BigDecimal price, int amount, BigDecimal lineTotal) {
            this.productId = productId;
            this.name = name;
            this.description = description;
            this.price = price;
            this.amount = amount;
            this.lineTotal = lineTotal;
        }
    }

    public static class CartResponse {
        public Long cartId;
        public List<CartItemResponse> items;
        public int itemsCount;          // общее кол-во штук (сумма amount)
        public BigDecimal subtotal;     // сумма товаров
        public BigDecimal vat;          // 21%
        public BigDecimal shipping;     // 50 если не пусто
        public BigDecimal total;        // итого

        public CartResponse(Long cartId, List<CartItemResponse> items, int itemsCount,
                            BigDecimal subtotal, BigDecimal vat, BigDecimal shipping, BigDecimal total) {
            this.cartId = cartId;
            this.items = items;
            this.itemsCount = itemsCount;
            this.subtotal = subtotal;
            this.vat = vat;
            this.shipping = shipping;
            this.total = total;
        }
    }
}
