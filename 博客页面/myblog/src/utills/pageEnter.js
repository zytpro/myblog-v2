export function usePageEnter(rootSelector) {
  const apply = () => {
    const el = document.querySelector(rootSelector)
    if (!el) return
    el.style.opacity = '0'
    el.style.transform = 'translateY(12px)'
    el.style.transition = 'opacity 0.3s ease, transform 0.3s ease'
    requestAnimationFrame(() => {
      el.style.opacity = '1'
      el.style.transform = 'translateY(0)'
    })
  }

  return { apply }
}
