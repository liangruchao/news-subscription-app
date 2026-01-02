import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        ws: true,
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            // 设置Origin为后端地址
            proxyReq.setHeader('Origin', 'http://localhost:8081')
            // 确保Cookie被正确传递
            if (req.headers.cookie) {
              proxyReq.setHeader('Cookie', req.headers.cookie)
            }
            // 设置Referer
            proxyReq.setHeader('Referer', 'http://localhost:8081/')
          })
          proxy.on('proxyRes', (proxyRes, req, res) => {
            // 确保Set-Cookie被正确传递
            if (proxyRes.headers['set-cookie']) {
              const cookies = proxyRes.headers['set-cookie']
              // 删除SameSite属性或设置为Lax，以确保Cookie能正确传递
              const modifiedCookies = cookies.map((cookie: string) => {
                if (!cookie.includes('SameSite')) {
                  return cookie + '; SameSite=Lax'
                }
                return cookie
              })
              proxyRes.headers['set-cookie'] = modifiedCookies
            }
          })
        }
      },
    },
    fs: {
      strict: false,
    },
    headers: {
      'Cache-Control': 'no-cache, no-store, must-revalidate',
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
    minify: 'terser',
  },
  optimizeDeps: {
    force: true,
  },
})
