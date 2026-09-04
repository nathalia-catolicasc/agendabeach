type PricedReservation = {
  hours: number
  pricePerHour: number
  equipments: { price: number; quantity: number }[]
}

export function getReservationCosts({ hours, pricePerHour, equipments }: PricedReservation) {
  const courtTotal = pricePerHour * hours
  const equipmentTotal = equipments.reduce((total, { price, quantity }) => total + price * quantity, 0)
  const serviceFee = (courtTotal + equipmentTotal) * 0.05

  return { courtTotal, equipmentTotal, serviceFee, total: courtTotal + equipmentTotal + serviceFee }
}
