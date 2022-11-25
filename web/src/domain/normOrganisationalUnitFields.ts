import { InputField, InputType } from "./types"

export const normOrganisationalUnitFields: InputField[] = [
  {
    name: "leadJurisdiction",
    type: InputType.TEXT,
    label: "Ressort",
    inputAttributes: {
      ariaLabel: "Ressort",
    },
  },
  {
    name: "leadUnit",
    type: InputType.TEXT,
    label: "Organisationseinheit",
    inputAttributes: {
      ariaLabel: "Organisationseinheit",
    },
  },
]
