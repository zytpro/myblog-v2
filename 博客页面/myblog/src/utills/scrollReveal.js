import { onMounted, onUnmounted, nextTick } from 'vue'

export function useScrollReveal(selector = '.fade-in-up, .fade-in-left, .fade-in-right, .fade-in-scale') {
  let observer = null

  const start = () => {
    observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting && !entry.target.classList.contains('visible')) {
          entry.target.classList.add('visible')
        }
      })
    }, {
      threshold: 0.15,
      rootMargin: '0px 0px -40px 0px'
    })

    document.querySelectorAll(selector).forEach(el => {
      if (!el.classList.contains('visible')) {
        observer.observe(el)
      }
    })
  }

  const refresh = () => {
    nextTick(() => {
      document.querySelectorAll(selector).forEach(el => {
        if (!el.classList.contains('visible')) {
          const rect = el.getBoundingClientRect()
          if (rect.top < window.innerHeight + 40) {
            el.classList.add('visible')
          } else if (observer) {
            observer.observe(el)
          }
        }
      })
    })
  }

  onMounted(() => start())
  onUnmounted(() => {
    if (observer) observer.disconnect()
  })

  return { refresh }
}
