import axios from "axios"

export default () => {
  const backendHost = import.meta.env.VITE_BACKEND_HOST ?? ""
  return axios.create({
    baseURL: `${backendHost}/api/v1/`,
  })
}
