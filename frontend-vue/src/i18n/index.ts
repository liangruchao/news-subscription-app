import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN.ts'
import enUS from './locales/en-US.ts'

const i18n = createI18n({
  legacy: false,
  locale: 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS,
  },
})

export function setLanguage(lang) {
  i18n.global.locale.value = lang
}

export default i18n
