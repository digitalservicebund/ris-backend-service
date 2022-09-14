export enum InputType {
  TEXT = "text",
  FILE = "file",
}

export interface BaseInputAttributes {
  ariaLabel: string
}

export interface BaseInputField {
  name: string
  type: InputType
  label: string
  iconName: string
  required?: boolean
  inputAttributes: BaseInputAttributes
  hasDropdown?: boolean
  dropdownValues?: string[]
}

export type TextInputModelType = string
export interface TextInputAttributes extends BaseInputAttributes {
  placeholder?: string
}

export interface TextInputField extends BaseInputField {
  type: InputType.TEXT
  inputAttributes: TextInputAttributes
}

export type InputField = TextInputField // | ...
export type InputAttributes = TextInputAttributes // | ...
export type ModelType = TextInputModelType // | ...
