import { LocationQuery } from "vue-router"

export default interface Route {
  params?: Record<string, string | undefined>
  query?: LocationQuery
  name?: string
  hash?: string
}
