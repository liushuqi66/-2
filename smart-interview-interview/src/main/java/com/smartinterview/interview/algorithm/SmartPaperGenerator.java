package com.smartinterview.interview.algorithm;

import com.smartinterview.interview.entity.Question;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能组卷算法 - 难度分层 + 知识点去重 + 贪心最大化覆盖
 */
@Slf4j
@Component
public class SmartPaperGenerator {

    public List<Question> generate(List<Question> pool, int count, int level) {
        Map<Integer, Double> ratio = getRatio(level);
        Map<Integer, List<Question>> byDiff = pool.stream().collect(Collectors.groupingBy(Question::getDifficulty));
        Set<String> covered = new HashSet<>();
        List<Question> paper = new ArrayList<>();

        for (Map.Entry<Integer, Double> e : ratio.entrySet()) {
            int target = (int) Math.round(count * e.getValue());
            List<Question> candidates = byDiff.getOrDefault(e.getKey(), Collections.emptyList());
            candidates.sort((a, b) -> Integer.compare(countNew(b, covered), countNew(a, covered)));

            for (Question q : candidates) {
                if (paper.size() >= count) break;
                if (paper.contains(q)) continue;
                if (paper.stream().filter(p -> p.getDifficulty().equals(e.getKey())).count() >= target) break;
                paper.add(q);
                addKp(q, covered);
            }
        }

        while (paper.size() < count) {
            Question q = pool.get(new Random().nextInt(pool.size()));
            if (!paper.contains(q)) { paper.add(q); addKp(q, covered); }
        }

        Collections.shuffle(paper);
        log.info("Generated paper: {}Q, {}KPs, Lv{}", paper.size(), covered.size(), level);
        return paper;
    }

    private int countNew(Question q, Set<String> covered) {
        if (q.getKnowledgePoints() == null) return 0;
        return (int) Arrays.stream(q.getKnowledgePoints().split(",")).map(String::trim).filter(k -> !covered.contains(k)).count();
    }
    private void addKp(Question q, Set<String> covered) {
        if (q.getKnowledgePoints() != null) Arrays.stream(q.getKnowledgePoints().split(",")).map(String::trim).forEach(covered::add);
    }
    private Map<Integer, Double> getRatio(int level) {
        return switch (level) {
            case 1 -> Map.of(1, 0.5, 2, 0.4, 3, 0.1);
            case 2 -> Map.of(1, 0.3, 2, 0.5, 3, 0.2);
            case 3 -> Map.of(1, 0.1, 2, 0.4, 3, 0.5);
            default -> Map.of(1, 0.4, 2, 0.4, 3, 0.2);
        };
    }
}
