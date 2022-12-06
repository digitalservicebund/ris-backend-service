import { InputField, InputType } from "@/domain"

export const subjectArea: InputField[] = [
  {
    name: "subjectFna",
    type: InputType.TEXT,
    label: "FNA-Nummer",
    inputAttributes: {
      ariaLabel: "FNA-Nummer",
    },
  },
  {
    name: "subjectPreviousFna",
    type: InputType.TEXT,
    label: "Frühere FNA-Nummer",
    inputAttributes: {
      ariaLabel: "Frühere FNA-Nummer",
    },
  },
  {
    name: "subjectGesta",
    type: InputType.TEXT,
    label: "GESTA number",
    inputAttributes: {
      ariaLabel: "GESTA number",
    },
  },
  {
    name: "subjectBgb3",
    type: InputType.TEXT,
    label: "Bundesgesetzblatt Teil III",
    inputAttributes: {
      ariaLabel: "Bundesgesetzblatt Teil III",
    },
  },
]
