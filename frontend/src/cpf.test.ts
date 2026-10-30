import { describe, expect, it } from 'vitest'
import { isValidCpf } from './cpf'

describe('CPF validation', () => {
  it.each(['52998224725', '529.982.247-25', '11144477735', '111.444.777-35'])('accepts %s', (cpf) => {
    expect(isValidCpf(cpf)).toBe(true)
  })

  it.each(['', '123', '52998224724', '11111111111', '00000000000', '529 982 247 25', '529.982.247-2x'])(
    'rejects %s',
    (cpf) => {
      expect(isValidCpf(cpf)).toBe(false)
    },
  )
})
