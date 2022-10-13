export enum InputType {
  TEXT = "text",
  FILE = "file",
  DROPDOWN = "dropdown",
  DATE = "date",
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

export type DateInputModelType = string

export interface DateInputAttributes extends BaseInputAttributes {
  hasError?: boolean
  isInPast?: boolean
}

export interface DateInputField extends BaseInputField {
  type: InputType.DATE
  inputAttributes: DateInputAttributes
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

export type InputField = TextInputField | DropdownInputField | DateInputField
export type InputAttributes =
  | TextInputAttributes
  | DropdownAttributes
  | DateInputAttributes
export type ModelType = TextInputModelType | DateInputModelType
