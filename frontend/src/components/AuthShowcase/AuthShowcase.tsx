import { Group, Text, Title } from '@mantine/core'
import { CheckIcon } from '../Icons'
import { CourtArtwork } from '../CourtArtwork'
import { Logo } from '../Logo'
import './AuthShowcase.css'

export function AuthShowcase() {
  return (
    <section className="showcase">
      <Logo showTagline />

      <div className="showcase__content">
        <Text className="showcase__eyebrow">SEU JOGO COMEÇA AQUI</Text>
        <Title className="showcase__title" order={1}>Menos espera.<br />Mais <span>Beach Tennis.</span></Title>
        <Text className="showcase__copy">Encontre sua quadra, escolha o melhor horário e faça sua reserva em poucos minutos.</Text>
        <Group className="showcase__features" gap="xl">
          <div><CheckIcon /><span>Reserva rápida</span></div>
          <div><CheckIcon /><span>Horários em tempo real</span></div>
        </Group>
      </div>

      <CourtArtwork />
      <Text className="showcase__footer">© 2026 Agenda Beach</Text>
    </section>
  )
}
