package com.mytour.auth.domain;

import com.mytour.auth.util.BaseTimeEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity(name = "member_agree")
@Getter
@Setter
public class MemberAgreeDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 20)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private EAgreement agreement;

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

    public MemberAgreeDTO(String username, EAgreement agreement) {
        this.username = username;
        this.agreement = agreement;
    }

    public MemberAgreeDTO() {

    }
}
