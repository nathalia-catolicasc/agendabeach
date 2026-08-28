import type { FormEvent } from 'react'
import { Anchor, Button, Checkbox, Divider, Group, PasswordInput, Stack, Text, TextInput } from '@mantine/core'
import { ArrowIcon, LockIcon, MailIcon } from '../Icons'

type LoginFormProps = {
  onLogin: () => void
  onShowRegister: () => void
}

export function LoginForm({ onLogin, onShowRegister }: LoginFormProps) {
  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    onLogin()
  }

  return (
    <form onSubmit={handleSubmit}>
      <Stack gap="md">
        <TextInput label="E-mail" placeholder="seu@email.com" type="email" required autoComplete="email" leftSection={<MailIcon />} />
        <PasswordInput label="Senha" placeholder="Digite sua senha" required autoComplete="current-password" leftSection={<LockIcon />} />
        <Group justify="space-between" align="center" mt={2}>
          <Checkbox label="Lembrar de mim" color="gold" />
          <Anchor component="button" type="button" size="sm" fw={600} c="brand.8">Esqueci minha senha</Anchor>
        </Group>
        <Button type="submit" fullWidth size="md" color="gold" rightSection={<ArrowIcon />} mt="xs">Entrar na minha conta</Button>
      </Stack>

      <Divider label="ou" labelPosition="center" my="xl" />
      <Text ta="center" size="sm" c="dimmed">
        Ainda não tem uma conta?{' '}
        <Anchor component="button" type="button" fw={700} c="brand.8" onClick={onShowRegister}>Cadastre-se grátis</Anchor>
      </Text>
    </form>
  )
}
