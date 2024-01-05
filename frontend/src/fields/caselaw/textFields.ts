import TextAreaInput from "@/shared/components/input/TextAreaInput.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

function defineTextEntry(
  name: string,
  label: string,
  fieldType: typeof TextInput | typeof TextAreaInput,
) {
  return { name, label, fieldType }
}
export const texts = [
  defineTextEntry("decisionName", "Entscheidungsname", TextInput),
  defineTextEntry("headline", "Titelzeile", TextAreaInput),
  defineTextEntry("guidingPrinciple", "Leitsatz", TextAreaInput),
  defineTextEntry("headnote", "Orientierungssatz", TextAreaInput),
  defineTextEntry("tenor", "Tenor", TextAreaInput),
  defineTextEntry("reasons", "Gründe", TextAreaInput),
  defineTextEntry("caseFacts", "Tatbestand", TextAreaInput),
  defineTextEntry("decisionReasons", "Entscheidungsgründe", TextAreaInput),
]
