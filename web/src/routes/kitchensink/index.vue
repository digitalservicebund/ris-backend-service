<script lang="ts" setup>
import { ref } from "vue"
import KitchensinkHeader from "@/kitchensink/components/KitchensinkHeader.vue"
import type { NormComponent } from "@/kitchensink/types/types"

const components = ref<NormComponent[]>([
  { name: "Text Button", view: "IndexView" },
  { name: "Text Editor", view: "IndexView" },
  { name: "Text Input", view: "IndexView" },
  { name: "Save Document Unit Button", view: "IndexView" },
  { name: "Publication Document", view: "IndexView" },
  { name: "Popup Modal", view: "IndexView" },
  { name: "Original File Side Panel", view: "IndexView" },
  { name: "Navbar Top", view: "IndexView" },
  { name: "Navbar Side", view: "IndexView" },
  { name: "Input Group", view: "IndexView" },
  { name: "Input Field", view: "IndexView" },
  { name: "Input Element", view: "IndexView" },
  { name: "File Viewer", view: "IndexView" },
  { name: "File Upload", view: "IndexView" },
  { name: "File Input Button", view: "IndexView" },
  { name: "Expandable Content", view: "IndexView" },
  { name: "Error Modal", view: "IndexView" },
  { name: "Dropdown Input", view: "IndexView" },
  { name: "Code Snippet", view: "IndexView" },
])
</script>

