package com.skillsexchange.interfaces;

import com.skillsexchange.model.Skill;

public interface SkillProvider {
    void addOfferedSkill(Skill skill);
    void addWantedSkill(Skill skill);
}