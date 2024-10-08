package cz.ememsoft.minibank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "customer", schema = "bank")
public class Customer {

    @OneToMany(mappedBy = "customerId", orphanRemoval = true)
    private Set<Account> accounts = new LinkedHashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_seq")
    @SequenceGenerator(name = "customer_seq")
    @Column(name = "customer_id", nullable = false)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String first_name;

    @Column(name = "middle_name", nullable = true)
    private String middle_name;

    @Column(name = "last_name", nullable = false)
    private String last_name;


}