<template>
  <div class="container p-4">
    <div class="text-xl">
      <h1>Shogun Miniatures Tray Calculator</h1>
      <p>
        This is an unofficial calculator to help expediate the ordering process when purchasing trays from Shogun
        Miniatures.
      </p>

      <p>
        Values are derived from the tables listed at <a
          href="http://shogunminiatures.com/shogunminjaturessteeltrays.html">Shogun Miniatures</a>.
        Trays calculated from dimensions not listed at the link above will not show a price but are included to make
        ordering a custom size easier
      </p>
    </div>
    <form>
      <div class="mb-3 row g-3 align-items-center">
        <div class="col-6-md">
          <label for="edge-type" class="form-label">Edge Type</label>
          <select id="edge-type" class="form-control" v-model="itemModel.edgeType">
            <option v-for="t in ['flanged', 'flat']" :value="t"> {{ t }} </option>
          </select>
        </div>
        <div class="col-6-md">
          <label for="base-size" class="form-label">Base Size</label>
          <select id="base-size" class="form-control" v-model.number="itemModel.baseSize">
            <option v-for="bs in [20, 25, 40, 50]" :value="bs"> {{ bs }}mm </option>
          </select>
        </div>
      </div>
      <div class="mb-3 row g-3 align-items-center">
        <div class="col-6-md">
          <label for="width" class="form-label">Width</label>
          <select id="width" class="form-control" v-model.number="itemModel.width">
            <option v-for="n in R.range(1, 11)" :value="n"> {{ n }} </option>
          </select>
        </div>
        <div class="col-6-md">
          <label for="depth" class="form-label">Depth</label>
          <select id="depth" class="form-control" v-model.number="itemModel.depth">
            <option v-for="n in R.range(1, 11)" :value="n"> {{ n }} </option>
          </select>
        </div>
      </div>
      <div class="row align-items-end justify-content-end">
        <div class="col-6">
          <label for="price" class="form-label">Price</label>
          <input id="price" disabled class="form-control" :value="formatPrice(price)" />
        </div>
        <div class="col-6 d-grid">
          <a @click="addItem" class="btn btn-primary">
            Add
          </a>
        </div>
      </div>
    </form>
    <table v-if="!R.isEmpty(lineItems)" class="table">
      <thead>
        <tr>
          <th>Frontage x Depth</th>
          <th>Edge Type</th>
          <th>Base Size</th>
          <th>Width</th>
          <th>Depth</th>
          <th>Price ea.</th>
          <th>Quantity</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="i in R.values(lineItems)" :key="i.key">
          <td>{{ computeDimensions(i.item) }}</td>
          <td>{{ i.item.edgeType }}</td>
          <td>{{ i.item.baseSize }}mm</td>
          <td>{{ i.item.width }}</td>
          <td>{{ i.item.depth }}</td>
          <td>{{ formatPrice(i.item.price, true) }}</td>
          <td>
            <a class="mr-2 btn btn-primary" @click="increment(i.key)">+</a>
            {{ i.quantity }}
            <a class="btn btn-primary" @click="decrement(i.key)">-</a>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import * as R from 'remeda'
import priceData from '../price-map.json'

type Item = {
  width: number
  depth: number
  edgeType: string
  baseSize: number
  price: number | null
}

const itemModel = ref<Item>({
  width: 1,
  baseSize: 20,
  depth: 1,
  edgeType: "flanged",
  price: null
})

const itemToKey = ({ edgeType, baseSize, width, depth }: Item): string =>
  [edgeType, baseSize, width, depth].join("-")


const computeDimensions = ({baseSize, width, depth}: Item): string =>
  `${baseSize * width}mm x ${baseSize * depth}mm`

type LineItem = {
  item: Item,
  quantity: number
  key: string
}

const lineItems = ref<{ [key: string]: LineItem }>({})

const addItem = () => {
  const key: string = itemToKey(itemModel.value)

  if (lineItems.value[key]) {
    lineItems.value[key].quantity++
  } else {
    const addedItem = R.clone(itemModel.value)
    addedItem.price = price.value

    lineItems.value[key] = {
      quantity: 1,
      item: addedItem,
      key
    }
  }
}

const increment = (key) => {
  if (lineItems.value[key]) {
    lineItems.value[key].quantity++
  }
}

const decrement = (key) => {
  if (lineItems.value[key]) {
    if (lineItems.value[key].quantity === 1) {
      lineItems.value = R.omit(lineItems.value, [key])
    } else {
      lineItems.value[key].quantity--
    }
  }
}

const price = computed(() =>
  priceData[itemToKey(itemModel.value)]
)

const formatPrice = (price: number | null, short: boolean = false): string => {
  if (price) {
    return `\$${price.toFixed(2)}`
  } else if (short) {
    return "--"
  } else {
    return "Please contact for pricing"
  }
}
</script>

