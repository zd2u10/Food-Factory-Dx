package com.foodfactory.dx.repository;

import com.foodfactory.dx.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {
}
