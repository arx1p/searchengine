package ru.gypsyjr.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gypsyjr.lemmatizer.Lemmatizer;
import ru.gypsyjr.main.models.*;
import ru.gypsyjr.main.repository.*;
import ru.gypsyjr.parse.WebMapParse;
import ru.gypsyjr.search.SearchEngine;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class Storage {
    private static final int NUMBER_OF_THREADS = 3;

    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private SearchIndexRepository indexRepository;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private Config config;
    private List<Thread> threads = new ArrayList<>();
    private List<ForkJoinPool> forkJoinPools = new ArrayList<>();

    private void clearData() {
        indexRepository.deleteAll();
        lemmaRepository.deleteAll();
        pageRepository.deleteAll();
        siteRepository.deleteAll();
    }

    private WebMapParse newParse(Site site) {
        return new WebMapParse(site.getUrl(), site, config, fieldRepository, siteRepository, indexRepository, pageRepository);
    }

    public ApiStatistics getStatistic() {
        Statistic statistic = new Statistic();

        AtomicInteger allLemmas = new AtomicInteger();
        AtomicInteger allPages = new AtomicInteger();
        AtomicInteger allSites = new AtomicInteger();

        List<Site> siteList = siteRepository.findAll();

        if (siteList.size() == 0) {
            return new ApiStatistics();
        }

        siteList.forEach(it -> {
            int pages = pageRepository.countBySite(it);
            int lemmas = lemmaRepository.countBySite(it);

            it.setPages(pages);
            it.setLemmas(lemmas);
            statistic.addDetailed(it);

            allPages.updateAndGet(v -> v + pages);
            allLemmas.updateAndGet(v -> v + lemmas);
            allSites.getAndIncrement();
        });

        Total total = new Total();
        total.setIndexing(true);
        total.setLemmas(allLemmas.get());
        total.setPages(allPages.get());
        total.setSites(allSites.get());

        statistic.setTotal(total);

        ApiStatistics statistics = new ApiStatistics();

        statistics.setResult(true);
        statistics.setStatistics(statistic);

        return statistics;
    }

    public void indexing() {
        threads = new ArrayList<>();
        forkJoinPools = new ArrayList<>();

        clearData();
        Lemmatizer.setLemmaRepository(lemmaRepository);

        List<WebMapParse> parses = new ArrayList<>();
        List<String> urls = config.getSitesUrl();
        List<String> namesUrls = config.getSitesName();


        for (int i = 0; i < urls.size(); ++i) {
            String mainPage = urls.get(i);

            Site site = siteRepository.findSiteByUrl(mainPage);

            if (site == null) {
                site = new Site();
            }

            site.setUrl(mainPage);
            site.setStatusTime(new Date());
            site.setStatus(Status.INDEXING);
            site.setName(namesUrls.get(i));

            parses.add(newParse(site));
            siteRepository.save(site);
        }

        urls.clear();
        namesUrls.clear();


        parses.forEach(parse -> threads.add(new Thread(() -> {
            Site site = parse.getSite();

            try {
                site.setStatus(Status.INDEXING);
                siteRepository.save(site);

                ForkJoinPool forkJoinPool = new ForkJoinPool(NUMBER_OF_THREADS);

                forkJoinPools.add(forkJoinPool);

                forkJoinPool.execute(parse);
                int count = parse.join();

                site.setStatus(Status.INDEXED);
                siteRepository.save(site);

                System.out.println("Сайт " + site.getName() + " проиндексирован,кол-во ссылок - " + count);
            } catch (CancellationException ex) {
                ex.printStackTrace();
                site.setLastError("Ошибка индексации: " + ex.getMessage());
                site.setStatus(Status.FAILED);
                siteRepository.save(site);
            }
        })));

        threads.forEach(Thread::start);
        forkJoinPools.forEach(ForkJoinPool::shutdown);

        forkJoinPools.forEach(ForkJoinPool::shutdown);
    }

    public boolean startIndexing() {
        AtomicBoolean isIndexing = new AtomicBoolean(false);

        siteRepository.findAll().forEach(site -> {
            if (site.getStatus().equals(Status.INDEXING)) {
                isIndexing.set(true);
            }
        });

        if (isIndexing.get()) {
            return true;
        }
        new Thread(this::indexing).start();

        return false;
    }

    public boolean stopIndexing() {
        System.out.println("Потоков работает: " + threads.size());

        AtomicBoolean isIndexing = new AtomicBoolean(false);

        siteRepository.findAll().forEach(site -> {
            if (site.getStatus().equals(Status.INDEXING)) {
                isIndexing.set(true);
            }
        });

        if (!isIndexing.get()) {
            return true;
        }

        forkJoinPools.forEach(ForkJoinPool::shutdownNow);
        threads.forEach(Thread::interrupt);

        siteRepository.findAll().forEach(site -> {
            site.setLastError("Остановка индексации");
            site.setStatus(Status.FAILED);
            siteRepository.save(site);
        });

        threads.clear();
        forkJoinPools.clear();

        return false;
    }

    public boolean indexPage(String url) {
        Lemmatizer.setLemmaRepository(lemmaRepository);
        List<Site> siteList = siteRepository.findAll();

        if (siteList.size() == 0) {
            List<String> urls = config.getSitesUrl();
            List<String> namesUrls = config.getSitesName();

            for (int i = 0; i < urls.size(); ++i) {
                if (url.contains(urls.get(i))) {
                    String mainPage = urls.get(i);
                    Site site = new Site();

                    site.setUrl(mainPage);
                    site.setStatusTime(new Date());
                    site.setStatus(Status.INDEXING);
                    site.setName(namesUrls.get(i));
                    siteRepository.save(site);

                    WebMapParse parse = new WebMapParse(mainPage, site, config, fieldRepository, siteRepository,
                            indexRepository, pageRepository);
                    try {
                        parse.addPage();
                    } catch (IOException e) {
                        return false;
                    }

                    site.setStatus(Status.INDEXED);
                    siteRepository.save(site);

                    return true;
                }
            }
        } else {
            for (Site site : siteList) {
                if (url.contains(site.getUrl())) {
                    site.setStatus(Status.INDEXING);
                    siteRepository.save(site);

                    WebMapParse parse = new WebMapParse(site.getUrl(), site, config, fieldRepository, siteRepository,
                            indexRepository, pageRepository);
                    try {
                        parse.addPage();
                    } catch (IOException e) {
                        return false;
                    }

                    site.setStatus(Status.INDEXED);
                    siteRepository.save(site);

                    return true;
                }
            }
        }

        return false;
    }

    public Search search(String query, String site, int offset, int limit) {
        Lemmatizer.setLemmaRepository(lemmaRepository);

        SearchEngine searchEngine = new SearchEngine();

        Search search = searchEngine.search(query, siteRepository.findSiteByUrl(site), pageRepository,
                indexRepository, fieldRepository, siteRepository);

        if (search.getCount() < offset) {
            return new Search();
        }

        if (search.getCount() > limit) {
            Set<SearchResult> searchResults = new TreeSet<>();

            search.getData().forEach(it -> {
                if (searchResults.size() <= limit) {
                    searchResults.add(it);
                }
            });

            search.setData(searchResults);
        }

        return search;
    }
}
