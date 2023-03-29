import { InputField, InputType } from "@/shared/components/input/types"

export const participatingInstitutions: InputField[] = [
  {
    name: "participationType",
    type: InputType.TEXT,
    label: "Art der Mitwirkung",
    inputAttributes: {
      ariaLabel: "Art der Mitwirkung",
    },
  },
  {
    name: "participationInstitution",
    type: InputType.TEXT,
    label: "Mitwirkendes Organ",
    inputAttributes: {
      ariaLabel: "Mitwirkendes Organ",
    },
  },
]
