interface Route {
  name?: string
  hash?: string
}

export default interface MenuItem {
  label: string
  route: Route
  isDisabled?: boolean
  children?: MenuItem[]
}
