package interfaces;

import java.util.List;
import java.util.Map;
import model.Skill;

/**
 * Interface defining matching behavior for peer skill exchange.
 * Demonstrates the Interface OOP abstraction principle.
 */
public interface Matchable {

    /**
     * Determines whether two skills form a valid reciprocal exchange candidate.
     *
     * @param teachSkill Skill being offered
     * @param learnSkill Skill desired to learn
     * @return true if skills are compatible
     */
    boolean isCompatible(Skill teachSkill, Skill learnSkill);

    /**
     * Finds potential reciprocal skill-exchange match suggestions for a student.
     *
     * @param studentID Student ID looking for matches
     * @return List of matching suggestions with partner details and reciprocal skills
     */
    List<Map<String, Object>> findMatchSuggestions(int studentID);
}
