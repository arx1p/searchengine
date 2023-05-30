package ru.gypsyjr.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gypsyjr.main.models.Site;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {
    Site findSiteByUrl(String url);
}
