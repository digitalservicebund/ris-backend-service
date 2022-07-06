<script lang="ts" setup>
import { updateDocUnit } from "../api/docUnitService"
import { useDocUnitsStore } from "../store"
import { DocUnit } from "../types/DocUnit"
import SimpleButton from "./SimpleButton.vue"

const store = useDocUnitsStore()

interface StammDatenListEntry {
  id: keyof DocUnit
  name: string
  label: string
  aria: string
  icon: string
}

const stammdatenDef: StammDatenListEntry[] = []

const add = (id: keyof DocUnit, name: string, icon: string) => {
  stammdatenDef.push({
    id: id,
    name: name,
    label: name,
    aria: name, // use these on the input fields, :aria-labelledby="abc" doesn't work anymore since vue-tsc 0.36.0 TODO
    icon: icon,
  })
}

add("aktenzeichen", "Aktenzeichen", "grid_3x3")
add("gerichtstyp", "Gerichtstyp", "home")
add("dokumenttyp", "Dokumenttyp", "category")
add("vorgang", "Vorgang", "inventory_2")
add("ecli", "ECLI", "translate")
add("spruchkoerper", "Spruchkörper", "people_alt")
add("entscheidungsdatum", "Entscheidungsdatum", "calendar_today")
add("gerichtssitz", "Gerichtssitz", "location_on")
add("rechtskraft", "Rechtskraft", "gavel")
add("eingangsart", "Eingangsart", "markunread_mailbox")
add("dokumentationsstelle", "Dokumentationsstelle", "school")
add("region", "Region", "map")

const onSubmit = () => {
  updateDocUnit(store.getSelected()).then((updatedDocUnit) => {
    store.update(updatedDocUnit)
  })
  alert("Stammdaten wurden gespeichert")
}
</script>

<template>
  <div v-if="!store.hasSelected()">Loading...</div>
  <div v-else>
    <form novalidate class="ris-form" @submit.prevent="onSubmit">
      <v-row>
        <v-col><h2>Stammdaten</h2></v-col>
      </v-row>
      <v-row>
        <v-col cols="6">
          <!-- ^ removed md="6" because vue-tsc 0.36.0 throws an error TODO -->
          <template v-for="(item, index) in stammdatenDef">
            <div v-if="index <= 5" :key="item.id" class="ris-form__textfield">
              <v-icon class="icon_stammdaten">
                {{ item.icon }}
              </v-icon>
              <label :for="item.name" class="ris-form__label">
                {{ item.label }}
                <input
                  :id="item.id"
                  v-model="store.getSelectedSafe()[item.id]"
                  class="ris-form__input"
                  type="text"
                  :name="item.name"
                />
              </label>
            </div>
          </template>
        </v-col>
        <v-col cols="6">
          <template v-for="(item, index) in stammdatenDef">
            <div v-if="index > 5" :key="item.id" class="ris-form__textfield">
              <v-icon class="icon_stammdaten">
                {{ item.icon }}
              </v-icon>
              <label :for="item.name" class="ris-form__label">
                {{ item.label }}
                <input
                  :id="item.id"
                  v-model="store.getSelectedSafe()[item.id]"
                  class="ris-form__input"
                  type="text"
                  :name="item.name"
                />
              </label>
            </div>
          </template>
        </v-col>
      </v-row>
      <v-row>
        <v-col>
          <div class="ris-form__textfield">
            <SimpleButton @click="onSubmit" />
          </div>
        </v-col>
      </v-row>
    </form>
  </div>
</template>

<style lang="scss">
.ris-form {
  padding: rem(20px);

  &__textfield {
    padding: rem(20px);
  }

  &__input {
    width: 100%;
    padding: 17px 24px;
    margin-top: 5px;
    outline: 2px solid $text-tertiary;
    resize: vertical;

    &:hover,
    &:focus {
      // outline-width: 4px;
    }
  }

  &__label {
    padding: 12px 12px 20px 0;
  }
}
.icon_stammdaten {
  margin: 0 5px 5px 0;
}
</style>
