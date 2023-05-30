package ru.gypsyjr.lemmatizer;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.gypsyjr.main.models.Lemma;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import ru.gypsyjr.main.models.Site;
import ru.gypsyjr.main.repository.LemmaRepository;

import java.io.IOException;
import java.util.*;


@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Lemmatizer {
    private final List<String> WRONG_TYPES = new ArrayList<>();
    private final LuceneMorphology russianMorph;
    private final LuceneMorphology englishMorph;
    private final LinkedHashMap<String, Lemma> wordsCount;
    private final LinkedHashMap<Lemma, Float> wordsRanks;
    private static LemmaRepository lemmaRepository;

    public Lemmatizer() {
        try {
            russianMorph = new RussianLuceneMorphology();
            englishMorph = new EnglishLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        wordsCount = new LinkedHashMap<>();
        wordsRanks = new LinkedHashMap<>();
        WRONG_TYPES.add("ПРЕДЛ");
        WRONG_TYPES.add("СОЮЗ");
        WRONG_TYPES.add("МЕЖД");
        WRONG_TYPES.add("ВВОДН");
        WRONG_TYPES.add("ЧАСТ");
        WRONG_TYPES.add("МС");
        WRONG_TYPES.add("CONJ");
        WRONG_TYPES.add("PART");
    }

    private boolean checkRussianForm(String word) {
        String russianAlphabet = "[а-яА-Я]+";

        if (!word.matches(russianAlphabet)) {
            return false;
        }

        List<String> wordBaseForm = russianMorph.getMorphInfo(word);

        for (String type : WRONG_TYPES) {
            if (wordBaseForm.toString().contains(type)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkEnglishForm(String word) {

        List<String> wordBaseForm = englishMorph.getMorphInfo(word);

        for (String type : WRONG_TYPES) {
            if (wordBaseForm.toString().contains(type)) {
                return false;
            }
        }

        return true;
    }

    private String checkLanguage(String word) {
        String russianAlphabet = "[а-яА-Я]+";
        String englishAlphabet = "[a-zA-z]+";

        if (word.matches(russianAlphabet)) {
            return "Russian";
        } else if (word.matches(englishAlphabet)) {
            return "English";
        } else {
            return "";
        }
    }

    private void addNewWord(String word, boolean isNew, float rank, Site site) {
        if (checkLanguage(word).equals("Russian")) {
            if (!checkRussianForm(word)) {
                return;
            }

            addNormalForms(word, isNew, russianMorph, rank, site);
        } else if (checkLanguage(word).equals("English")) {
            if (!checkEnglishForm(word)) {
                return;
            }

            addNormalForms(word, isNew, englishMorph, rank, site);
        }
    }

    private void addNormalForms(String word, boolean isNew, LuceneMorphology wordMorph, float rank, Site site) {
        List<String> normalWords = wordMorph.getNormalForms(word);

        normalWords.forEach(it -> {
            Lemma lemma;

            if (wordsCount.containsKey(it) && isNew) {
                lemma = wordsCount.get(it);
                lemma.setFrequency(lemma.getFrequency() + 1);
                lemma.setSite(site);
                wordsCount.replace(it, lemma);
                wordsRanks.clear();

                lemmaRepository.save(lemma);
            } else if (isNew) {
                wordsRanks.clear();
                lemma = new Lemma();
                lemma.setFrequency(1);
                lemma.setLemma(it);
                lemma.setSite(site);
                wordsCount.put(it, lemma);
                wordsRanks.put(lemma, rank);

                lemmaRepository.save(lemma);
            } else if (wordsCount.containsKey(it)) {
                lemma = wordsCount.get(it);
                if (wordsRanks.containsKey(lemma)) {
                    wordsRanks.replace(lemma, wordsRanks.get(lemma) + rank);
                } else {
                    wordsRanks.put(lemma, rank);
                }
            }
        });
    }

    public void addString(String sentence, boolean isNew, float rank, Site site) {
        if (wordsCount.isEmpty()) {
            List<Lemma> lemmas = lemmaRepository.findAllBySite(site);
            if (!lemmas.isEmpty()) {
                lemmas.forEach(lemma -> wordsCount.put(lemma.getLemma(), lemma));
            }
        }

        String regex = "[.,!?\\-:;()'\"]?";
        sentence = sentence.replaceAll(regex, "");
        String[] words = sentence.toLowerCase().split(" ");
        Arrays.stream(words).distinct().forEach(it -> {
            addNewWord(it, isNew, rank, site);
        });
    }

    public LinkedHashMap<Lemma, Float> getLemmasWithRanks() {
        return wordsRanks;
    }

    public Lemma getLemma(String word, Site site) {

        if ((checkLanguage(word).equals("Russian") && checkRussianForm(word)) ||
                (checkLanguage(word).equals("English") && checkEnglishForm(word))) {
            return lemmaRepository.findLemmaByLemmaAndSite(word, site);
        }

        return null;
    }

    public static void setLemmaRepository(LemmaRepository lemmaRepository) {
        Lemmatizer.lemmaRepository = lemmaRepository;
    }
}
