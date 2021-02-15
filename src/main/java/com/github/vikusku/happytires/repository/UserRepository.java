package com.github.vikusku.happytires.repository;

import com.github.vikusku.happytires.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Customer, Long> {
}
