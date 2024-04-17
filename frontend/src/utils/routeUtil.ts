import { RouteLocationRaw } from "vue-router"
import Route from "@/domain/route"

export default class RouteUtil {
  static transformToRouteLocationRaw(route: Route): RouteLocationRaw {
    const paramsObj: Record<string, string | string[]> = {}
    const queryObj: Record<string, string | string[]> = {}

    return {
      params: paramsObj,
      query: queryObj,
      name: route.name,
      hash: route.hash,
    }
  }
}
