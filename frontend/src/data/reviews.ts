// Backend-shaped Rating examples; scores must stay between 1 and 5, matching the Java model.
import type { Rating, Student } from "../types/models";
import { users } from "./users";

function studentReference(userId: number): Student {
  const student = users.find((user) => user.userId === userId);
  if (!student) throw new Error(`Missing sample student ${userId} for a rating.`);
  return { ...student, ratings: [] };
}

export const reviews: Rating[] = [
  { ratingId: 1, fromStudent: studentReference(3), toStudent: studentReference(1), score: 5, feedback: "Maya made the research process feel practical from day one." },
  { ratingId: 2, fromStudent: studentReference(4), toStudent: studentReference(2), score: 5, feedback: "Patient, generous, and very fun to learn with." },
  { ratingId: 3, fromStudent: studentReference(6), toStudent: studentReference(3), score: 4, feedback: "I left with a working prototype and a much clearer path." },
  { ratingId: 4, fromStudent: studentReference(8), toStudent: studentReference(7), score: 5, feedback: "A thoughtful teacher who helped the tricky parts click." },
];
