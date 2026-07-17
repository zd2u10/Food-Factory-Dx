package com.foodfactory.dx.repository;

import com.foodfactory.dx.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
