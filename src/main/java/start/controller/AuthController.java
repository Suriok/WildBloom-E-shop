package start.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import start.dto.CreateAdministratorDto;
import start.dto.CreateEmployeeDto;
import start.dto.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import start.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // public registration for CUSTOMER
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody UserDto userDto) {
        userService.registerCustomer(userDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // admin creates employee
    @PostMapping("/employees")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> createEmployee(@RequestBody CreateEmployeeDto dto) {
        userService.createEmployee(dto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // admin creates another admin (optional but useful)
    @PostMapping("/admins")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> createAdmin(@RequestBody CreateAdministratorDto dto) {
        userService.createAdministrator(dto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
