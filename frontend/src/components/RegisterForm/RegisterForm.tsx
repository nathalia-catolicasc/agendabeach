import { useState } from 'react'
import type { FormEvent } from 'react'
import { Alert, Anchor, Button, Checkbox, PasswordInput, Stack, Text, TextInput, ThemeIcon, Title } from '@mantine/core'
import { ArrowIcon, CheckIcon, LockIcon, MailIcon, UserIcon } from '../Icons'
import { ApiError, register } from '../../api'
import { isValidCpf } from '../../cpf'
import './RegisterForm.css'

type RegisterFormProps = {
  onShowLogin: () => void
}

export function RegisterForm({ onShowLogin }: RegisterFormProps) {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [cpf, setCpf] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [submitted, setSubmitted] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    if (!isValidCpf(cpf.trim())) {
      setError('CPF inválido. Informe os 11 dígitos de um CPF válido.')
      return
    }
    setLoading(true)

    try {
      await register(name.trim(), email.trim(), password, cpf.trim().replace(/[.-]/g, ''))
      setSubmitted(true)
    } catch (caught) {
      setError(
        caught instanceof ApiError
          ? caught.status === 409
            ? 'E-mail ou CPF já cadastrado. Confira os dados ou faça login.'
            : caught.message
          : 'Não foi possível criar a conta. Tente novamente.',
      )
      setLoading(false)
    }
  }

  if (submitted) {
    return (
      <div className="success-state" role="status">
        <ThemeIcon size={62} radius="xl" color="green" variant="light">
          <CheckIcon size={30} />
        </ThemeIcon>
        <Title order={3}>Conta criada!</Title>
        <Text c="dimmed" ta="center">
          Agora é só entrar e escolher a melhor quadra e horário para você.
        </Text>
        <Button color="gold" fullWidth onClick={onShowLogin}>
          Ir para o login
        </Button>
      </div>
    )
  }

  return (
    <form onSubmit={handleSubmit}>
      <Stack gap="sm">
        {error && (
          <Alert color="red" variant="light" role="alert">
            {error}
          </Alert>
        )}

        <TextInput
          label="Nome completo"
          placeholder="Como devemos chamar você?"
          required
          autoComplete="name"
          maxLength={150}
          leftSection={<UserIcon />}
          value={name}
          onChange={(event) => setName(event.currentTarget.value)}
        />
        <TextInput
          label="E-mail"
          placeholder="seu@email.com"
          type="email"
          required
          autoComplete="email"
          maxLength={150}
          leftSection={<MailIcon />}
          value={email}
          onChange={(event) => setEmail(event.currentTarget.value)}
        />
        <TextInput
          label="CPF"
          placeholder="000.000.000-00"
          description="Usado para identificar suas reservas."
          required
          inputMode="numeric"
          maxLength={14}
          value={cpf}
          onChange={(event) => setCpf(event.currentTarget.value)}
        />
        <PasswordInput
          label="Senha"
          description="Use pelo menos 8 caracteres"
          placeholder="Crie uma senha segura"
          minLength={8}
          maxLength={72}
          required
          autoComplete="new-password"
          leftSection={<LockIcon />}
          value={password}
          onChange={(event) => setPassword(event.currentTarget.value)}
        />
        <Checkbox
          required
          color="gold"
          mt={4}
          label={
            <Text span size="sm" c="dimmed">
              Li e aceito os{' '}
              <Anchor href="#" fw={600} c="brand.8">
                Termos de Uso
              </Anchor>{' '}
              e a{' '}
              <Anchor href="#" fw={600} c="brand.8">
                Política de Privacidade
              </Anchor>
              .
            </Text>
          }
        />
        <Button type="submit" fullWidth size="md" color="gold" rightSection={<ArrowIcon />} mt="xs" loading={loading}>
          Criar minha conta
        </Button>
      </Stack>

      <Text ta="center" size="sm" c="dimmed" mt="xl">
        Já tem uma conta?{' '}
        <Anchor component="button" type="button" fw={700} c="brand.8" onClick={onShowLogin}>
          Fazer login
        </Anchor>
      </Text>
    </form>
  )
}
