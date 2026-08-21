import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { createTheme, MantineProvider } from '@mantine/core'
import '@mantine/core/styles.css'
import './index.css'
import App from './App.tsx'

const theme = createTheme({
  fontFamily: "'Manrope', system-ui, sans-serif",
  headings: { fontFamily: "'Manrope', system-ui, sans-serif" },
  primaryColor: 'brand',
  colors: {
    brand: ['#e8f4fb', '#cde7f6', '#9acde9', '#62b1db', '#3699d0', '#1d88c8', '#117fc5', '#0d6eae', '#0f4f7c', '#093a5f'],
    gold: ['#fff8e3', '#fcecc4', '#f6d98b', '#f1c74e', '#edbb2d', '#e8b441', '#d69d24', '#bd861b', '#9f7018', '#855d15'],
  },
  defaultRadius: 'md',
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <MantineProvider theme={theme} defaultColorScheme="light"><App /></MantineProvider>
  </StrictMode>,
)
