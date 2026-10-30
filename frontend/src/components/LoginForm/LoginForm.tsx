import { useState } from 'react'
import type { FormEvent } from 'react'
import { Alert, Anchor, Button, Checkbox, Divider, Group, PasswordInput, Stack, Text, TextInput } from '@mantine/core'
import { ArrowIcon, LockIcon, MailIcon } from '../Icons'
import { ApiError, login } from '../../api'
import type { SessionUser } from '../../session'

type LoginFormProps = {
  onLogin: (user: SessionUser) => void
  onShowRegister: () => void
}

export function LoginForm({ onLogin, onShowRegister }: LoginFormProps) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setLoading(true)

    try {
      const user = await login(email.trim(), password)
      onLogin(user)
    } catch (caught) {
      setError(caught instanceof ApiError && caught.status !== 401 ? caught.message : 'E-mail ou senha inválidos.')
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <Stack gap="md">
        {error && (
          <Alert color="red" variant="light" role="alert">
            {error}
          </Alert>
        )}

        <TextInput
          label="E-mail"
          placeholder="seu@email.com"
          type="email"
          required
          autoComplete="email"
          leftSection={<MailIcon />}
          value={email}
          onChange={(event) => setEmail(event.currentTarget.value)}
        />
        <PasswordInput
          label="Senha"
          placeholder="Digite sua senha"
          required
          autoComplete="current-password"
          leftSection={<LockIcon />}
          value={password}
          onChange={(event) => setPassword(event.currentTarget.value)}
        />
        <Group justify="space-between" align="center" mt={2}>
          <Checkbox label="Lembrar de mim" color="gold" defaultChecked />
          <Anchor component="button" type="button" size="sm" fw={600} c="brand.8">
            Esqueci minha senha
          </Anchor>
        </Group>
        <Button type="submit" fullWidth size="md" color="gold" rightSection={<ArrowIcon />} mt="xs" loading={loading}>
          Entrar na minha conta
        </Button>
      </Stack>

      <Divider label="ou" labelPosition="center" my="xl" />
      <Text ta="center" size="sm" c="dimmed">
        Ainda não tem uma conta?{' '}
        <Anchor component="button" type="button" fw={700} c="brand.8" onClick={onShowRegister}>
          Cadastre-se grátis
        </Anchor>
      </Text>
    </form>
  )
}
