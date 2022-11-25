import { InputField, InputType } from "./types"

export const normSubjectFields: InputField[] = [
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
    label: "GESTA-Nummer",
    inputAttributes: {
      ariaLabel: "GESTA-Nummer",
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