<template>
  <div
    id="kitchensink"
    class="absolute bg-white box-border h-full left-0 overflow-y-scroll top-0 w-full"
  >
    <KitchensinkHeader :components="components" />
    <!-- Container -->
    <div
      class="border-b-4 flex flex-nowrap flex-row items-center justify-center overflow-y-hidden relative w-full"
    >
      <div
        class="flex flex-col flex-nowrap gap-y-10 justify-start py-10 text-justify w-2/3"
      >
        <!-- Title -->
        <h1 class="font-bold text-32">Component</h1>
        <!-- Content -->
        <div class="content-container flex flex-col">
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Voluptatem
          iusto autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Sequi, magni culpa dolorem saepe explicabo numquam officia incidunt
          quis corporis assumenda. Sit maxime consequatur dignissimos aperiam
          consectetur, dolorum natus fuga numquam. Lorem ipsum dolor sit amet,
          consectetur adipisicing elit. Porro officiis, reiciendis quisquam
          voluptatibus sint aperiam atque. Pariatur dolorem error, porro modi
          maiores dolore dolores animi distinctio sint nesciunt vel eius. Lorem
          ipsum dolor sit, amet consectetur adipisicing elit. Cupiditate odio
          placeat sapiente tempora in a voluptas laudantium possimus nisi unde
          veniam labore, nemo quo. Nisi repudiandae id non eum debitis. Lorem
          ipsum dolor sit amet consectetur adipisicing elit. Voluptatem iusto
          autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Sequi, magni culpa dolorem saepe explicabo numquam officia incidunt
          quis corporis assumenda. Sit maxime consequatur dignissimos aperiam
          consectetur, dolorum natus fuga numquam. Lorem ipsum dolor sit amet,
          consectetur adipisicing elit. Porro officiis, reiciendis quisquam
          voluptatibus sint aperiam atque. Pariatur dolorem error, porro modi
          maiores dolore dolores animi distinctio sint nesciunt vel eius. Lorem
          ipsum dolor sit, amet consectetur adipisicing elit. Cupiditate odio
          placeat sapiente tempora in a voluptas laudantium possimus nisi unde
          veniam labore, nemo quo. Nisi repudiandae id non eum debitis. Lorem
          ipsum dolor sit amet consectetur adipisicing elit. Voluptatem iusto
          autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Sequi, magni culpa dolorem saepe explicabo numquam officia incidunt
          quis corporis assumenda. Sit maxime consequatur dignissimos aperiam
          consectetur, dolorum natus fuga numquam. Lorem ipsum dolor sit amet,
          consectetur adipisicing elit. Porro officiis, reiciendis quisquam
          voluptatibus sint aperiam atque. Pariatur dolorem error, porro modi
          maiores dolore dolores animi distinctio sint nesciunt vel eius. Lorem
          ipsum dolor sit, amet consectetur adipisicing elit. Cupiditate odio
          placeat sapiente tempora in a voluptas laudantium possimus nisi unde
          veniam labore, nemo quo. Nisi repudiandae id non eum debitis. Lorem
          ipsum dolor sit amet consectetur adipisicing elit. Voluptatem iusto
          autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Sequi, magni culpa dolorem saepe explicabo numquam officia incidunt
          quis corporis assumenda. Sit maxime consequatur dignissimos aperiam
          consectetur, dolorum natus fuga numquam. Lorem ipsum dolor sit amet,
          consectetur adipisicing elit. Porro officiis, reiciendis quisquam
          voluptatibus sint aperiam atque. Pariatur dolorem error, porro modi
          maiores dolore dolores animi distinctio sint nesciunt vel eius. Lorem
          ipsum dolor sit, amet consectetur adipisicing elit. Cupiditate odio
          placeat sapiente tempora in a voluptas laudantium possimus nisi unde
          veniam labore, nemo quo. Nisi repudiandae id non eum debitis. Lorem
          ipsum dolor sit amet consectetur adipisicing elit. Voluptatem iusto
          autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Sequi, magni culpa dolorem saepe explicabo numquam officia incidunt
          quis corporis assumenda. Sit maxime consequatur dignissimos aperiam
          consectetur, dolorum natus fuga numquam. Lorem ipsum dolor sit amet,
          consectetur adipisicing elit. Porro officiis, reiciendis quisquam
          voluptatibus sint aperiam atque. Pariatur dolorem error, porro modi
          maiores dolore dolores animi distinctio sint nesciunt vel eius. Lorem
          ipsum dolor sit, amet consectetur adipisicing elit. Cupiditate odio
          placeat sapiente tempora in a voluptas laudantium possimus nisi unde
          veniam labore, nemo quo. Nisi repudiandae id non eum debitis. Lorem
          ipsum dolor sit amet consectetur adipisicing elit. Voluptatem iusto
          autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Sequi, magni culpa dolorem saepe explicabo numquam officia incidunt
          quis corporis assumenda. Sit maxime consequatur dignissimos aperiam
          consectetur, dolorum natus fuga numquam. Lorem ipsum dolor sit amet,
          consectetur adipisicing elit. Porro officiis, reiciendis quisquam
          voluptatibus sint aperiam atque. Pariatur dolorem error, porro modi
          maiores dolore dolores animi distinctio sint nesciunt vel eius. Lorem
          ipsum dolor sit, amet consectetur adipisicing elit. Cupiditate odio
          placeat sapiente tempora in a voluptas laudantium possimus nisi unde
          veniam labore, nemo quo. Nisi repudiandae id non eum debitis. Lorem
          ipsum dolor sit amet consectetur adipisicing elit. Voluptatem iusto
          autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Sequi, magni culpa dolorem saepe explicabo numquam officia incidunt
          quis corporis assumenda. Sit maxime consequatur dignissimos aperiam
          consectetur, dolorum natus fuga numquam. Lorem ipsum dolor sit amet,
          consectetur adipisicing elit. Porro officiis, reiciendis quisquam
          voluptatibus sint aperiam atque. Pariatur dolorem error, porro modi
          maiores dolore dolores animi distinctio sint nesciunt vel eius. Lorem
          ipsum dolor sit, amet consectetur adipisicing elit. Cupiditate odio
          placeat sapiente tempora in a voluptas laudantium possimus nisi unde
          veniam labore, nemo quo. Nisi repudiandae id non eum debitis. Lorem
          ipsum dolor sit amet consectetur adipisicing elit. Voluptatem iusto
          autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Sequi, magni culpa dolorem saepe explicabo numquam officia incidunt
          quis corporis assumenda. Sit maxime consequatur dignissimos aperiam
          consectetur, dolorum natus fuga numquam. Lorem ipsum dolor sit amet,
          consectetur adipisicing elit. Porro officiis, reiciendis quisquam
          voluptatibus sint aperiam atque. Pariatur dolorem error, porro modi
          maiores dolore dolores animi distinctio sint nesciunt vel eius. Lorem
          ipsum dolor sit, amet consectetur adipisicing elit. Cupiditate odio
          placeat sapiente tempora in a voluptas laudantium possimus nisi unde
          veniam labore, nemo quo. Nisi repudiandae id non eum debitis. Lorem
          ipsum dolor sit amet consectetur adipisicing elit. Voluptatem iusto
          autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Sequi, magni culpa dolorem saepe explicabo numquam officia incidunt
          quis corporis assumenda. Sit maxime consequatur dignissimos aperiam
          consectetur, dolorum natus fuga numquam. Lorem ipsum dolor sit amet,
          consectetur adipisicing elit. Porro officiis, reiciendis quisquam
          voluptatibus sint aperiam atque. Pariatur dolorem error, porro modi
          maiores dolore dolores animi distinctio sint nesciunt vel eius. Lorem
          ipsum dolor sit, amet consectetur adipisicing elit. Cupiditate odio
          placeat sapiente tempora in a voluptas laudantium possimus nisi unde
          veniam labore, nemo quo. Nisi repudiandae id non eum debitis. Lorem
          ipsum dolor sit amet consectetur adipisicing elit. Voluptatem iusto
          autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Sequi, magni culpa dolorem saepe explicabo numquam officia incidunt
          quis corporis assumenda. Sit maxime consequatur dignissimos aperiam
          consectetur, dolorum natus fuga numquam. Lorem ipsum dolor sit amet,
          consectetur adipisicing elit. Porro officiis, reiciendis quisquam
          voluptatibus sint aperiam atque. Pariatur dolorem error, porro modi
          maiores dolore dolores animi distinctio sint nesciunt vel eius. Lorem
          ipsum dolor sit, amet consectetur adipisicing elit. Cupiditate odio
          placeat sapiente tempora in a voluptas laudantium possimus nisi unde
          veniam labore, nemo quo. Nisi repudiandae id non eum debitis. Lorem
          ipsum dolor sit amet consectetur adipisicing elit. Voluptatem iusto
          autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Sequi, magni culpa dolorem saepe explicabo numquam officia incidunt
          quis corporis assumenda. Sit maxime consequatur dignissimos aperiam
          consectetur, dolorum natus fuga numquam. Lorem ipsum dolor sit amet,
          consectetur adipisicing elit. Porro officiis, reiciendis quisquam
          voluptatibus sint aperiam atque. Pariatur dolorem error, porro modi
          maiores dolore dolores animi distinctio sint nesciunt vel eius. Lorem
          ipsum dolor sit, amet consectetur adipisicing elit. Cupiditate odio
          placeat sapiente tempora in a voluptas laudantium possimus nisi unde
          veniam labore, nemo quo. Nisi repudiandae id non eum debitis. Lorem
          ipsum dolor sit amet consectetur adipisicing elit. Voluptatem iusto
          autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Sequi, magni culpa dolorem saepe explicabo numquam officia incidunt
          quis corporis assumenda. Sit maxime consequatur dignissimos aperiam
          consectetur, dolorum natus fuga numquam. Lorem ipsum dolor sit amet,
          consectetur adipisicing elit. Porro officiis, reiciendis quisquam
          voluptatibus sint aperiam atque. Pariatur dolorem error, porro modi
          maiores dolore dolores animi distinctio sint nesciunt vel eius. Lorem
          ipsum dolor sit, amet consectetur adipisicing elit. Cupiditate odio
          placeat sapiente tempora in a voluptas laudantium possimus nisi unde
          veniam labore, nemo quo. Nisi repudiandae id non eum debitis. Lorem
          ipsum dolor sit amet consectetur adipisicing elit. Voluptatem iusto
          autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus. Lorem ipsum dolor sit amet consectetur adipisicing elit.
          Sequi, magni culpa dolorem saepe explicabo numquam officia incidunt
          quis corporis assumenda. Sit maxime consequatur dignissimos aperiam
          consectetur, dolorum natus fuga numquam. Lorem ipsum dolor sit amet,
          consectetur adipisicing elit. Porro officiis, reiciendis quisquam
          voluptatibus sint aperiam atque. Pariatur dolorem error, porro modi
          maiores dolore dolores animi distinctio sint nesciunt vel eius. Lorem
          ipsum dolor sit, amet consectetur adipisicing elit. Cupiditate odio
          placeat sapiente tempora in a voluptas laudantium possimus nisi unde
          veniam labore, nemo quo. Nisi repudiandae id non eum debitis.
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
#kitchensink {
  scrollbar-width: none;
}
</style>
