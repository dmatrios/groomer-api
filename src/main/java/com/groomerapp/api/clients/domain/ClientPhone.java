package com.groomerapp.api.clients.domain;

import com.groomerapp.api.shared.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "client_phone",
        indexes = {
                @Index(name = "ix_client_phone_client_id", columnList = "client_id"),
                @Index(name = "ix_client_phone_phone", columnList = "phone")
        }
)
public class ClientPhone extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "phone", nullable = false, length = 30)
    private String phone;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public ClientPhone(Long clientId, String phone) {
        this.clientId = clientId;
        this.phone = phone;
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
