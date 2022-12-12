import { FieldSize } from "./FieldSize"

function defineTextEntry(name: string, label: string, fieldSize: FieldSize) {
  return { name, label, fieldSize }
}
export const texts = [
  defineTextEntry("decisionName", "Entscheidungsname Editor Feld", "small"),
  defineTextEntry("headline", "Titelzeile Editor Feld", "small"),
  defineTextEntry("guidingPrinciple", "Leitsatz Editor Feld", "medium"),
  defineTextEntry("headnote", "Orientierungssatz Editor Feld", "small"),
  defineTextEntry("tenor", "Tenor Editor Feld", "medium"),
  defineTextEntry("reasons", "Gründe Editor Feld", "large"),
  defineTextEntry("caseFacts", "Tatbestand Editor Feld", "large"),
  defineTextEntry(
    "decisionReasons",
    "Entscheidungsgründe Editor Feld",
    "large"
  ),
]
