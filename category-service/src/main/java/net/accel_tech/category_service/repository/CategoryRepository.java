package net.accel_tech.category_service.repository;

import net.accel_tech.category_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // JpaRepository fournit déjà toutes les méthodes CRUD (findAll, save, delete, etc.)
}