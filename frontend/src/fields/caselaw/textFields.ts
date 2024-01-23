import TextAreaInput from "@/shared/components/input/TextAreaInput.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import { TextAreaInputAttributes } from "@/shared/components/input/types"

function defineTextEntry(
  name: string,
  label: string,
  fieldType: typeof TextInput | typeof TextAreaInput,
  fieldSize: TextAreaInputAttributes["fieldSize"] = "big",
) {
  return {
    name,
    label,
    fieldType,
    ...(fieldType == TextAreaInput && { fieldSize }),
  }
}
export const texts = [
  defineTextEntry("decisionName", "Entscheidungsname", TextInput),
  defineTextEntry("headline", "Titelzeile", TextAreaInput, "small"),
  defineTextEntry("guidingPrinciple", "Leitsatz", TextAreaInput),
  defineTextEntry("headnote", "Orientierungssatz", TextAreaInput),
  defineTextEntry("tenor", "Tenor", TextAreaInput),
  defineTextEntry("reasons", "Gründe", TextAreaInput),
  defineTextEntry("caseFacts", "Tatbestand", TextAreaInput),
  defineTextEntry("decisionReasons", "Entscheidungsgründe", TextAreaInput),
]
