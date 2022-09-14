import { FieldSize } from "./FieldSize"

function defineTextEntry(name: string, label: string, fieldSize: FieldSize) {
  return { name, label, fieldSize }
}
export const texts = [
  defineTextEntry("decisionName", "Entscheidungsname", "small"),
  defineTextEntry("headline", "Titelzeile", "small"),
  defineTextEntry("guidingPrinciple", "Leitsatz", "medium"),
  defineTextEntry("headnote", "Orientierungssatz", "small"),
  defineTextEntry("tenor", "Tenor", "medium"),
  defineTextEntry("reasons", "Gründe", "large"),
  defineTextEntry("caseFacts", "Tatbestand", "large"),
  defineTextEntry("decisionReasons", "Entscheidungsgründe", "large"),
]
