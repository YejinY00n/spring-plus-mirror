package org.example.expert.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.expert.config.security.CustomUserDetails;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.entity.Timestamped;
import org.example.expert.domain.image.entity.Image;
import org.example.expert.domain.user.enums.UserRole;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String email;
    private String nickname;
    private String password;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name="image_id")
    @Setter
    private Image profile;

    public User(String email, String password, UserRole userRole, String nickname) {
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.nickname = nickname;
    }

    private User(Long id, String email, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
    }

    public static User fromAuthUser(AuthUser authUser) {
        return new User(authUser.getId(), authUser.getEmail(), authUser.getUserRole());
    }

    public static User fromAuthDetails(CustomUserDetails userDetails) {
        return new User(userDetails.getId(), userDetails.getNickname(), userDetails.getUserRole());
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void updateRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public void removeProfile() { this.profile = null; }
}
