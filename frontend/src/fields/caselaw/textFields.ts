import TextAreaInput from "@/components/input/TextAreaInput.vue"
import TextInput from "@/components/input/TextInput.vue"
import { TextAreaInputAttributes } from "@/components/input/types"

function defineTextEntry(
  name: string,
  label: string,
  fieldType: typeof TextInput | typeof TextAreaInput,
  readOnly: boolean = false,
  fieldSize: TextAreaInputAttributes["fieldSize"] = "big",
) {
  return {
    name,
    label,
    fieldType,
    readOnly,
    ...(fieldType == TextAreaInput && { fieldSize }),
  }
}
export const texts = [
  defineTextEntry("decisionName", "Entscheidungsname", TextInput),
  defineTextEntry("headline", "Titelzeile", TextAreaInput, false, "small"),
  defineTextEntry("guidingPrinciple", "Leitsatz", TextAreaInput),
  defineTextEntry("headnote", "Orientierungssatz", TextAreaInput),
  defineTextEntry("tenor", "Tenor", TextAreaInput, true),
  defineTextEntry("reasons", "Gründe", TextAreaInput, true),
  defineTextEntry("caseFacts", "Tatbestand", TextAreaInput, true),
  defineTextEntry(
    "decisionReasons",
    "Entscheidungsgründe",
    TextAreaInput,
    true,
  ),
]
