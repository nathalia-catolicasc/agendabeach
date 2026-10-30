export function isValidCpf(value: string) {
  if (!/^(?:[0-9]{11}|[0-9]{3}\.[0-9]{3}\.[0-9]{3}-[0-9]{2})$/.test(value)) return false

  const cpf = value.replace(/[.-]/g, '')
  if (/^([0-9])\1{10}$/.test(cpf)) return false

  for (let length = 9; length <= 10; length++) {
    let sum = 0
    for (let i = 0; i < length; i++) sum += Number(cpf[i]) * (length + 1 - i)
    const digit = ((sum * 10) % 11) % 10
    if (digit !== Number(cpf[length])) return false
  }
  return true
}
