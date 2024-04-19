import { RouteRecordName } from "vue-router"
import Route from "@/domain/route"

type ParsedLocation = { path?: string; name?: RouteRecordName; hash?: string }

export default class RouteUtil {
  static transformToRouteLocationRaw(route: Route): ParsedLocation {
    return {
      name: route.name,
      hash: route.hash,
    }
  }
}
