<script setup lang="ts">
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import TextEditor from "@/components/input/TextEditor.vue"

const props = defineProps<{
  content: string
}>()
</script>

<template>
  <FlexContainer
    v-if="props.content"
    class="sticky top-0 flex w-full flex-col gap-40 p-24"
    v-bind="$attrs"
  >
    <FlexItem
      class="h-[65vh] overflow-scroll border-1 border-solid border-gray-400"
    >
      <TextEditor
        data-testid="text-editor"
        element-id="odoc"
        field-size="max"
        :value="props.content"
      />
    </FlexItem>
  </FlexContainer>
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
</style>
