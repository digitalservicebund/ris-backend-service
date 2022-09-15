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
export type DropdownItem = {
  text: string
  value: string
}
export interface DropdownAttributes extends BaseInputAttributes {
  placeholder?: string
  dropdownItems?: DropdownItem[]
}

export interface DropdownInputField extends BaseInputField {
  type: InputType.DROPDOWN
  inputAttributes: DropdownAttributes
}

export type InputField = TextInputField | DropdownInputField
export type InputAttributes = TextInputAttributes | DropdownAttributes
export type ModelType = TextInputModelType // | ...
