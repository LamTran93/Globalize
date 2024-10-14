package com.project.booking_platform.controller;

import com.project.booking_platform.dto.auth.*;
import com.project.booking_platform.model.Admin;
import com.project.booking_platform.model.Guest;
import com.project.booking_platform.model.Owner;
import com.project.booking_platform.model.RefreshToken;
import com.project.booking_platform.service.database.*;
import com.project.booking_platform.service.email.EmailService;
import com.project.booking_platform.utils.enums.Status;
import com.project.booking_platform.utils.generate.RandomID;
import com.project.booking_platform.utils.token_handling.TokenUtils;
import io.jsonwebtoken.Claims;
import jakarta.persistence.Column;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final TokenUtils tokenUtils;
    private final GuestService guestService;
    private final AdminService adminService;
    private final OwnerService ownerService;
    private final RefreshTokenService refreshTokenService;
    private final ModelMapper modelMapper;
    private final EmailService emailService;

    public AuthController(TokenUtils tokenUtils, GuestService guestService, AdminService adminService, OwnerService ownerService, RefreshTokenService refreshTokenService, ModelMapper modelMapper, EmailService emailService) {
        this.tokenUtils = tokenUtils;
        this.guestService = guestService;
        this.adminService = adminService;
        this.ownerService = ownerService;
        this.refreshTokenService = refreshTokenService;
        this.modelMapper = modelMapper;
        this.emailService = emailService;
    }

    @PostMapping("/register/guest")
    public ResponseEntity<GuestDTO> registerGuest(@RequestBody RegisterInfo body) {
        try {
            // Parse guest info
            Guest guest = modelMapper.map(body, Guest.class);
            guest.setId(RandomID.generateRandomID());
            guest.setStatus(Status.UNVERIFIED);
            guest.setCreatedAt(Date.from(Instant.now()));
            guest.setLastModifiedDate(Date.from(Instant.now()));
            guest.setIsVerified(false);
            String code = RandomID.generateRandomVerificationCode();
            guest.setVerificationCode(code);

            // Check username and email
            if (guestService.checkGuest(guest.getUsername(), guest.getEmail())) {
                return ResponseEntity.badRequest().build();
            }
            GuestDTO guestDTO = modelMapper.map(guestService.save(guest), GuestDTO.class);

            // Send email
            emailService.sendEmail(guest.getEmail(), "Verification Code", "Your verification code is: " + code);

            return ResponseEntity.created(null).body(guestDTO);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/register/owner")
    public ResponseEntity<OwnerDTO> registerOwner(@RequestBody RegisterOwnerInfo body) {
        try {
            // Parse owner info
            Owner owner = modelMapper.map(body, Owner.class);
            owner.setId(RandomID.generateRandomID());
            owner.setStatus(Status.UNVERIFIED);
            owner.setCreatedAt(Date.from(Instant.now()));
            owner.setLastModifiedDate(Date.from(Instant.now()));
            owner.setIsVerified(false);
            String code = RandomID.generateRandomVerificationCode();
            owner.setVerificationCode(code);

            // Check username and email
            if (ownerService.checkOwner(owner.getUserName(), owner.getEmail())) {
                return ResponseEntity.badRequest().build();
            }
            OwnerDTO ownerDTO = modelMapper.map(ownerService.save(owner), OwnerDTO.class);

            // Send email
            emailService.sendEmail(owner.getEmail(), "Verification Code", "Your verification code is: " + code);

            return ResponseEntity.created(null).body(ownerDTO);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("verify/{actor}/{id}/{code}")
    public ResponseEntity<HashMap<String, String>> verify(@PathVariable String actor, @PathVariable String id, @PathVariable String code) {
        try {
            switch (actor.toLowerCase()) {
                case "guest":
                    Optional<Guest> guest = guestService.getGuestById(id);
                    if (guest.isEmpty()) return ResponseEntity.notFound().build();
                    var newGuest = guest.get();
                    if (!newGuest.getVerificationCode().equals(code)) return ResponseEntity.badRequest().build();
                    newGuest.setIsVerified(true);
                    newGuest.setStatus(Status.ACTIVE);
                    guestService.save(newGuest);
                    break;
                case "owner":
                    Optional<Owner> owner = ownerService.getOwnerById(id);
                    if (owner.isEmpty()) return ResponseEntity.notFound().build();
                    var newOwner = owner.get();
                    if (!newOwner.getVerificationCode().equals(code)) return ResponseEntity.badRequest().build();
                    newOwner.setIsVerified(true);
                    newOwner.setStatus(Status.ACTIVE);
                    ownerService.update(newOwner);
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<HashMap<String, String>> login(@RequestBody LoginInfo body, HttpServletResponse response) {

        // Check if the login info is correct
        String id = "";
        switch (body.type.actor.toLowerCase()) {
            case "guest":
                Optional<Guest> guest = guestService.login(body.username, body.password);
                if (guest.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                if (!guest.get().getIsVerified()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                id = guest.get().getId();
                break;
            case "admin":
                Optional<Admin> admin = adminService.login(body.username, body.password);
                if (admin.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                if (!admin.get().getIsVerified()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                id = admin.get().getId();
                break;
            case "owner":
                Optional<Owner> owner = ownerService.login(body.username, body.password);
                if (owner.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                if (!owner.get().getIsVerified()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                id = owner.get().getId();
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        // Create tokens
        String token = tokenUtils.createToken(id, body.type);
        String refreshToken = tokenUtils.createRefreshToken(id, body.type);

        // Save refresh token
        refreshTokenService.addRefreshToken(new RefreshToken(refreshToken));

        // Set cookies
        Cookie tokenCookie = new Cookie("tokenFor" + upper(body.type.actor), token);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(true);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(60*60*12);
        response.addCookie(tokenCookie);

        Cookie refreshTokenCookie = new Cookie("refreshTokenFor" + upper(body.type.actor), refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(60*60*24*24);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(formTokens(token, refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody TokensHolder holder, HttpServletResponse response) {
        // Get refresh token
        String refreshToken = holder.refreshToken;

        // Deactivate refresh token
        refreshTokenService.deactivateRefreshToken(refreshToken);

        // Set cookies
        var payload = tokenUtils.decodeRefreshToken(refreshToken);
        HashMap<String, String> type = (HashMap<String, String>) payload.get("type");
        var actor = upper(type.get("actor"));
        Cookie tokenCookie = new Cookie("tokenFor" + actor, "deleted");
        tokenCookie.setMaxAge(0);
        tokenCookie.setPath("/");
        response.addCookie(tokenCookie);

        Cookie refreshTokenCookie = new Cookie("refreshTokenFor" + actor, "deleted");
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<HashMap<String, String>> refresh(@RequestBody TokensHolder holder, HttpServletResponse response) {
        // Get refresh token
        String refreshToken = holder.refreshToken;

        // Check if refresh token is valid
        if (!refreshTokenService.isTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Generate new tokens
        Claims claims = tokenUtils.decodeRefreshToken(refreshToken);
        String newToken = tokenUtils.regenerateToken(claims);
        String newRefreshToken = tokenUtils.regenerateRefreshToken(claims);

        return ResponseEntity.ok(formTokens(newToken, newRefreshToken));
    }

    private HashMap<String, String> formTokens(String token, String refreshToken) {
        HashMap<String, String> tokenHolder = new HashMap<>();
        tokenHolder.put("token", token);
        tokenHolder.put("refreshToken", refreshToken);
        return tokenHolder;
    }

    private String upper(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static class TokensHolder {
        public String token;
        public String refreshToken;
    }
}
