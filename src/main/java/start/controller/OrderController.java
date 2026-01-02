package start.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import start.model.Order;
import start.model.OrderItem;
import start.model.OrderStatus;
import start.service.OrderService;
import start.service.UserService;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    public static class OrderSummaryResponse {
        public Long orderId;
        public String date; // ISO
        public OrderStatus status;
        public BigDecimal totalAmount;
        public BigDecimal dph;
        public BigDecimal dorights;
        public String customerName;
        public String customerEmail;

        public OrderSummaryResponse(Long orderId, String date, OrderStatus status,
                                    BigDecimal totalAmount, BigDecimal dph, BigDecimal dorights, String customerName, String customerEmail) {
            this.orderId = orderId;
            this.date = date;
            this.status = status;
            this.totalAmount = totalAmount;
            this.dph = dph;
            this.dorights = dorights;
            this.customerName = customerName;
            this.customerEmail = customerEmail;
        }
    }

    public static class OrderItemResponse {
        public Long productId;
        public String name;
        public int amount;
        public BigDecimal price;
        public BigDecimal lineTotal;

        public OrderItemResponse(Long productId, String name, int amount, BigDecimal price, BigDecimal lineTotal) {
            this.productId = productId;
            this.name = name;
            this.amount = amount;
            this.price = price;
            this.lineTotal = lineTotal;
        }
    }

    public static class OrderDetailResponse {
        public OrderSummaryResponse order;
        public List<OrderItemResponse> items;

        public OrderDetailResponse(OrderSummaryResponse order, List<OrderItemResponse> items) {
            this.order = order;
            this.items = items;
        }
    }

    private static OrderSummaryResponse toSummary(Order o) {
        String cName = "Guest";
        String cEmail = "No email";

        if (o.getCustomer() != null) {
            cName = o.getCustomer().getName();
            cEmail = o.getCustomer().getEmail();
        }

        String iso = (o.getdate() == null) ? null : Instant.ofEpochMilli(o.getdate().getTime()).toString();
        return new OrderSummaryResponse(
                o.getorderId(),
                iso,
                o.getstatus(),
                o.getTotalAmount(),
                o.getDph(),
                o.getDorights(),
                cName,
                cEmail
        );
    }

    private static OrderItemResponse toItem(OrderItem it) {
        BigDecimal price = it.getPriceSnapshot();
        BigDecimal line = price.multiply(BigDecimal.valueOf(it.getamount()));
        return new OrderItemResponse(
                it.getproduct().getproductId(),
                it.getproduct().getName(),
                it.getamount(),
                price,
                line
        );
    }

    private Long currentUserId(Principal principal) {
        String email = principal.getName();
        return userService.getByEmail(email).getUserId();
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createOrder(Principal principal) {
        try {
            Long userId = currentUserId(principal);
            Order created = orderService.createOrderFromCart(userId);
            return ResponseEntity.ok(toSummary(created));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> myOrders(Principal principal) {
        Long userId = currentUserId(principal);
        List<OrderSummaryResponse> list = orderService.findOrdersForCustomer(userId)
                .stream()
                .map(OrderController::toSummary)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> myOrderDetail(Principal principal, @PathVariable Long id) {
        try {
            Long userId = currentUserId(principal);
            Order o = orderService.getOrderForCustomerWithItems(userId, id);
            OrderDetailResponse dto = new OrderDetailResponse(
                    toSummary(o),
                    o.getitem().stream().map(OrderController::toItem).toList()
            );
            return ResponseEntity.ok(dto);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> cancelOrder(Principal principal, @PathVariable Long id) {
        try {
            Long userId = currentUserId(principal);
            orderService.cancelOrder(userId, id);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMINISTRATOR')")
    public ResponseEntity<OrderSummaryResponse> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        Order updatedOrder = orderService.changeStatus(id, status);
        return ResponseEntity.ok(toSummary(updatedOrder));
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMINISTRATOR')")
    public ResponseEntity<?> orderDetailsForStaff(@PathVariable Long id) {
        try {
            Order o = orderService.getOrderWithItems(id);
            OrderDetailResponse dto = new OrderDetailResponse(
                    toSummary(o),
                    o.getitem().stream().map(OrderController::toItem).toList()
            );
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/whoami")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMINISTRATOR')")
    public Map<String, Object> whoami(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMINISTRATOR"));

        boolean isEmp = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_EMPLOYEE"));

        String role = isAdmin ? "ADMINISTRATOR" : (isEmp ? "EMPLOYEE" : "UNKNOWN");

        return Map.of(
                "email", auth.getName(),
                "role", role
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMINISTRATOR')")
    public ResponseEntity<List<OrderSummaryResponse>> getAllOrders() {
        List<OrderSummaryResponse> list = orderService.findAll()
                .stream()
                .map(OrderController::toSummary)
                .toList();
        return ResponseEntity.ok(list);
    }
}