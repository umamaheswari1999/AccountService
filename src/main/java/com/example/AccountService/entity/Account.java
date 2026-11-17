package com.example.AccountService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(name = "account_id")
    private String accountId;

    @Column(nullable = false, precision = 19, scale = 2, columnDefinition = "DECIMAL(19,2) DEFAULT 0.00")
    private BigDecimal balance = BigDecimal.ZERO ;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> transactions = new ArrayList<>();


    public Account(String accountId) {
        this.accountId = accountId;
    }

    public void applyTransaction(String eventId, BigDecimal amount, String type, Instant eventTimestamp) {
        if ("CREDIT".equalsIgnoreCase(type)) {
            this.balance = this.balance.add(amount);
        } else if ("DEBIT".equalsIgnoreCase(type)) {
            this.balance = this.balance.subtract(amount);
        }
        
        Event tx = new Event(eventId, amount, type, eventTimestamp, this);
        this.transactions.add(tx);
    }


}