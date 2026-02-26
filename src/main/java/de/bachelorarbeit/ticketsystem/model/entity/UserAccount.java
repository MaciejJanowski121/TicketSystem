package de.bachelorarbeit.ticketsystem.model.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collection;
import java.util.Collections;

/**
 * User entity representing a user in the system.
 * Implements UserDetails for Spring Security integration.
 * Uses a composite primary key (username + mail).
 */
@Entity
@Table(name = "user_account")
public class UserAccount implements UserDetails {

    @Id
    private String mail;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Default constructor required by JPA
    public UserAccount() {
    }

    public UserAccount(String username, String mail, String passwordHash) {
        this.username = username;
        this.mail = mail;
        this.passwordHash = passwordHash;
        this.role = Role.ENDUSER;
    }

    public UserAccount(String username, String mail, String passwordHash, Role role) {
       this.username = username;
       this.mail = mail;
       this.passwordHash = passwordHash;
       this.role = role;
    }

    // UserDetails implementation

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Getters and setters




    public String getMail() {
        return mail;
    }

    public void setEmail(String mail) {
        this.mail = mail;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccount that = (UserAccount) o;
        return mail != null && mail.equals(that.mail);
    }

    @Override
    public int hashCode() {
        return mail != null ? mail.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "mail=" + mail +
                ", role=" + role +
                '}';
    }
}
