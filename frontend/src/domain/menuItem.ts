import Route from "@/domain/route"

export default interface MenuItem {
  label: string
  route: Route
  isDisabled?: boolean
  children?: MenuItem[]
}
