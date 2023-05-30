package ru.gypsyjr.search;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.gypsyjr.lemmatizer.Lemmatizer;
import ru.gypsyjr.main.models.*;
import ru.gypsyjr.main.repository.FieldRepository;
import ru.gypsyjr.main.repository.PageRepository;
import ru.gypsyjr.main.repository.SearchIndexRepository;
import ru.gypsyjr.main.repository.SiteRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SearchEngine {
    private final Lemmatizer lemmatizer;
    private SearchIndexRepository indexRepository;
    private FieldRepository fieldRepository;
    private PageRepository pageRepository;

    public SearchEngine() {
        lemmatizer = new Lemmatizer();
    }

    public Search search(String query, Site site,
                         PageRepository pageRepository, SearchIndexRepository indexRepository,
                         FieldRepository fieldRepository, SiteRepository siteRepository) {
        this.indexRepository = indexRepository;
        this.fieldRepository = fieldRepository;
        this.pageRepository = pageRepository;

        SortedSet<SearchResult> searchResults = new TreeSet<>();

        if (site == null) {
            siteRepository.findAll().forEach(s -> {
                searchResults.addAll(getSearchesBySite(s, query));
            });
        } else {
            searchResults.addAll(getSearchesBySite(site, query));
        }

        Search search = new Search();

        search.setCount(searchResults.size());
        search.setResult(true);
        search.setData(searchResults);

        return search;
    }

    private Set<SearchResult> getSearchesBySite(Site site, String query) {
        System.out.println(site.getUrl());
        List<Page> pages = pageRepository.findAllBySite(site);
        return addSearchQuery(query, site, pages);
    }

    private Set<SearchResult> addSearchQuery(String query, Site site, List<Page> pages) {
        SortedSet<Lemma> lemmas = new TreeSet<>();

        for (String word : query.split(" ")) {
            Lemma lemma = lemmatizer.getLemma(word.toLowerCase(), site);
            if (lemma != null) {
                lemmas.add(lemma);
            }
        }

        List<IndexRanks> indexRanks = getIndexRanks(lemmas, pages);

        return getSearchResults(indexRanks, lemmas, site);
    }

    private List<IndexRanks> getIndexRanks(SortedSet<Lemma> lemmas, List<Page> pages) {
        List<IndexRanks> indexRanks = new ArrayList<>();

        lemmas.forEach(lemma -> {
            int count = 0;
            while (pages.size() > count) {
                IndexTable indexTable = indexRepository.findByLemmaAndPage(lemma, pages.get(count));
                if (indexTable == null) {
                    pages.remove(count);
                } else {
                    IndexRanks indexRank = new IndexRanks();

                    indexRank.setPage(pages.get(count));
                    indexRank.setRanks(lemma.getLemma(), indexTable.getLemmaRank());
                    indexRank.setRAbs();

                    indexRanks.add(indexRank);
                    count++;
                }
            }
        });

        indexRanks.forEach(IndexRanks::setRRel);
        return indexRanks;
    }

    private SortedSet<SearchResult> getSearchResults(List<IndexRanks> indexRanks, SortedSet<Lemma> lemmas, Site site) {
        SortedSet<SearchResult> searchResults = new TreeSet<>();
        List<Field> fieldList = fieldRepository.findAll();

        indexRanks.forEach(it -> {
            Document document = Jsoup.parse(it.getPage().getContent());

            AtomicReference<String> snippet = new AtomicReference<>("");
            AtomicInteger maxSnippet = new AtomicInteger();
            SearchResult sResult = new SearchResult();
            AtomicBoolean isHaven = new AtomicBoolean(false);

            fieldList.forEach(field -> {

                document.select(field.getSelector()).forEach(i -> {
                    String str = i.text().toLowerCase();
                    int count = 0;
                    for (Lemma lem : lemmas.stream().toList()) {
                        String l = lem.getLemma();
                        if (str.contains(l)) {
                            count++;
                            str = str.replaceAll("(?i)" + l,
                                    "<b>" + l + "</b>");
                        } else {
                            lemmas.remove(lem);
                        }
                    }

                    if (count > maxSnippet.get()) {
                        snippet.set(str);
                        maxSnippet.set(count);
                        isHaven.set(true);
                    }
                });
            });

            if (isHaven.get()) {
                sResult.setTitle(document.title());
                sResult.setRelevance(it.getRRel());
                sResult.setSnippet(snippet.get());
                sResult.setUri(it.getPage().getPath().replace(site.getUrl(), ""));
                sResult.setSite(site.getUrl());
                sResult.setSiteName(site.getName());

                searchResults.add(sResult);
            }
        });

        return searchResults;
    }
}
