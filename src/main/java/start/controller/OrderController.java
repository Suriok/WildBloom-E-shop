package start.controller;

import start.model.Order;
import start.model.OrderStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import start.service.OrderService;
import start.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Order> createOrder(Principal principal) {
        String email = principal.getName();
        Long userId = userService.getByEmail(email).getUserId();
        return ResponseEntity.ok(orderService.createOrderFromCart(userId));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMINISTRATOR')")
    public ResponseEntity<Void> cancelOrder(Principal principal, @PathVariable Long id) {
        String email = principal.getName();
        Long userId = userService.getByEmail(email).getUserId();
        orderService.cancelOrder(userId, id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMINISTRATOR')")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.changeStatus(id, status));
    }
}
