<script setup lang="ts">
import { computed, ref, watch } from "vue"
import { useRoute } from "vue-router"
import TextEditor from "@/shared/components/input/TextEditor.vue"

const props = defineProps<{
  open?: boolean
  hasFile: boolean
  file?: string
}>()

const emit = defineEmits<{ (e: "update:open", value: boolean): void }>()

const open = ref(false)

watch(
  () => props.open,
  () => (open.value = props.open ?? false),
  { immediate: true }
)

watch(open, () => emit("update:open", open.value))
const route = useRoute()

const uploadFileRoute = computed(() => ({
  name: "caselaw-documentUnit-:documentNumber-files",
  params: { documentNumber: route.params.documentNumber },
  query: route.query,
}))
</script>

<script lang="ts">
// As this component does not have a root node, Vue throws a warning that Extraneous non-props attributes (class)
// were passed to component but could not be automatically inherited. This disables attribute inheritance to avoid
// the warning. An alternative could be to wrap the component with another root div.
export default {
  inheritAttrs: false,
}
</script>

<template>
  <div v-bind="$attrs">
    <div class="basis-1/3! flex flex-col gap-56 w-full">
      <div class="flex items-center">
        <h2 class="grow heading-02-regular">Originaldokument</h2>
      </div>

      <div v-if="!hasFile" class="flex flex-col gap-24">
        <span class="material-icons odoc-upload-icon">cloud_upload</span>

        Es wurde noch kein Originaldokument hochgeladen.

        <router-link
          class="flex gap-2 items-center link-01-bold"
          :to="uploadFileRoute"
        >
          <span class="material-icons">arrow_forward</span>
          <span>Zum Upload</span>
        </router-link>
      </div>

      <div v-else-if="!file">Dokument wird geladen</div>

      <div v-else class="border-1 border-gray-400 border-solid overflow-scroll">
        <TextEditor element-id="odoc" field-size="max" :value="file" />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.odoc-open {
  display: flex;
  height: 65px;
  align-items: center; // align vertical
  justify-content: center; // align horizontal
  border-radius: 10px;
  margin-right: 40px;
  transform: rotate(-90deg);
  transform-origin: right;
}

.odoc-open-text {
  margin-left: 30px;
}

.odoc-open-icon-background {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: white;
  transform: rotate(90deg) translateY(-25px);
}

.odoc-open-icon {
  margin-top: 8px;
  margin-right: 9px;
}

.odoc-upload-icon {
  font-size: 50px;
  @apply text-blue-800;
}
</style>
