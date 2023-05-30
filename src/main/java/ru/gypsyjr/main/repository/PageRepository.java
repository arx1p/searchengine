package ru.gypsyjr.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import ru.gypsyjr.main.models.Page;
import ru.gypsyjr.main.models.Site;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    List<Page> findAllBySite(Site site);

    Integer countBySite(Site site);

    Page findByPath(String path);
}
