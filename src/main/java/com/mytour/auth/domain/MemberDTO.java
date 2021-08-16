package com.mytour.auth.domain;

import com.mytour.auth.util.BaseTimeEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "member")
@Getter
@Setter
public class MemberDTO {

    @Id
    @Column(name = "username",
            unique = true,
            nullable = false,
            length = 20)
    private String username;

    @Column(unique = true, nullable = false, length = 50)
    private String email;

    @Column(nullable = false, length = 300)
    private String password;

    @Column(nullable = true, length = 20)
    private String phone;

    @Column(length = 20)
    private String name;

    private int age;

    @Column(length = 3)
    private String gender;

    @Column(length = 300)
    private String imagePath;

    @Column(length = 150)
    private String salt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "member_role",
            joinColumns = @JoinColumn(name = "username"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    private Date created;

    private Date modified;


    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.modified = new Date();
    }

}
