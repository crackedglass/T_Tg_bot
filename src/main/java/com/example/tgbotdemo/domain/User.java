package com.example.tgbotdemo.domain;

import org.hibernate.annotations.ColumnDefault;

import com.example.tgbotdemo.domain.statemachine.ChatStates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private long userId;
    @Column(name = "username")
    private String username;
    @Column(name = "money")
    private int money;
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private ChatStates state = ChatStates.AUTH;

    @ManyToOne
    @JoinColumn(name = "guild_id", referencedColumnName = "guild_id", nullable = false)
    private Guild guild;

    protected User() {
    };

    public User(String name, int money, Guild guild) {
        this.username = name;
        this.money = money;
        this.guild = guild;
    }
}
