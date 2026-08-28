import { useState } from 'react'
import type { FormEvent } from 'react'
import { Anchor, Button, Checkbox, PasswordInput, Stack, Text, TextInput, ThemeIcon, Title } from '@mantine/core'
import { ArrowIcon, CheckIcon, LockIcon, MailIcon, UserIcon } from '../Icons'
import './RegisterForm.css'

type RegisterFormProps = {
  onShowLogin: () => void
}

export function RegisterForm({ onShowLogin }: RegisterFormProps) {
  const [submitted, setSubmitted] = useState(false)

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitted(true)
  }

  if (submitted) {
    return (
      <div className="success-state" role="status">
        <ThemeIcon size={62} radius="xl" color="green" variant="light"><CheckIcon size={30} /></ThemeIcon>
        <Title order={3}>Conta criada!</Title>
        <Text c="dimmed" ta="center">Agora é só entrar e escolher a melhor quadra e horário para você.</Text>
        <Button color="gold" fullWidth onClick={onShowLogin}>Ir para o login</Button>
      </div>
    )
  }

  return (
    <form onSubmit={handleSubmit}>
      <Stack gap="sm">
        <TextInput label="Nome completo" placeholder="Como devemos chamar você?" required autoComplete="name" leftSection={<UserIcon />} />
        <TextInput label="E-mail" placeholder="seu@email.com" type="email" required autoComplete="email" leftSection={<MailIcon />} />
        <PasswordInput label="Senha" description="Use pelo menos 8 caracteres" placeholder="Crie uma senha segura" minLength={8} required autoComplete="new-password" leftSection={<LockIcon />} />
        <Checkbox
          required
          color="gold"
          mt={4}
          label={<Text span size="sm" c="dimmed">Li e aceito os <Anchor href="#" fw={600} c="brand.8">Termos de Uso</Anchor> e a <Anchor href="#" fw={600} c="brand.8">Política de Privacidade</Anchor>.</Text>}
        />
        <Button type="submit" fullWidth size="md" color="gold" rightSection={<ArrowIcon />} mt="xs">Criar minha conta</Button>
      </Stack>

      <Text ta="center" size="sm" c="dimmed" mt="xl">
        Já tem uma conta?{' '}
        <Anchor component="button" type="button" fw={700} c="brand.8" onClick={onShowLogin}>Fazer login</Anchor>
      </Text>
    </form>
  )
}
