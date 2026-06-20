import { defineStore } from 'pinia'

export const useBreadcrumbStore = defineStore('breadcrumb', {
  state: () => ({
    history: []
  }),
  actions: {
    add(item) {
      if (!item?.path || !item?.title) return
      const existsIndex = this.history.findIndex(h => h.path === item.path)
      if (existsIndex !== -1) {
        // 将已存在的项移动到末尾，形成最近访问顺序
        const [exist] = this.history.splice(existsIndex, 1)
        this.history.push(exist)
      } else {
        this.history.push({ path: item.path, title: item.title })
      }
    },
    remove(path) {
      this.history = this.history.filter(h => h.path !== path)
    },
    clear() {
      this.history = []
    }
  }
}) 