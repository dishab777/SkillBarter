// Backend-shaped Skill examples; edit these entries to change the demo's skill offerings.
import type { Skill } from "../types/models";

export const skills: Skill[] = [
  { skillId: 101, skillName: "Modern watercolor", category: "Art & design", level: "BEGINNER", learningGoal: "" },
  { skillId: 102, skillName: "Spanish conversation", category: "Languages", level: "ADVANCED", learningGoal: "" },
  { skillId: 103, skillName: "React interfaces", category: "Technology", level: "EXPERT", learningGoal: "" },
  { skillId: 104, skillName: "Python analytics", category: "Technology", level: "ADVANCED", learningGoal: "" },
  { skillId: 105, skillName: "Sourdough baking", category: "Food & craft", level: "ADVANCED", learningGoal: "" },
  { skillId: 106, skillName: "Product photography", category: "Art & design", level: "INTERMEDIATE", learningGoal: "" },
  { skillId: 107, skillName: "Fingerstyle guitar", category: "Music", level: "EXPERT", learningGoal: "" },
  { skillId: 108, skillName: "UX research", category: "Art & design", level: "ADVANCED", learningGoal: "" },
  { skillId: 109, skillName: "Japanese basics", category: "Languages", level: "INTERMEDIATE", learningGoal: "" },
  { skillId: 110, skillName: "Personal finance", category: "Business", level: "ADVANCED", learningGoal: "" },
  { skillId: 111, skillName: "Motion design", category: "Art & design", level: "INTERMEDIATE", learningGoal: "" },
  { skillId: 112, skillName: "Public speaking", category: "Communication", level: "EXPERT", learningGoal: "" },
  { skillId: 113, skillName: "Urban gardening", category: "Nature & home", level: "ADVANCED", learningGoal: "" },
  { skillId: 114, skillName: "SQL storytelling", category: "Technology", level: "ADVANCED", learningGoal: "" },
  { skillId: 115, skillName: "Hand-built ceramics", category: "Food & craft", level: "BEGINNER", learningGoal: "" },
  { skillId: 116, skillName: "Brand identity", category: "Art & design", level: "EXPERT", learningGoal: "" },
  { skillId: 117, skillName: "French conversation", category: "Languages", level: "ADVANCED", learningGoal: "" },
  { skillId: 118, skillName: "Strength training", category: "Wellness", level: "EXPERT", learningGoal: "" },
  { skillId: 119, skillName: "Creative writing", category: "Writing", level: "ADVANCED", learningGoal: "" },
  { skillId: 120, skillName: "Data visualization", category: "Technology", level: "EXPERT", learningGoal: "" },
  { skillId: 121, skillName: "Public speaking", category: "Communication", level: "ADVANCED", learningGoal: "" },
  { skillId: 122, skillName: "Hand-built ceramics", category: "Food & craft", level: "INTERMEDIATE", learningGoal: "" },
];

export function findSkill(skillId: number): Skill | undefined {
  return skills.find((skill) => skill.skillId === skillId);
}
