package ru.gypsyjr.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import ru.gypsyjr.main.models.Field;

@Repository
public interface FieldRepository extends JpaRepository<Field, Integer> {

}
