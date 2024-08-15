import TextAreaInput from "@/components/input/TextAreaInput.vue"
import TextInput from "@/components/input/TextInput.vue"
import { TextAreaInputAttributes } from "@/components/input/types"
import useSessionStore from "@/stores/sessionStore"

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
const session = useSessionStore()
const isReadOnly = await session.isExternal()
export const texts = [
  defineTextEntry("decisionName", "Entscheidungsname", TextInput),
  defineTextEntry("headline", "Titelzeile", TextAreaInput, false, "small"),
  defineTextEntry("guidingPrinciple", "Leitsatz", TextAreaInput),
  defineTextEntry("headnote", "Orientierungssatz", TextAreaInput),
  defineTextEntry("tenor", "Tenor", TextAreaInput, isReadOnly),
  defineTextEntry("reasons", "Gründe", TextAreaInput, isReadOnly),
  defineTextEntry("caseFacts", "Tatbestand", TextAreaInput, isReadOnly),
  defineTextEntry(
    "decisionReasons",
    "Entscheidungsgründe",
    TextAreaInput,
    true,
  ),
]
