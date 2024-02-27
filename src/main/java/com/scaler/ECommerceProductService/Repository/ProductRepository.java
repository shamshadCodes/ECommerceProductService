package com.scaler.ECommerceProductService.Repository;

import com.scaler.ECommerceProductService.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    @Override
    @NonNull
    List<Product> findAll();

    @NonNull
    @Override
    Optional<Product> findById(@NonNull String id);

    Optional<Product> findByTitleIgnoreCase(String title);

    @NonNull
    @Override
    <S extends Product> S save(@NonNull S product);
}
