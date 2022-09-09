import { FieldSize } from "./FieldSize"

function defineTextEntry(name: string, label: string, fieldSize: FieldSize) {
  return { name, label, fieldSize }
}
export const texts = [
  defineTextEntry("entscheidungsname", "Entscheidungsname", "small"),
  defineTextEntry("titelzeile", "Titelzeile", "small"),
  defineTextEntry("leitsatz", "Leitsatz", "medium"),
  defineTextEntry("orientierungssatz", "Orientierungssatz", "small"),
  defineTextEntry("tenor", "Tenor", "medium"),
  defineTextEntry("gruende", "Gründe", "large"),
  defineTextEntry("tatbestand", "Tatbestand", "large"),
  defineTextEntry("entscheidungsgruende", "Entscheidungsgründe", "large"),
]
