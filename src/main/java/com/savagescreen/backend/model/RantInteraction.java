package com.savagescreen.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "rant_interactions", uniqueConstraints = @UniqueConstraint(
        name = "uk_rant_interaction_user_kind", columnNames = {"rant_id", "user_id", "kind"}))
public class RantInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rant_id", nullable = false)
    private Rant rant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false, length = 16)
    private String kind;

    @Column(name = "reaction_emoji", length = 16)
    private String value;

    protected RantInteraction() {
    }

    public RantInteraction(Rant rant, UserAccount user, String kind, String value) {
        this.rant = rant;
        this.user = user;
        this.kind = kind;
        this.value = value;
    }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
