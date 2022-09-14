export enum InputType {
  TEXT = "text",
  FILE = "file",
  DROPDOWN = "dropdown",
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
}

export type TextInputModelType = string
export interface TextInputAttributes extends BaseInputAttributes {
  placeholder?: string
}

export interface TextInputField extends BaseInputField {
  type: InputType.TEXT
  inputAttributes: TextInputAttributes
}

export interface DropdownAttributes extends BaseInputAttributes {
  placeholder?: string
  dropdownItems?: string[]
}

export interface DropdownInputField extends BaseInputField {
  type: InputType.DROPDOWN
  inputAttributes: DropdownAttributes
}

export type BaseInput = BaseInputField
export type InputField = TextInputField // | ...
export type InputAttributes = TextInputAttributes // | ...
export type ModelType = TextInputModelType // | ...
export type DropDownField = DropdownInputField // | ...
