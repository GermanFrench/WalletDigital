import { useEffect, useMemo, useState } from 'react'

const BALANCE_VISIBILITY_KEY = 'wallet-ui:show-balance'
const SAVINGS_GOALS_KEY = 'wallet-ui:savings-goals'

type SavingsGoal = {
  id: string
  label: string
  saved: number
  target: number
  createdAt: number
}

type GoalSort = 'newest' | 'progress-desc'
type GoalFilter = 'all' | 'pending' | 'done'
type TxType = 'income' | 'expense'

type Transaction = {
  id: string
  label: string
  amount: number
  type: TxType
  createdAt: number
}

type AccountResponse = {
  id: string
  balance: number
  currency: string
  accountNumber?: string
  alias?: string
}

type ApiTransaction = {
  id: string
  accountId: string
  amount: number
  type: 'CREDIT' | 'DEBIT'
  status: string
  timestamp: string
  description: string
  relatedAccountId: string | null
}

type AuthResponse = {
  accessToken: string
  tokenType: string
  expiresInSeconds: number
  refreshToken?: string
}

type ServiceStatus = 'pending' | 'paid'

type Service = {
  id: string
  label: string
  amount: number
  dueDate: string
  status: ServiceStatus
  createdAt: number
}

const formatDate = (ts: number) => {
  const diff = Math.floor((Date.now() - ts) / 86400000)
  if (diff === 0) return 'Hoy'
  if (diff === 1) return 'Ayer'
  return new Date(ts).toLocaleDateString('es-AR', { day: 'numeric', month: 'short' })
}

const DEFAULT_SAVINGS_GOALS: SavingsGoal[] = []

const TRANSACTIONS_KEY = 'wallet-ui:transactions'
const AUTH_TOKEN_KEY = 'wallet-ui:auth-token'
const REFRESH_TOKEN_KEY = 'wallet-ui:refresh-token'
const ACCOUNT_ID_KEY = 'wallet-ui:account-id'
const USER_EMAIL_KEY = 'wallet-ui:user-email'
const ACCOUNT_NUMBER_KEY = 'wallet-ui:account-number'
const ACCOUNT_ALIAS_KEY = 'wallet-ui:account-alias'
const SERVICES_KEY = 'wallet-ui:services'
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
const BASE_AMOUNT = 0

const DEFAULT_TRANSACTIONS: Transaction[] = []

const LANDING_FEATURES = [
  {
    title: 'Control inteligente en tiempo real',
    description:
      'Cada movimiento impacta tus métricas al instante. No recargás ni actualizás manualmente: ves el estado financiero real mientras tomás decisiones.',
    stat: 'Actualización inmediata',
    image: '/cartoon-live.svg',
  },
  {
    title: 'Visualización clara y accionable',
    description:
      'Gráficos de dona y barras para detectar equilibrio entre ingresos y gastos. Menos tablas confusas, más señales visuales para actuar rápido.',
    stat: 'Insights en 7 días',
    image: '/cartoon-visual.svg',
  },
  {
    title: 'Objetivos que sí se cumplen',
    description:
      'Tus metas de ahorro tienen progreso visible, edición simple y orden por prioridad. Eso convierte una intención en un plan medible.',
    stat: 'Progreso medible',
    image: '/cartoon-goals.svg',
  },
]

const LANDING_WORKFLOW = [
  {
    step: '01',
    title: 'Conectás tus hábitos',
    description: 'Registrás ingresos y gastos diarios con una carga mínima.',
  },
  {
    step: '02',
    title: 'La wallet organiza y resume',
    description: 'Consolida movimientos y te da un resumen mensual claro.',
  },
  {
    step: '03',
    title: 'Tomás decisiones con datos',
    description: 'Ajustás metas, recortás gastos y proyectás con confianza.',
  },
]

function App() {
  const [screen, setScreen] = useState<'home' | 'auth' | 'dashboard'>('home')
  const [authMode, setAuthMode] = useState<'login' | 'register' | 'forgot-password' | 'reset-password'>('login')
  const [authName, setAuthName] = useState('')
  const [authEmail, setAuthEmail] = useState('')
  const [authPassword, setAuthPassword] = useState('')
  const [authConfirmPassword, setAuthConfirmPassword] = useState('')
  const [authError, setAuthError] = useState('')
  const [authLoading, setAuthLoading] = useState(false)
  const [sessionNotice, setSessionNotice] = useState('')
  const [forgotEmail, setForgotEmail] = useState('')
  const [forgotLoading, setForgotLoading] = useState(false)
  const [forgotError, setForgotError] = useState('')
  const [forgotSuccess, setForgotSuccess] = useState('')
  const [resetToken, setResetToken] = useState('')
  const [resetNewPassword, setResetNewPassword] = useState('')
  const [resetConfirm, setResetConfirm] = useState('')
  const [resetLoading, setResetLoading] = useState(false)
  const [resetError, setResetError] = useState('')
  const [resetSuccess, setResetSuccess] = useState('')
  const [accountId, setAccountId] = useState<string | null>(() => {
    if (typeof window === 'undefined') return null
    return window.localStorage.getItem(ACCOUNT_ID_KEY)
  })
  const [accountBalance, setAccountBalance] = useState<number | null>(null)
  const [accountSyncError, setAccountSyncError] = useState('')
  const [userEmail, setUserEmail] = useState<string>(() => {
    if (typeof window === 'undefined') return ''
    return window.localStorage.getItem(USER_EMAIL_KEY) ?? ''
  })
  const [accountNumber, setAccountNumber] = useState<string>(() => {
    if (typeof window === 'undefined') return ''
    return window.localStorage.getItem(ACCOUNT_NUMBER_KEY) ?? ''
  })
  const [accountAlias, setAccountAlias] = useState<string>(() => {
    if (typeof window === 'undefined') return ''
    return window.localStorage.getItem(ACCOUNT_ALIAS_KEY) ?? ''
  })

  const [showDepositModal, setShowDepositModal] = useState(false)
  const [depositAmount, setDepositAmount] = useState('')
  const [depositLoading, setDepositLoading] = useState(false)
  const [depositError, setDepositError] = useState('')
  const [showTransferModal, setShowTransferModal] = useState(false)
  const [transferDestId, setTransferDestId] = useState('')
  const [transferAmount, setTransferAmount] = useState('')
  const [transferLoading, setTransferLoading] = useState(false)
  const [transferError, setTransferError] = useState('')

  const [showBalance, setShowBalance] = useState(() => {
    if (typeof window === 'undefined') {
      return true
    }

    const savedPreference = window.localStorage.getItem(BALANCE_VISIBILITY_KEY)
    return savedPreference === null ? true : savedPreference === 'true'
  })
  const [newGoalName, setNewGoalName] = useState('')
  const [newGoalTarget, setNewGoalTarget] = useState('')
  const [newGoalSaved, setNewGoalSaved] = useState('')
  const [editingGoalId, setEditingGoalId] = useState<string | null>(null)
  const [editingGoalName, setEditingGoalName] = useState('')
  const [editingGoalTarget, setEditingGoalTarget] = useState('')
  const [editingGoalSaved, setEditingGoalSaved] = useState('')
  const [goalSort, setGoalSort] = useState<GoalSort>('newest')
  const [goalFilter, setGoalFilter] = useState<GoalFilter>('all')
  const [savingsGoals, setSavingsGoals] = useState<SavingsGoal[]>(() => {
    if (typeof window === 'undefined') {
      return DEFAULT_SAVINGS_GOALS
    }

    const rawGoals = window.localStorage.getItem(SAVINGS_GOALS_KEY)

    if (!rawGoals) {
      return DEFAULT_SAVINGS_GOALS
    }

    try {
      const parsed = JSON.parse(rawGoals) as Array<
        Omit<SavingsGoal, 'createdAt'> & { createdAt?: number }
      >
      if (!Array.isArray(parsed)) {
        return DEFAULT_SAVINGS_GOALS
      }

      return parsed
        .filter(
          (goal) =>
            typeof goal.id === 'string' &&
            typeof goal.label === 'string' &&
            typeof goal.saved === 'number' &&
            typeof goal.target === 'number',
        )
        .map((goal, index) => ({
          ...goal,
          createdAt: goal.createdAt ?? Date.now() - index,
        }))
    } catch {
      return DEFAULT_SAVINGS_GOALS
    }
  })

  const [showTxForm, setShowTxForm] = useState(false)
  const [newTxLabel, setNewTxLabel] = useState('')
  const [newTxAmount, setNewTxAmount] = useState('')
  const [newTxType, setNewTxType] = useState<TxType>('expense')
  const [transactions, setTransactions] = useState<Transaction[]>(() => {
    if (typeof window === 'undefined') return DEFAULT_TRANSACTIONS
    const raw = window.localStorage.getItem(TRANSACTIONS_KEY)
    if (!raw) return DEFAULT_TRANSACTIONS
    try {
      const parsed = JSON.parse(raw) as Transaction[]
      if (!Array.isArray(parsed)) return DEFAULT_TRANSACTIONS
      return parsed.filter(
        (tx) =>
          typeof tx.id === 'string' &&
          typeof tx.label === 'string' &&
          typeof tx.amount === 'number' &&
          (tx.type === 'income' || tx.type === 'expense'),
      )
    } catch {
      return DEFAULT_TRANSACTIONS
    }
  })

  const [services, setServices] = useState<Service[]>(() => {
    if (typeof window === 'undefined') return []
    const raw = window.localStorage.getItem(SERVICES_KEY)
    if (!raw) return []
    try {
      const parsed = JSON.parse(raw) as Service[]
      if (!Array.isArray(parsed)) return []
      return parsed.filter(
        (s) =>
          typeof s.id === 'string' &&
          typeof s.label === 'string' &&
          typeof s.amount === 'number' &&
          (s.status === 'pending' || s.status === 'paid'),
      )
    } catch {
      return []
    }
  })
  const [showServiceForm, setShowServiceForm] = useState(false)
  const [newServiceLabel, setNewServiceLabel] = useState('')
  const [newServiceAmount, setNewServiceAmount] = useState('')
  const [newServiceDueDate, setNewServiceDueDate] = useState('')

  const balance = useMemo(
    () =>
      transactions.reduce(
        (acc, tx) => (tx.type === 'income' ? acc + tx.amount : acc - tx.amount),
        BASE_AMOUNT,
      ),
    [transactions],
  )

  const effectiveBalance = accountBalance ?? balance
  const formattedBalance = useMemo(() => `$${effectiveBalance.toLocaleString('es-AR')}`, [effectiveBalance])

  const clearLocalSession = () => {
    window.localStorage.removeItem(AUTH_TOKEN_KEY)
    window.localStorage.removeItem(REFRESH_TOKEN_KEY)
    window.localStorage.removeItem(ACCOUNT_ID_KEY)
    window.localStorage.removeItem(USER_EMAIL_KEY)
    window.localStorage.removeItem(ACCOUNT_NUMBER_KEY)
    window.localStorage.removeItem(ACCOUNT_ALIAS_KEY)
    window.localStorage.removeItem(TRANSACTIONS_KEY)
    window.localStorage.removeItem(SAVINGS_GOALS_KEY)
    window.localStorage.removeItem(SERVICES_KEY)
    setAccountId(null)
    setAccountBalance(null)
    setAccountNumber('')
    setAccountAlias('')
    setUserEmail('')
    setTransactions([])
    setSavingsGoals([])
    setServices([])
    setAuthPassword('')
    setAuthConfirmPassword('')
    setAuthLoading(false)
    setAuthError('')
    setAccountSyncError('')
    setScreen('auth')
    setAuthMode('login')
  }

  const storeAuthTokens = (auth: AuthResponse) => {
    window.localStorage.setItem(AUTH_TOKEN_KEY, auth.accessToken)
    if (auth.refreshToken) {
      window.localStorage.setItem(REFRESH_TOKEN_KEY, auth.refreshToken)
    }
  }

  const getJwtExpiration = (token: string) => {
    try {
      const payload = token.split('.')[1]
      if (!payload) return null
      const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
      const decoded = window.atob(normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '='))
      const data = JSON.parse(decoded) as { exp?: number }
      return typeof data.exp === 'number' ? data.exp * 1000 : null
    } catch {
      return null
    }
  }

  const isTokenExpiringSoon = (token: string, thresholdMs = 60_000) => {
    const expiration = getJwtExpiration(token)
    if (!expiration) return true
    return expiration - Date.now() <= thresholdMs
  }

  const refreshAccessToken = async () => {
    const refreshToken = window.localStorage.getItem(REFRESH_TOKEN_KEY)
    if (!refreshToken) {
      throw new Error('No hay refresh token disponible.')
    }

    const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    })

    const data = (await response.json()) as Partial<AuthResponse> & { message?: string }
    if (!response.ok || !data.accessToken) {
      throw new Error(data.message || 'Tu sesión expiró. Volvé a iniciar sesión.')
    }

    storeAuthTokens({
      accessToken: data.accessToken,
      tokenType: data.tokenType ?? 'Bearer',
      expiresInSeconds: data.expiresInSeconds ?? 0,
      refreshToken: data.refreshToken,
    })

    return data.accessToken
  }

  const getValidAccessToken = async () => {
    const token = window.localStorage.getItem(AUTH_TOKEN_KEY)
    if (!token) {
      return refreshAccessToken()
    }
    if (isTokenExpiringSoon(token)) {
      return refreshAccessToken()
    }
    return token
  }

  const fetchWithAuth = async (input: string, init: RequestInit = {}, retryOnUnauthorized = true) => {
    const token = await getValidAccessToken()
    const headers = new Headers(init.headers)
    headers.set('Authorization', `Bearer ${token}`)

    const response = await fetch(input, { ...init, headers })
    if (response.status === 401 && retryOnUnauthorized) {
      const refreshedToken = await refreshAccessToken()
      const retryHeaders = new Headers(init.headers)
      retryHeaders.set('Authorization', `Bearer ${refreshedToken}`)
      return fetch(input, { ...init, headers: retryHeaders })
    }
    return response
  }

  const monthlySummary = useMemo(() => {
    const now = new Date()
    const m = now.getMonth()
    const y = now.getFullYear()
    const monthTx = transactions.filter((tx) => {
      const d = new Date(tx.createdAt)
      return d.getMonth() === m && d.getFullYear() === y
    })
    const income = monthTx.filter((t) => t.type === 'income').reduce((s, t) => s + t.amount, 0)
    const expense = monthTx.filter((t) => t.type === 'expense').reduce((s, t) => s + t.amount, 0)
    return { income, expense, net: income - expense }
  }, [transactions])

  const barChartData = useMemo(() => {
    const days: { label: string; income: number; expense: number }[] = []
    for (let i = 6; i >= 0; i--) {
      const start = new Date()
      start.setHours(0, 0, 0, 0)
      start.setDate(start.getDate() - i)
      const end = new Date(start)
      end.setDate(end.getDate() + 1)
      const dayTx = transactions.filter(
        (tx) => tx.createdAt >= start.getTime() && tx.createdAt < end.getTime(),
      )
      const income = dayTx.filter((t) => t.type === 'income').reduce((s, t) => s + t.amount, 0)
      const expense = dayTx.filter((t) => t.type === 'expense').reduce((s, t) => s + t.amount, 0)
      const shortDay = start.toLocaleDateString('es-AR', { weekday: 'short' }).replace('.', '')
      days.push({ label: i === 0 ? 'Hoy' : shortDay, income, expense })
    }
    return days
  }, [transactions])

  const donutValues = useMemo(() => {
    const r = 40
    const circ = 2 * Math.PI * r
    const total = monthlySummary.income + monthlySummary.expense
    if (total === 0) {
      return { r, circ, incomeLen: 0, expenseLen: circ, incomeRotate: -90, expenseRotate: -90, incomePct: 0 }
    }
    const incomeLen = (monthlySummary.income / total) * circ
    const expenseLen = circ - incomeLen
    const expenseRotate = -90 + (incomeLen / circ) * 360
    return { r, circ, incomeLen, expenseLen, incomeRotate: -90, expenseRotate, incomePct: Math.round((monthlySummary.income / total) * 100) }
  }, [monthlySummary])

  useEffect(() => {
    window.localStorage.setItem(BALANCE_VISIBILITY_KEY, String(showBalance))
  }, [showBalance])

  useEffect(() => {
    window.localStorage.setItem(SAVINGS_GOALS_KEY, JSON.stringify(savingsGoals))
  }, [savingsGoals])

  useEffect(() => {
    window.localStorage.setItem(TRANSACTIONS_KEY, JSON.stringify(transactions))
  }, [transactions])

  useEffect(() => {
    window.localStorage.setItem(SERVICES_KEY, JSON.stringify(services))
  }, [services])

  useEffect(() => {
    if (typeof window === 'undefined') return
    if (accountId) {
      window.localStorage.setItem(ACCOUNT_ID_KEY, accountId)
    } else {
      window.localStorage.removeItem(ACCOUNT_ID_KEY)
    }
  }, [accountId])

  useEffect(() => {
    if (typeof window === 'undefined') return
    if (userEmail) {
      window.localStorage.setItem(USER_EMAIL_KEY, userEmail)
    } else {
      window.localStorage.removeItem(USER_EMAIL_KEY)
    }
  }, [userEmail])

  useEffect(() => {
    if (typeof window === 'undefined') return

    const storedToken = window.localStorage.getItem(AUTH_TOKEN_KEY)
    const storedAccountId = window.localStorage.getItem(ACCOUNT_ID_KEY)
    const storedUserEmail = window.localStorage.getItem(USER_EMAIL_KEY)

    if (!storedToken || !storedAccountId || !storedUserEmail) {
      return
    }

    let cancelled = false

    const restoreSession = async () => {
      try {
        await getValidAccessToken()
        if (cancelled) return
        setAccountId(storedAccountId)
        setUserEmail(storedUserEmail)
        setScreen('dashboard')
        setSessionNotice('')
      } catch {
        if (cancelled) return
        clearLocalSession()
        setSessionNotice('Tu sesión expiró. Iniciá sesión de nuevo.')
      }
    }

    restoreSession()

    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    if (typeof window === 'undefined') return

    const intervalId = window.setInterval(() => {
      const token = window.localStorage.getItem(AUTH_TOKEN_KEY)
      if (!token || !isTokenExpiringSoon(token, 90_000)) {
        return
      }

      refreshAccessToken().catch(() => {
        clearLocalSession()
        setSessionNotice('Tu sesión expiró. Iniciá sesión de nuevo.')
      })
    }, 30_000)

    return () => {
      window.clearInterval(intervalId)
    }
  }, [])

  useEffect(() => {
    if (screen !== 'home' || typeof window === 'undefined') {
      return
    }

    const revealNodes = Array.from(document.querySelectorAll<HTMLElement>('[data-reveal]'))
    if (revealNodes.length === 0) {
      return
    }

    if (!('IntersectionObserver' in window)) {
      revealNodes.forEach((node) => {
        node.classList.add('is-visible')
      })
      return
    }

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible')
            observer.unobserve(entry.target)
          }
        })
      },
      { threshold: 0.16, rootMargin: '0px 0px -8% 0px' },
    )

    revealNodes.forEach((node, index) => {
      node.style.transitionDelay = `${Math.min(index * 70, 360)}ms`
      observer.observe(node)
    })

    return () => {
      revealNodes.forEach((node) => {
        node.style.transitionDelay = '0ms'
      })
      observer.disconnect()
    }
  }, [screen])

  const getGoalProgress = (saved: number, target: number) =>
    Math.min(100, Math.round((saved / target) * 100))

  const visibleGoals = useMemo(() => {
    let goalsCopy = [...savingsGoals]

    if (goalFilter === 'done') {
      goalsCopy = goalsCopy.filter((g) => getGoalProgress(g.saved, g.target) === 100)
    } else if (goalFilter === 'pending') {
      goalsCopy = goalsCopy.filter((g) => getGoalProgress(g.saved, g.target) < 100)
    }

    if (goalSort === 'progress-desc') {
      goalsCopy.sort((a, b) => getGoalProgress(b.saved, b.target) - getGoalProgress(a.saved, a.target))
      return goalsCopy
    }

    goalsCopy.sort((a, b) => b.createdAt - a.createdAt)
    return goalsCopy
  }, [goalSort, goalFilter, savingsGoals])

  const handleAddGoal = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    const goalName = newGoalName.trim()
    const target = Number(newGoalTarget)
    const saved = Number(newGoalSaved || '0')

    if (!goalName || Number.isNaN(target) || target <= 0 || Number.isNaN(saved) || saved < 0) {
      return
    }

    const normalizedSaved = Math.min(saved, target)

    setSavingsGoals((currentGoals) => [
      {
        id: `${Date.now()}-${goalName.toLowerCase().replace(/\s+/g, '-')}`,
        label: goalName,
        saved: normalizedSaved,
        target,
        createdAt: Date.now(),
      },
      ...currentGoals,
    ])

    setNewGoalName('')
    setNewGoalTarget('')
    setNewGoalSaved('')
  }

  const handleDeleteGoal = (goalId: string) => {
    setSavingsGoals((currentGoals) => currentGoals.filter((goal) => goal.id !== goalId))
  }

  const handleStartGoalEdit = (goal: SavingsGoal) => {
    setEditingGoalId(goal.id)
    setEditingGoalName(goal.label)
    setEditingGoalTarget(String(goal.target))
    setEditingGoalSaved(String(goal.saved))
  }

  const handleCancelGoalEdit = () => {
    setEditingGoalId(null)
    setEditingGoalName('')
    setEditingGoalTarget('')
    setEditingGoalSaved('')
  }

  const handleAddTransaction = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const label = newTxLabel.trim()
    const amount = Number(newTxAmount)
    if (!label || Number.isNaN(amount) || amount <= 0) return
    setTransactions((current) => [
      {
        id: `${Date.now()}-${label.toLowerCase().replace(/\s+/g, '-')}`,
        label,
        amount,
        type: newTxType,
        createdAt: Date.now(),
      },
      ...current,
    ])
    setNewTxLabel('')
    setNewTxAmount('')
    setShowTxForm(false)
  }

  const handleAddService = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const label = newServiceLabel.trim()
    const amount = Number(newServiceAmount)
    if (!label || Number.isNaN(amount) || amount <= 0) return
    setServices((current) => [
      {
        id: `${Date.now()}-${label.toLowerCase().replace(/\s+/g, '-')}`,
        label,
        amount,
        dueDate: newServiceDueDate,
        status: 'pending',
        createdAt: Date.now(),
      },
      ...current,
    ])
    setNewServiceLabel('')
    setNewServiceAmount('')
    setNewServiceDueDate('')
    setShowServiceForm(false)
  }

  const handleToggleService = (serviceId: string) => {
    setServices((current) =>
      current.map((s) =>
        s.id === serviceId ? { ...s, status: s.status === 'pending' ? 'paid' : 'pending' } : s,
      ),
    )
  }

  const handleDeleteService = (serviceId: string) => {
    setServices((current) => current.filter((s) => s.id !== serviceId))
  }

  const handleDeleteTransaction = (txId: string) => {
    setTransactions((current) => current.filter((tx) => tx.id !== txId))
  }

  const handleSaveGoalEdit = (event: React.FormEvent<HTMLFormElement>, goalId: string) => {
    event.preventDefault()

    const label = editingGoalName.trim()
    const target = Number(editingGoalTarget)
    const saved = Number(editingGoalSaved || '0')

    if (!label || Number.isNaN(target) || target <= 0 || Number.isNaN(saved) || saved < 0) {
      return
    }

    const normalizedSaved = Math.min(saved, target)

    setSavingsGoals((currentGoals) =>
      currentGoals.map((goal) =>
        goal.id === goalId
          ? { ...goal, label, target, saved: normalizedSaved }
          : goal,
      ),
    )

    handleCancelGoalEdit()
  }

  const ensureAccountForEmail = async (email: string, fallbackName: string, token: string) => {
    // Primero intenta recuperar la cuenta existente desde el backend
    const meResponse = await fetchWithAuth(`${API_BASE_URL}/api/accounts/me`, {
      headers: { Authorization: `Bearer ${token}` },
    })

    if (meResponse.ok) {
      const meData = (await meResponse.json()) as AccountResponse
      if (meData.id) {
        setAccountId(meData.id)
        return meData.id
      }
    }

    // Si no existe (404), crea una nueva
    const accountResponse = await fetch(`${API_BASE_URL}/api/accounts`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: fallbackName,
        email,
        currency: 'USD',
      }),
    })

    const accountData = (await accountResponse.json()) as {
      id?: string
      message?: string
    }

    if (!accountResponse.ok || !accountData.id) {
      throw new Error(accountData.message || 'No se pudo crear/obtener la cuenta.')
    }

    setAccountId(accountData.id)
    return accountData.id
  }

  const fetchApiTransactions = async (token: string, currentAccountId: string) => {
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/api/accounts/${currentAccountId}/transactions?size=50`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      if (!res.ok) return
      const data = (await res.json()) as ApiTransaction[]
      if (!Array.isArray(data)) return
      setTransactions(
        data.map((tx) => ({
          id: tx.id,
          label: tx.description ?? (tx.type === 'CREDIT' ? 'Crédito' : 'Débito'),
          amount: Math.abs(Number(tx.amount)),
          type: tx.type === 'CREDIT' ? 'income' : 'expense',
          createdAt: new Date(tx.timestamp).getTime(),
        }))
      )
    } catch {
      // silent – transacciones locales se mantienen
    }
  }

  const fetchAccountBalance = async (token: string, currentAccountId: string) => {
    const accountResponse = await fetchWithAuth(`${API_BASE_URL}/api/accounts/${currentAccountId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

    const accountData = (await accountResponse.json()) as AccountResponse & { message?: string }

    if (!accountResponse.ok) {
      throw new Error(accountData.message || 'No se pudo sincronizar la cuenta.')
    }

    setAccountBalance(Number(accountData.balance))
    if (accountData.accountNumber) {
      setAccountNumber(accountData.accountNumber)
      window.localStorage.setItem(ACCOUNT_NUMBER_KEY, accountData.accountNumber)
    }
    if (accountData.alias) {
      setAccountAlias(accountData.alias)
      window.localStorage.setItem(ACCOUNT_ALIAS_KEY, accountData.alias)
    }
    setAccountSyncError('')
  }

  const handleLogout = async () => {
    const refreshToken = window.localStorage.getItem(REFRESH_TOKEN_KEY)
    if (refreshToken) {
      try {
        await fetch(`${API_BASE_URL}/api/auth/logout`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken }),
        })
      } catch {
        // continuar con el logout local aunque falle la llamada
      }
    }
    clearLocalSession()
    setSessionNotice('')
  }

  const handleAuthSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const name = authName.trim()
    const email = authEmail.trim().toLowerCase()

    if (authMode === 'register' && name.length < 2) {
      setAuthError('Ingresá un nombre válido.')
      return
    }

    if (!email.includes('@') || email.length < 5) {
      setAuthError('Ingresá un email válido.')
      return
    }

    if (authPassword.length < 6) {
      setAuthError('La contraseña debe tener al menos 6 caracteres.')
      return
    }

    if (authMode === 'register' && authPassword !== authConfirmPassword) {
      setAuthError('Las contraseñas no coinciden.')
      return
    }

    setAuthError('')
    setAuthLoading(true)

    try {
      if (authMode === 'register') {
        const registerResponse = await fetch(`${API_BASE_URL}/api/auth/register`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email, password: authPassword }),
        })

        const registerData = (await registerResponse.json()) as {
          accessToken?: string
          refreshToken?: string
          message?: string
        }

        if (!registerResponse.ok || !registerData.accessToken) {
          throw new Error(registerData.message || 'No se pudo registrar el usuario.')
        }

        storeAuthTokens({
          accessToken: registerData.accessToken,
          tokenType: 'Bearer',
          expiresInSeconds: 0,
          refreshToken: registerData.refreshToken,
        })
        const createdAccountId = await ensureAccountForEmail(email, name || 'Usuario Wallet', registerData.accessToken)
        await fetchAccountBalance(registerData.accessToken, createdAccountId)
        await fetchApiTransactions(registerData.accessToken, createdAccountId)
      } else {
        const loginResponse = await fetch(`${API_BASE_URL}/api/auth/login`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email, password: authPassword }),
        })

        const loginData = (await loginResponse.json()) as {
          accessToken?: string
          refreshToken?: string
          message?: string
        }

        if (!loginResponse.ok || !loginData.accessToken) {
          throw new Error(loginData.message || 'Credenciales inválidas.')
        }

        storeAuthTokens({
          accessToken: loginData.accessToken,
          tokenType: 'Bearer',
          expiresInSeconds: 0,
          refreshToken: loginData.refreshToken,
        })
        const currentAccountId = await ensureAccountForEmail(email, name || email.split('@')[0] || 'Usuario Wallet', loginData.accessToken)
        await fetchAccountBalance(loginData.accessToken, currentAccountId)
        await fetchApiTransactions(loginData.accessToken, currentAccountId)
      }

      setUserEmail(email)
      setScreen('dashboard')
      setSessionNotice('')
      setAuthPassword('')
      setAuthConfirmPassword('')
    } catch (error) {
      const message = error instanceof Error ? error.message : 'No se pudo conectar con el backend.'
      setAuthError(message)
    } finally {
      setAuthLoading(false)
    }
  }

  const handleDeposit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const amount = Number(depositAmount)
    if (!accountId || Number.isNaN(amount) || amount <= 0) return
    setDepositLoading(true)
    setDepositError('')
    try {
      const token = await getValidAccessToken()
      const response = await fetchWithAuth(`${API_BASE_URL}/api/accounts/${accountId}/deposit`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({ amount }),
      })
      const data = (await response.json()) as AccountResponse & { message?: string }
      if (!response.ok) throw new Error(data.message || 'No se pudo procesar el depósito.')
      setAccountBalance(Number(data.balance))
      setDepositAmount('')
      setShowDepositModal(false)
    } catch (error) {
      setDepositError(error instanceof Error ? error.message : 'Error al depositar.')
    } finally {
      setDepositLoading(false)
    }
  }

  const handleTransfer = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const amount = Number(transferAmount)
    if (!accountId || !transferDestId.trim() || Number.isNaN(amount) || amount <= 0) return
    setTransferLoading(true)
    setTransferError('')
    try {
      const token = await getValidAccessToken()
      const response = await fetchWithAuth(`${API_BASE_URL}/api/accounts/${accountId}/transfer-by-email`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({ emailOrAlias: transferDestId.trim(), amount }),
      })
      const data = (await response.json()) as AccountResponse & { message?: string }
      if (!response.ok) throw new Error(data.message || 'No se pudo procesar la transferencia.')
      setAccountBalance(Number(data.balance))
      await fetchApiTransactions(token, accountId)
      setTransferAmount('')
      setTransferDestId('')
      setShowTransferModal(false)
    } catch (error) {
      setTransferError(error instanceof Error ? error.message : 'Error al transferir.')
    } finally {
      setTransferLoading(false)
    }
  }

  const handleForgotPassword = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const email = forgotEmail.trim().toLowerCase()
    if (!email.includes('@')) { setForgotError('Ingresá un email válido.'); return }
    setForgotLoading(true)
    setForgotError('')
    setForgotSuccess('')
    try {
      const res = await fetch(`${API_BASE_URL}/api/auth/forgot-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email }),
      })
      const data = (await res.json()) as { message?: string; resetToken?: string }
      if (!res.ok) throw new Error(data.message || 'No se pudo enviar el correo.')
      setForgotSuccess('Revisá tu correo. Si la cuenta existe, recibirás el enlace de recuperación.')
      if (data.resetToken) {
        setResetToken(data.resetToken)
        setAuthMode('reset-password')
      }
    } catch (error) {
      setForgotError(error instanceof Error ? error.message : 'Error al solicitar recuperación.')
    } finally {
      setForgotLoading(false)
    }
  }

  const handleResetPassword = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (resetNewPassword.length < 8) { setResetError('La contraseña debe tener al menos 8 caracteres.'); return }
    if (resetNewPassword !== resetConfirm) { setResetError('Las contraseñas no coinciden.'); return }
    setResetLoading(true)
    setResetError('')
    setResetSuccess('')
    try {
      const res = await fetch(`${API_BASE_URL}/api/auth/reset-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token: resetToken, newPassword: resetNewPassword }),
      })
      const data = (await res.json()) as { message?: string }
      if (!res.ok) throw new Error(data.message || 'No se pudo restablecer la contraseña.')
      setResetSuccess('¡Contraseña actualizada! Ya podés iniciar sesión.')
      setTimeout(() => {
        setAuthMode('login')
        setResetToken('')
        setResetNewPassword('')
        setResetConfirm('')
        setResetSuccess('')
      }, 2500)
    } catch (error) {
      setResetError(error instanceof Error ? error.message : 'Error al restablecer contraseña.')
    } finally {
      setResetLoading(false)
    }
  }

  useEffect(() => {
    const syncAccountOnDashboard = async () => {
      if (screen !== 'dashboard' || !accountId || typeof window === 'undefined') {
        return
      }

      const token = window.localStorage.getItem(AUTH_TOKEN_KEY)
      if (!token) {
        return
      }

      try {
        await fetchAccountBalance(token, accountId)
        await fetchApiTransactions(token, accountId)
      } catch (error) {
        const message = error instanceof Error ? error.message : 'No se pudo sincronizar la cuenta.'
        setAccountSyncError(message)
      }
    }

    syncAccountOnDashboard()
  }, [screen, accountId])

  const renderDashboard = () => (
    <main className="relative min-h-screen overflow-hidden bg-slate-100 px-3 py-8 text-slate-900 min-[320px]:px-4 sm:px-6 lg:px-12">
      <div className="pointer-events-none absolute inset-0">
        <div className="absolute -top-16 left-1/2 h-72 w-72 -translate-x-1/2 rounded-full bg-cyan-100/60 blur-3xl" />
        <div className="absolute bottom-12 left-10 h-40 w-40 rounded-full bg-emerald-100/50 blur-2xl" />
        <div className="absolute right-10 top-20 h-32 w-32 rounded-full bg-violet-100/40 blur-2xl" />
      </div>

      <section className="mx-auto flex min-h-[calc(100vh-4rem)] w-full min-w-0 max-w-[420px] items-center lg:max-w-5xl lg:items-start lg:pt-16">
        <article className="relative w-full min-w-0 animate-[card-in_450ms_ease-out_both] rounded-3xl border border-white/70 bg-white p-4 shadow-[0_16px_45px_rgba(15,23,42,0.11)] min-[360px]:p-6 sm:p-7 lg:p-10">
          <header className="flex items-center justify-between">
            <div>
              <h1 className="text-lg font-semibold tracking-tight text-slate-700">{userEmail ? userEmail.split('@')[0] : 'Mi billetera'}</h1>
              {accountNumber && (
                <p className="mt-0.5 text-[11px] text-slate-400">
                  CVU: <span className="select-all font-mono text-slate-500">{accountNumber}</span>
                </p>
              )}
              {accountAlias && (
                <p className="text-[11px] text-slate-400">
                  Alias: <span className="select-all font-semibold text-slate-600">{accountAlias}</span>
                </p>
              )}
            </div>
            <div className="flex items-center gap-2">
              <span className="rounded-full bg-slate-100 px-2.5 py-1 text-[11px] font-semibold tracking-wide text-slate-500">
                Wallet
              </span>
              <button
                type="button"
                onClick={handleLogout}
                className="rounded-lg border border-slate-200 px-2.5 py-1 text-[11px] font-semibold text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-700"
              >
                Salir
              </button>
            </div>
          </header>

          <section className="mt-7 lg:flex lg:items-end lg:justify-between">
            <div>
            <div className="flex items-center justify-between gap-6">
              <p className="text-sm font-medium text-slate-500">Podés gastar</p>
              <button
                type="button"
                onClick={() => setShowBalance((current) => !current)}
                className="rounded-lg px-2 py-1 text-xs font-semibold text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-300"
              >
                {showBalance ? 'Ocultar saldo' : 'Mostrar saldo'}
              </button>
            </div>
            <p className="mt-1 text-[2.35rem] leading-none font-extrabold tracking-tight text-slate-900">
              {showBalance ? formattedBalance : '••••••'}
            </p>
            <p className="mt-2 inline-flex items-center gap-2 text-sm font-semibold text-emerald-600">
              <span className="inline-block h-1.5 w-1.5 rounded-full bg-emerald-500" />
              Todo bajo control
            </p>
            {accountSyncError && (
              <p className="mt-2 text-xs font-semibold text-amber-600">{accountSyncError}</p>
            )}
            </div>
            <section className="mt-6 grid grid-cols-3 gap-2.5 lg:mt-0 lg:w-56 lg:shrink-0">
              <button
                type="button"
                onClick={() => { setShowTransferModal(true); setTransferError('') }}
                className="h-10 rounded-xl border border-slate-200/70 bg-slate-100/90 text-sm font-semibold text-slate-700 transition-all duration-200 hover:-translate-y-0.5 hover:bg-slate-200/80 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-300"
              >
                Enviar
              </button>
              <button
                type="button"
                onClick={() => { setShowDepositModal(true); setDepositError('') }}
                className="h-10 rounded-xl border border-slate-200/70 bg-slate-100/90 text-sm font-semibold text-slate-700 transition-all duration-200 hover:-translate-y-0.5 hover:bg-slate-200/80 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-300"
              >
                Agregar
              </button>
              <button
                type="button"
                disabled
                className="h-10 rounded-xl border border-slate-200/70 bg-slate-100/90 text-sm font-semibold text-slate-400 cursor-not-allowed"
              >
                IA
              </button>
            </section>
          </section>

          {/* ── Analytics ─────────────────────────────────────────── */}
          <div className="mt-6 grid grid-cols-1 gap-3 border-t border-slate-100 pt-5 lg:grid-cols-2">
            {/* Donut – distribución del mes */}
            <div className="rounded-2xl bg-slate-50 px-4 py-4">
              <p className="text-[10px] font-semibold uppercase tracking-wide text-slate-400">Distribución del mes</p>
              <div className="mt-3 flex flex-col items-center gap-4 min-[380px]:flex-row min-[380px]:items-center">
                <svg viewBox="0 0 120 120" className="h-24 w-24 shrink-0">
                  {/* track */}
                  <circle cx="60" cy="60" r={donutValues.r} fill="none" stroke="#e2e8f0" strokeWidth="13" />
                  {/* ingresos arc */}
                  {donutValues.incomeLen > 0 && (
                    <circle
                      cx="60" cy="60" r={donutValues.r}
                      fill="none" stroke="#10b981" strokeWidth="13" strokeLinecap="round"
                      strokeDasharray={`${donutValues.incomeLen} ${donutValues.circ - donutValues.incomeLen}`}
                      transform={`rotate(${donutValues.incomeRotate} 60 60)`}
                    />
                  )}
                  {/* gastos arc */}
                  {donutValues.expenseLen > 0 && donutValues.incomeLen > 0 && (
                    <circle
                      cx="60" cy="60" r={donutValues.r}
                      fill="none" stroke="#f43f5e" strokeWidth="13" strokeLinecap="round"
                      strokeDasharray={`${donutValues.expenseLen} ${donutValues.circ - donutValues.expenseLen}`}
                      transform={`rotate(${donutValues.expenseRotate} 60 60)`}
                    />
                  )}
                  {/* center label */}
                  <text x="60" y="57" textAnchor="middle" fontSize="15" fontWeight="700" fill="#0f172a">
                    {donutValues.incomePct}%
                  </text>
                  <text x="60" y="70" textAnchor="middle" fontSize="8.5" fill="#94a3b8">
                    ingresos
                  </text>
                </svg>
                <div className="w-full space-y-2 text-xs min-[380px]:w-auto">
                  <div className="flex items-center gap-2">
                    <span className="h-2.5 w-2.5 shrink-0 rounded-full bg-emerald-500" />
                    <span className="text-slate-600">Ingresos <span className="font-semibold">${monthlySummary.income.toLocaleString('es-AR')}</span></span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="h-2.5 w-2.5 shrink-0 rounded-full bg-rose-500" />
                    <span className="text-slate-600">Gastos <span className="font-semibold">${monthlySummary.expense.toLocaleString('es-AR')}</span></span>
                  </div>
                  <div className="border-t border-slate-200 pt-2">
                    <span className={`font-bold ${monthlySummary.net >= 0 ? 'text-emerald-600' : 'text-rose-500'}`}>
                      Neto {monthlySummary.net >= 0 ? '+' : ''}${Math.abs(monthlySummary.net).toLocaleString('es-AR')}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Bar chart – últimos 7 días */}
            <div className="rounded-2xl bg-slate-50 px-4 py-4">
              <p className="text-[10px] font-semibold uppercase tracking-wide text-slate-400">Últimos 7 días</p>
              <div className="mt-3">
                {(() => {
                  const maxVal = Math.max(...barChartData.flatMap((d) => [d.income, d.expense]), 1)
                  const chartH = 50
                  const barW = 7
                  const groupW = 30
                  const svgW = barChartData.length * groupW
                  const svgH = chartH + 18
                  return (
                    <svg viewBox={`0 0 ${svgW} ${svgH}`} className="w-full">
                      {barChartData.map((day, i) => {
                        const incH = Math.max(Math.round((day.income / maxVal) * chartH), day.income > 0 ? 2 : 0)
                        const expH = Math.max(Math.round((day.expense / maxVal) * chartH), day.expense > 0 ? 2 : 0)
                        const x = i * groupW + groupW / 2 - barW - 1
                        return (
                          <g key={i}>
                            {incH > 0 && (
                              <rect x={x} y={chartH - incH} width={barW} height={incH} rx="2" fill="#10b981" opacity="0.85" />
                            )}
                            {expH > 0 && (
                              <rect x={x + barW + 2} y={chartH - expH} width={barW} height={expH} rx="2" fill="#f43f5e" opacity="0.75" />
                            )}
                            <text x={i * groupW + groupW / 2} y={svgH - 2} textAnchor="middle" fontSize="7" fill="#94a3b8">
                              {day.label}
                            </text>
                          </g>
                        )
                      })}
                    </svg>
                  )
                })()}
                <div className="mt-1.5 flex gap-3">
                  <span className="flex items-center gap-1 text-[10px] text-slate-400">
                    <span className="inline-block h-2 w-2 rounded-sm bg-emerald-500" />Ingresos
                  </span>
                  <span className="flex items-center gap-1 text-[10px] text-slate-400">
                    <span className="inline-block h-2 w-2 rounded-sm bg-rose-500/75" />Gastos
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div className="mt-8 border-t border-slate-100 pt-6 lg:grid lg:grid-cols-2 lg:gap-0">
          <section className="lg:pr-10">
            <div className="mb-5 grid grid-cols-3 gap-2">
              <div className="rounded-xl bg-emerald-50 px-3 py-2.5 text-center">
                <p className="text-[10px] font-semibold uppercase tracking-wide text-emerald-600">Ingresos</p>
                <p className="mt-0.5 text-sm font-bold text-emerald-700">${monthlySummary.income.toLocaleString('es-AR')}</p>
              </div>
              <div className="rounded-xl bg-rose-50 px-3 py-2.5 text-center">
                <p className="text-[10px] font-semibold uppercase tracking-wide text-rose-500">Gastos</p>
                <p className="mt-0.5 text-sm font-bold text-rose-600">${monthlySummary.expense.toLocaleString('es-AR')}</p>
              </div>
              <div className="rounded-xl bg-slate-50 px-3 py-2.5 text-center">
                <p className="text-[10px] font-semibold uppercase tracking-wide text-slate-500">Neto</p>
                <p className={`mt-0.5 text-sm font-bold ${monthlySummary.net >= 0 ? 'text-emerald-700' : 'text-rose-600'}`}>
                  {monthlySummary.net >= 0 ? '+' : ''}${Math.abs(monthlySummary.net).toLocaleString('es-AR')}
                </p>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold tracking-wide text-slate-500">Reciente</h2>
              <button
                type="button"
                onClick={() => setShowTxForm((v) => !v)}
                className="rounded-lg px-2 py-1 text-xs font-semibold text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-700"
              >
                {showTxForm ? 'Cancelar' : '+ Nueva'}
              </button>
            </div>

            {showTxForm && (
              <form className="mt-3 space-y-2" onSubmit={handleAddTransaction}>
                <input
                  type="text"
                  value={newTxLabel}
                  onChange={(e) => setNewTxLabel(e.target.value)}
                  placeholder="Descripción"
                  className="h-10 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-300 focus:outline-none"
                />
                <div className="flex gap-2">
                  <input
                    type="number"
                    min="1"
                    step="1"
                    value={newTxAmount}
                    onChange={(e) => setNewTxAmount(e.target.value)}
                    placeholder="Monto"
                    className="h-10 flex-1 rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-300 focus:outline-none"
                  />
                  <select
                    value={newTxType}
                    onChange={(e) => setNewTxType(e.target.value as TxType)}
                    className="h-10 rounded-xl border border-slate-200 bg-white px-2 text-sm font-semibold text-slate-600 focus:border-slate-300 focus:outline-none"
                  >
                    <option value="expense">Gasto</option>
                    <option value="income">Ingreso</option>
                  </select>
                </div>
                <button
                  type="submit"
                  className="h-10 w-full rounded-xl bg-slate-900 text-sm font-semibold text-white transition-colors hover:bg-slate-700"
                >
                  Agregar
                </button>
              </form>
            )}

            <ul className="mt-3 space-y-3">
              {transactions.map((tx, index) => (
                <li
                  key={tx.id}
                  className="flex animate-[item-in_480ms_ease-out_both] items-center justify-between rounded-xl border border-slate-100 bg-slate-50/80 px-3 py-2.5"
                  style={{ animationDelay: `${index * 70}ms` }}
                >
                  <div>
                    <p className="text-sm font-medium text-slate-700">{tx.label}</p>
                    <p className="text-[11px] text-slate-400">{formatDate(tx.createdAt)}</p>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <span className={`text-sm font-semibold ${tx.type === 'income' ? 'text-emerald-600' : 'text-rose-500'}`}>
                      {tx.type === 'income' ? '+' : '-'}${tx.amount.toLocaleString('es-AR')}
                    </span>
                    <button
                      type="button"
                      onClick={() => handleDeleteTransaction(tx.id)}
                      className="rounded-md p-1 text-[10px] text-slate-300 transition-colors hover:bg-slate-200 hover:text-slate-500"
                    >
                      ✕
                    </button>
                  </div>
                </li>
              ))}
              {transactions.length === 0 && (
                <p className="mt-4 text-center text-xs text-slate-400">Sin transacciones aún.</p>
              )}
            </ul>
          </section>

          <section className="mt-8 border-t border-slate-100 pt-5 lg:mt-0 lg:border-t-0 lg:border-l lg:border-slate-100 lg:pl-10 lg:pt-0">
            <div className="flex items-center justify-between gap-2">
              <h2 className="text-sm font-semibold tracking-wide text-slate-500">Objetivos</h2>
              <select
                value={goalSort}
                onChange={(event) => setGoalSort(event.target.value as GoalSort)}
                className="h-8 rounded-lg border border-slate-200 bg-white px-2 text-xs font-semibold text-slate-600 focus:border-slate-300 focus:outline-none"
              >
                <option value="newest">Mas nuevos</option>
                <option value="progress-desc">Mayor progreso</option>
              </select>
            </div>

            <div className="mt-3 flex gap-1 rounded-xl bg-slate-100 p-1">
              {(['all', 'pending', 'done'] as const).map((filter) => (
                <button
                  key={filter}
                  type="button"
                  onClick={() => setGoalFilter(filter)}
                  className={`flex-1 rounded-lg py-1.5 text-xs font-semibold transition-colors ${
                    goalFilter === filter
                      ? 'bg-white text-slate-800 shadow-sm'
                      : 'text-slate-500 hover:text-slate-700'
                  }`}
                >
                  {filter === 'all' ? 'Todos' : filter === 'pending' ? 'Pendientes' : 'Completos'}
                </button>
              ))}
            </div>

            {visibleGoals.length === 0 && (
              <p className="mt-4 text-center text-xs text-slate-400">
                {goalFilter === 'done' ? 'Ningún objetivo completado aún.' : 'No hay objetivos pendientes.'}
              </p>
            )}

            <form className="mt-3 grid grid-cols-2 gap-2" onSubmit={handleAddGoal}>
              <input
                type="text"
                value={newGoalName}
                onChange={(event) => setNewGoalName(event.target.value)}
                placeholder="Nombre"
                className="col-span-2 h-10 rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-300 focus:outline-none"
              />
              <input
                type="number"
                min="1"
                step="100"
                value={newGoalTarget}
                onChange={(event) => setNewGoalTarget(event.target.value)}
                placeholder="Meta"
                className="h-10 rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-300 focus:outline-none"
              />
              <input
                type="number"
                min="0"
                step="100"
                value={newGoalSaved}
                onChange={(event) => setNewGoalSaved(event.target.value)}
                placeholder="Ahorrado"
                className="h-10 rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-300 focus:outline-none"
              />
              <button
                type="submit"
                className="col-span-2 h-10 rounded-xl bg-slate-900 text-sm font-semibold text-white transition-colors hover:bg-slate-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-300"
              >
                Agregar objetivo
              </button>
            </form>

            <ul className="mt-3 space-y-3">
              {visibleGoals.map((goal) => {
                const progress = getGoalProgress(goal.saved, goal.target)
                const isEditing = editingGoalId === goal.id

                return (
                  <li key={goal.id} className="rounded-xl border border-slate-100 bg-slate-50/85 px-3 py-3">
                    {isEditing ? (
                      <form className="space-y-2" onSubmit={(event) => handleSaveGoalEdit(event, goal.id)}>
                        <input
                          type="text"
                          value={editingGoalName}
                          onChange={(event) => setEditingGoalName(event.target.value)}
                          className="h-9 w-full rounded-lg border border-slate-200 bg-white px-3 text-sm text-slate-700 focus:border-slate-300 focus:outline-none"
                        />
                        <div className="grid grid-cols-2 gap-2">
                          <input
                            type="number"
                            min="1"
                            step="100"
                            value={editingGoalTarget}
                            onChange={(event) => setEditingGoalTarget(event.target.value)}
                            className="h-9 rounded-lg border border-slate-200 bg-white px-3 text-sm text-slate-700 focus:border-slate-300 focus:outline-none"
                          />
                          <input
                            type="number"
                            min="0"
                            step="100"
                            value={editingGoalSaved}
                            onChange={(event) => setEditingGoalSaved(event.target.value)}
                            className="h-9 rounded-lg border border-slate-200 bg-white px-3 text-sm text-slate-700 focus:border-slate-300 focus:outline-none"
                          />
                        </div>
                        <div className="flex items-center justify-end gap-2">
                          <button
                            type="button"
                            onClick={handleCancelGoalEdit}
                            className="rounded-md px-2.5 py-1.5 text-xs font-semibold text-slate-500 transition-colors hover:bg-slate-200"
                          >
                            Cancelar
                          </button>
                          <button
                            type="submit"
                            className="rounded-md bg-slate-900 px-2.5 py-1.5 text-xs font-semibold text-white transition-colors hover:bg-slate-700"
                          >
                            Guardar
                          </button>
                        </div>
                      </form>
                    ) : (
                      <>
                        <div className="flex items-center justify-between text-sm">
                          <span className="font-medium text-slate-700">{goal.label}</span>
                          <div className="flex items-center gap-2">
                            <span className="font-semibold text-slate-500">{progress}%</span>
                            <button
                              type="button"
                              onClick={() => handleStartGoalEdit(goal)}
                              className="rounded-md px-2 py-1 text-xs font-semibold text-slate-400 transition-colors hover:bg-slate-200 hover:text-slate-600"
                            >
                              Editar
                            </button>
                            <button
                              type="button"
                              onClick={() => handleDeleteGoal(goal.id)}
                              className="rounded-md px-2 py-1 text-xs font-semibold text-slate-400 transition-colors hover:bg-slate-200 hover:text-slate-600"
                            >
                              Quitar
                            </button>
                          </div>
                        </div>
                        <div className="mt-2 h-2 rounded-full bg-slate-200">
                          <div
                            className="h-2 rounded-full bg-emerald-500 transition-[width] duration-500"
                            style={{ width: `${progress}%` }}
                          />
                        </div>
                        <p className="mt-2 text-xs text-slate-500">
                          ${goal.saved.toLocaleString('es-AR')} de ${goal.target.toLocaleString('es-AR')}
                        </p>
                      </>
                    )}
                  </li>
                )
              })}
            </ul>
          </section>
          </div>

          {/* ── Servicios a pagar ──────────────────────────────── */}
          <section className="mt-8 border-t border-slate-100 pt-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-sm font-semibold tracking-wide text-slate-500">Servicios a pagar</h2>
                {services.filter((s) => s.status === 'pending').length > 0 && (
                  <p className="mt-0.5 text-xs text-slate-400">
                    Total pendiente:{' '}
                    <span className="font-semibold text-rose-500">
                      ${services.filter((s) => s.status === 'pending').reduce((sum, s) => sum + s.amount, 0).toLocaleString('es-AR')}
                    </span>
                  </p>
                )}
              </div>
              <button
                type="button"
                onClick={() => setShowServiceForm((v) => !v)}
                className="rounded-lg px-2 py-1 text-xs font-semibold text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-700"
              >
                {showServiceForm ? 'Cancelar' : '+ Nuevo'}
              </button>
            </div>

            {showServiceForm && (
              <form className="mt-3 grid grid-cols-2 gap-2 sm:grid-cols-4" onSubmit={handleAddService}>
                <input
                  type="text"
                  value={newServiceLabel}
                  onChange={(e) => setNewServiceLabel(e.target.value)}
                  placeholder="Nombre del servicio"
                  className="col-span-2 h-10 rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-300 focus:outline-none sm:col-span-1"
                />
                <input
                  type="number"
                  min="1"
                  step="1"
                  value={newServiceAmount}
                  onChange={(e) => setNewServiceAmount(e.target.value)}
                  placeholder="Monto"
                  className="h-10 rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-300 focus:outline-none"
                />
                <input
                  type="date"
                  value={newServiceDueDate}
                  onChange={(e) => setNewServiceDueDate(e.target.value)}
                  className="h-10 rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-600 focus:border-slate-300 focus:outline-none"
                />
                <button
                  type="submit"
                  className="col-span-2 h-10 rounded-xl bg-slate-900 text-sm font-semibold text-white transition-colors hover:bg-slate-700 sm:col-span-1"
                >
                  Agregar
                </button>
              </form>
            )}

            {services.length === 0 && !showServiceForm && (
              <p className="mt-4 text-center text-xs text-slate-400">Sin servicios cargados.</p>
            )}

            <ul className="mt-3 grid grid-cols-1 gap-2 sm:grid-cols-2 lg:grid-cols-3">
              {services.map((service) => {
                const isOverdue =
                  service.status === 'pending' &&
                  service.dueDate &&
                  new Date(service.dueDate + 'T23:59:59') < new Date()
                const isDueSoon =
                  service.status === 'pending' &&
                  service.dueDate &&
                  !isOverdue &&
                  (new Date(service.dueDate + 'T23:59:59').getTime() - Date.now()) / 86400000 <= 3
                return (
                  <li
                    key={service.id}
                    className={`flex items-center justify-between rounded-xl border px-3 py-2.5 transition-colors ${
                      service.status === 'paid'
                        ? 'border-emerald-100 bg-emerald-50/60'
                        : isOverdue
                          ? 'border-rose-200 bg-rose-50/60'
                          : isDueSoon
                            ? 'border-amber-200 bg-amber-50/60'
                            : 'border-slate-100 bg-slate-50/80'
                    }`}
                  >
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <p
                          className={`truncate text-sm font-medium ${
                            service.status === 'paid' ? 'text-slate-400 line-through' : 'text-slate-700'
                          }`}
                        >
                          {service.label}
                        </p>
                        {service.status === 'paid' && (
                          <span className="shrink-0 rounded-full bg-emerald-100 px-1.5 py-0.5 text-[10px] font-semibold text-emerald-600">
                            Pagado
                          </span>
                        )}
                        {isOverdue && (
                          <span className="shrink-0 rounded-full bg-rose-100 px-1.5 py-0.5 text-[10px] font-semibold text-rose-600">
                            Vencido
                          </span>
                        )}
                        {isDueSoon && (
                          <span className="shrink-0 rounded-full bg-amber-100 px-1.5 py-0.5 text-[10px] font-semibold text-amber-600">
                            Próximo
                          </span>
                        )}
                      </div>
                      <div className="mt-0.5 flex items-center gap-2 text-[11px] text-slate-400">
                        <span className="font-semibold text-slate-500">${service.amount.toLocaleString('es-AR')}</span>
                        {service.dueDate && (
                          <span>
                            · Vence{' '}
                            {new Date(service.dueDate + 'T12:00:00').toLocaleDateString('es-AR', {
                              day: 'numeric',
                              month: 'short',
                            })}
                          </span>
                        )}
                      </div>
                    </div>
                    <div className="ml-2 flex shrink-0 items-center gap-1">
                      <button
                        type="button"
                        onClick={() => handleToggleService(service.id)}
                        title={service.status === 'paid' ? 'Marcar como pendiente' : 'Marcar como pagado'}
                        className={`rounded-md px-2 py-1 text-xs font-semibold transition-colors ${
                          service.status === 'paid'
                            ? 'text-slate-400 hover:bg-slate-200 hover:text-slate-600'
                            : 'text-emerald-600 hover:bg-emerald-100'
                        }`}
                      >
                        {service.status === 'paid' ? 'Deshacer' : '✓'}
                      </button>
                      <button
                        type="button"
                        onClick={() => handleDeleteService(service.id)}
                        className="rounded-md p-1 text-[10px] text-slate-300 transition-colors hover:bg-slate-200 hover:text-slate-500"
                      >
                        ✕
                      </button>
                    </div>
                  </li>
                )
              })}
            </ul>
          </section>

          {/* ── Modal Depositar ──────────────────────────────────── */}
          {showDepositModal && (
            <div
              className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
              onClick={() => setShowDepositModal(false)}
            >
              <div
                className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-2xl"
                onClick={(e) => e.stopPropagation()}
              >
                <h2 className="text-base font-bold text-slate-800">Depositar fondos</h2>
                <p className="mt-1 text-xs text-slate-500">El monto se acreditará de inmediato a tu cuenta.</p>
                <form className="mt-4 space-y-3" onSubmit={handleDeposit}>
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={depositAmount}
                    onChange={(e) => setDepositAmount(e.target.value)}
                    placeholder="Monto"
                    autoFocus
                    className="h-11 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-400 focus:outline-none"
                  />
                  {depositError && <p className="text-xs font-semibold text-rose-600">{depositError}</p>}
                  <div className="flex gap-2">
                    <button
                      type="button"
                      onClick={() => setShowDepositModal(false)}
                      className="h-10 flex-1 rounded-xl border border-slate-200 text-sm font-semibold text-slate-600 transition-colors hover:bg-slate-100"
                    >
                      Cancelar
                    </button>
                    <button
                      type="submit"
                      disabled={depositLoading}
                      className="h-10 flex-1 rounded-xl bg-emerald-500 text-sm font-semibold text-white transition-colors hover:bg-emerald-600 disabled:opacity-60"
                    >
                      {depositLoading ? 'Procesando…' : 'Depositar'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}

          {/* ── Modal Transferir ─────────────────────────────────── */}
          {showTransferModal && (
            <div
              className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
              onClick={() => setShowTransferModal(false)}
            >
              <div
                className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-2xl"
                onClick={(e) => e.stopPropagation()}
              >
                <h2 className="text-base font-bold text-slate-800">Enviar dinero</h2>
                {accountAlias && (
                  <p className="mt-1 text-[11px] text-slate-400">
                    Tu alias: <span className="select-all font-semibold text-slate-600">{accountAlias}</span>
                  </p>
                )}
                <form className="mt-4 space-y-3" onSubmit={handleTransfer}>
                  <input
                    type="text"
                    value={transferDestId}
                    onChange={(e) => setTransferDestId(e.target.value)}
                    placeholder="Email, alias o CVU destino"
                    autoFocus
                    className="h-11 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-400 focus:outline-none"
                  />
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={transferAmount}
                    onChange={(e) => setTransferAmount(e.target.value)}
                    placeholder="Monto"
                    className="h-11 w-full rounded-xl border border-slate-200 bg-slate-50 px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-400 focus:outline-none"
                  />
                  {transferError && <p className="text-xs font-semibold text-rose-600">{transferError}</p>}
                  <div className="flex gap-2">
                    <button
                      type="button"
                      onClick={() => setShowTransferModal(false)}
                      className="h-10 flex-1 rounded-xl border border-slate-200 text-sm font-semibold text-slate-600 transition-colors hover:bg-slate-100"
                    >
                      Cancelar
                    </button>
                    <button
                      type="submit"
                      disabled={transferLoading}
                      className="h-10 flex-1 rounded-xl bg-cyan-500 text-sm font-semibold text-white transition-colors hover:bg-cyan-600 disabled:opacity-60"
                    >
                      {transferLoading ? 'Procesando…' : 'Enviar'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}
        </article>
      </section>
    </main>
  )

  if (screen === 'home') {
    return (
      <main className="relative min-h-screen overflow-hidden bg-slate-950 px-4 py-8 text-white min-[320px]:px-5 sm:px-7 lg:px-12">
        <div className="pointer-events-none absolute inset-0">
          <div className="absolute -top-24 left-1/2 h-80 w-80 -translate-x-1/2 rounded-full bg-cyan-400/20 blur-3xl" />
          <div className="absolute bottom-8 right-0 h-72 w-72 rounded-full bg-emerald-300/10 blur-3xl" />
        </div>

        <section className="relative mx-auto w-full max-w-5xl">
          <article className="reveal grid min-h-[calc(100vh-5.5rem)] w-full items-center gap-8 rounded-3xl border border-white/10 bg-white/5 p-5 shadow-[0_20px_70px_rgba(0,0,0,0.45)] backdrop-blur-xl sm:p-7 lg:grid-cols-2 lg:gap-12 lg:p-10" data-reveal>
            <div className="max-w-xl">
              <span className="inline-flex rounded-full border border-white/20 bg-white/10 px-3 py-1 text-[11px] font-semibold tracking-[0.12em] text-cyan-200 uppercase">
                Wallet Digital Oficial
              </span>
              <h1 className="mt-5 text-3xl font-extrabold leading-tight tracking-tight text-white sm:text-4xl lg:text-5xl">
                Tu dinero en un solo lugar, sin vueltas.
              </h1>
              <p className="mt-4 max-w-xl text-sm leading-relaxed text-slate-200 sm:text-base">
                Gestioná gastos, ingresos y objetivos con una experiencia pensada para personas reales.
                Visualizá todo en segundos y tomá mejores decisiones financieras.
              </p>

              <div className="mt-6 grid grid-cols-3 gap-2 sm:max-w-md">
                <div className="rounded-xl border border-white/15 bg-white/10 px-2 py-2.5 text-center">
                  <p className="text-[10px] font-semibold uppercase tracking-wide text-cyan-100">Seguridad</p>
                  <p className="mt-1 text-xs font-bold">Cifrado E2E</p>
                </div>
                <div className="rounded-xl border border-white/15 bg-white/10 px-2 py-2.5 text-center">
                  <p className="text-[10px] font-semibold uppercase tracking-wide text-cyan-100">Velocidad</p>
                  <p className="mt-1 text-xs font-bold">Respuesta instantánea</p>
                </div>
                <div className="rounded-xl border border-white/15 bg-white/10 px-2 py-2.5 text-center">
                  <p className="text-[10px] font-semibold uppercase tracking-wide text-cyan-100">Claridad</p>
                  <p className="mt-1 text-xs font-bold">Datos accionables</p>
                </div>
              </div>

              <div className="mt-6 flex flex-col gap-2 min-[420px]:flex-row">
                <button
                  type="button"
                  onClick={() => {
                    setAuthMode('register')
                    setScreen('auth')
                    setAuthError('')
                  }}
                  className="h-11 rounded-xl bg-emerald-400 px-5 text-sm font-bold text-slate-900 transition-transform hover:-translate-y-0.5 hover:bg-emerald-300"
                >
                  Crear cuenta
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setAuthMode('login')
                    setScreen('auth')
                    setAuthError('')
                  }}
                  className="h-11 rounded-xl border border-white/25 bg-transparent px-5 text-sm font-semibold text-white transition-colors hover:bg-white/10"
                >
                  Iniciar sesión
                </button>
              </div>
            </div>

            <div className="rounded-2xl border border-white/15 bg-slate-900/60 p-4 sm:p-5">
              <div className="relative overflow-hidden rounded-xl border border-white/10">
                <img
                  src="/cartoon-hero.svg"
                  alt="Ilustracion de personas usando la wallet"
                  className="h-36 w-full object-cover object-center sm:h-44"
                  loading="lazy"
                />
                <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-slate-950/75 to-transparent" />
                <p className="absolute bottom-3 left-3 rounded-full bg-black/35 px-2.5 py-1 text-[10px] font-semibold uppercase tracking-wider text-cyan-100">
                  Ilustraciones de uso
                </p>
              </div>
              <p className="text-xs font-semibold uppercase tracking-wider text-slate-300">Vista previa</p>
              <div className="mt-4 space-y-3">
                <div className="rounded-xl bg-white/10 p-3">
                  <p className="text-[11px] text-slate-300">Balance disponible</p>
                  <p className="mt-1 text-2xl font-extrabold text-white">$57.300</p>
                </div>
                <div className="grid grid-cols-3 gap-2">
                  <div className="rounded-lg bg-emerald-400/20 px-2 py-2 text-center">
                    <p className="text-[10px] font-semibold text-emerald-200">Ingresos</p>
                    <p className="text-xs font-bold text-emerald-100">$30.000</p>
                  </div>
                  <div className="rounded-lg bg-rose-400/20 px-2 py-2 text-center">
                    <p className="text-[10px] font-semibold text-rose-200">Gastos</p>
                    <p className="text-xs font-bold text-rose-100">$14.500</p>
                  </div>
                  <div className="rounded-lg bg-cyan-400/20 px-2 py-2 text-center">
                    <p className="text-[10px] font-semibold text-cyan-200">Objetivos</p>
                    <p className="text-xs font-bold text-cyan-100">2 activos</p>
                  </div>
                </div>
                <div className="h-2 rounded-full bg-white/10">
                  <div className="h-2 w-2/3 rounded-full bg-emerald-300" />
                </div>
              </div>
            </div>
          </article>

          <section className="mt-10 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {LANDING_FEATURES.map((feature) => (
              <article
                key={feature.title}
                className="reveal rounded-2xl border border-white/10 bg-white/5 p-5 backdrop-blur-sm"
                data-reveal
              >
                <div className="overflow-hidden rounded-xl border border-white/10">
                  <img
                    src={feature.image}
                    alt={feature.title}
                    className="h-36 w-full object-cover object-center transition-transform duration-500 hover:scale-[1.04]"
                    loading="lazy"
                  />
                </div>
                <p className="text-[10px] font-semibold uppercase tracking-[0.14em] text-cyan-200">Por que elegir Wallet</p>
                <h3 className="mt-3 text-lg font-bold text-white">{feature.title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-slate-200">{feature.description}</p>
                <p className="mt-4 inline-flex rounded-lg bg-emerald-400/20 px-2.5 py-1 text-[11px] font-semibold text-emerald-100">
                  {feature.stat}
                </p>
              </article>
            ))}
          </section>

          <section className="reveal mt-10 rounded-3xl border border-white/10 bg-gradient-to-r from-cyan-400/10 via-slate-900/60 to-emerald-300/10 p-5 sm:p-7" data-reveal>
            <p className="text-xs font-semibold uppercase tracking-[0.13em] text-cyan-100">Forma de trabajo</p>
            <h2 className="mt-3 text-2xl font-extrabold tracking-tight text-white sm:text-3xl">Un flujo simple para resultados reales</h2>
            <div className="mt-5 overflow-hidden rounded-2xl border border-white/10">
              <img
                src="/workflow-showcase.svg"
                alt="Flujo de trabajo de Wallet"
                className="h-44 w-full object-cover sm:h-56"
                loading="lazy"
              />
            </div>
            <div className="mt-6 grid gap-4 md:grid-cols-3">
              {LANDING_WORKFLOW.map((item) => (
                <article key={item.step} className="rounded-2xl border border-white/10 bg-white/5 p-4">
                  <p className="text-sm font-black text-emerald-200">{item.step}</p>
                  <h3 className="mt-2 text-base font-bold text-white">{item.title}</h3>
                  <p className="mt-1 text-sm text-slate-200">{item.description}</p>
                </article>
              ))}
            </div>
          </section>

          <section className="reveal mt-10 grid gap-4 sm:grid-cols-2" data-reveal>
            <article className="rounded-2xl border border-white/10 bg-white/5 p-5">
              <p className="text-xs font-semibold uppercase tracking-[0.13em] text-cyan-100">Como mostramos tus datos</p>
              <h3 className="mt-2 text-xl font-bold text-white">Contexto mensual + detalle diario</h3>
              <div className="mt-3 overflow-hidden rounded-xl border border-white/10">
                <img
                  src="/feature-visual.svg"
                  alt="Visualizacion de datos de Wallet"
                  className="h-32 w-full object-cover sm:h-40"
                  loading="lazy"
                />
              </div>
              <p className="mt-2 text-sm leading-relaxed text-slate-200">
                Combinamos resumen mensual, transacciones recientes y objetivos para que veas el mapa completo.
                No solo sabes cuanto tenes: entendés por que sube o baja.
              </p>
            </article>
            <article className="rounded-2xl border border-white/10 bg-white/5 p-5">
              <p className="text-xs font-semibold uppercase tracking-[0.13em] text-cyan-100">Propiedades clave</p>
              <ul className="mt-2 space-y-2 text-sm text-slate-200">
                <li>Dashboard reactivo sin recargas.</li>
                <li>Objetivos editables y filtrables por estado.</li>
                <li>Persistencia local para no perder contexto.</li>
                <li>Diseño mobile-first y experiencia limpia.</li>
              </ul>
            </article>
          </section>

          <footer className="reveal mt-12 border-t border-white/10 pb-6 pt-6" data-reveal>
            <div className="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <p className="text-sm font-bold text-white">Wallet Digital</p>
                <p className="mt-1 max-w-md text-xs text-slate-300">
                  La wallet pensada para ordenar tus finanzas de forma visual, simple y segura.
                </p>
              </div>
              <div className="grid grid-cols-2 gap-4 text-xs text-slate-300">
                <div>
                  <p className="font-semibold text-white">Producto</p>
                  <p className="mt-1">Dashboard</p>
                  <p>Objetivos</p>
                  <p>Reportes</p>
                </div>
                <div>
                  <p className="font-semibold text-white">Legal</p>
                  <p className="mt-1">Privacidad</p>
                  <p>Términos</p>
                  <p>Seguridad</p>
                </div>
              </div>
            </div>
            <p className="mt-6 text-[11px] text-slate-400">© {new Date().getFullYear()} Wallet Digital. Todos los derechos reservados.</p>
          </footer>
        </section>
      </main>
    )
  }

  if (screen === 'auth') {
    return (
      <main className="relative min-h-screen overflow-hidden bg-slate-100 px-4 py-8 text-slate-900 min-[320px]:px-5 sm:px-7 lg:px-12">
        <div className="pointer-events-none absolute inset-0">
          <div className="absolute -top-16 left-1/2 h-72 w-72 -translate-x-1/2 rounded-full bg-cyan-100/60 blur-3xl" />
          <div className="absolute bottom-10 left-8 h-44 w-44 rounded-full bg-emerald-100/60 blur-2xl" />
        </div>

        <section className="relative mx-auto flex min-h-[calc(100vh-4rem)] w-full max-w-[420px] items-center">
          <article className="w-full rounded-3xl border border-white/70 bg-white p-5 shadow-[0_16px_45px_rgba(15,23,42,0.11)] min-[360px]:p-6">
            <button
              type="button"
              onClick={() => {
                setScreen('home')
                setAuthError('')
              }}
              className="text-xs font-semibold text-slate-500 transition-colors hover:text-slate-700"
            >
              ← Volver al sitio
            </button>

            <h2 className="mt-4 text-2xl font-extrabold tracking-tight text-slate-900">
              {authMode === 'login' ? 'Bienvenido de nuevo' : authMode === 'register' ? 'Creá tu cuenta' : authMode === 'forgot-password' ? 'Recuperar contraseña' : 'Nueva contraseña'}
            </h2>
            <p className="mt-1 text-sm text-slate-500">
              {authMode === 'login'
                ? 'Ingresá para ver tu wallet y tus movimientos.'
                : authMode === 'register'
                ? 'Te lleva menos de un minuto empezar.'
                : authMode === 'forgot-password'
                ? 'Te enviaremos un enlace para restablecer tu contraseña.'
                : 'Ingresá tu nueva contraseña.'}
            </p>

            {sessionNotice && (
              <p className="mt-3 rounded-xl bg-amber-50 px-3 py-2 text-xs font-semibold text-amber-700">
                {sessionNotice}
              </p>
            )}

            {/* ── Forgot-password form ── */}
            {authMode === 'forgot-password' && (
              <form className="mt-5 space-y-3" onSubmit={handleForgotPassword}>
                <input
                  type="email"
                  value={forgotEmail}
                  onChange={(e) => setForgotEmail(e.target.value)}
                  placeholder="Email de tu cuenta"
                  autoFocus
                  className="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-300 focus:outline-none"
                />
                {forgotError && <p className="text-xs font-semibold text-rose-500">{forgotError}</p>}
                {forgotSuccess && <p className="text-xs font-semibold text-emerald-600">{forgotSuccess}</p>}
                <button
                  type="submit"
                  disabled={forgotLoading}
                  className="h-11 w-full rounded-xl bg-slate-900 text-sm font-semibold text-white transition-colors hover:bg-slate-700 disabled:cursor-not-allowed disabled:opacity-70"
                >
                  {forgotLoading ? 'Enviando...' : 'Enviar enlace'}
                </button>
                <div className="border-t border-slate-100 pt-3 text-center text-xs text-slate-500">
                  <button
                    type="button"
                    onClick={() => { setAuthMode('login'); setForgotError(''); setForgotSuccess('') }}
                    className="font-bold text-slate-700 hover:text-slate-900"
                  >
                    ← Volver al login
                  </button>
                </div>
              </form>
            )}

            {/* ── Reset-password form ── */}
            {authMode === 'reset-password' && (
              <form className="mt-5 space-y-3" onSubmit={handleResetPassword}>
                <input
                  type="password"
                  value={resetNewPassword}
                  onChange={(e) => setResetNewPassword(e.target.value)}
                  placeholder="Nueva contraseña (min. 8 caracteres)"
                  autoFocus
                  className="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-300 focus:outline-none"
                />
                <input
                  type="password"
                  value={resetConfirm}
                  onChange={(e) => setResetConfirm(e.target.value)}
                  placeholder="Repetir nueva contraseña"
                  className="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-300 focus:outline-none"
                />
                {resetError && <p className="text-xs font-semibold text-rose-500">{resetError}</p>}
                {resetSuccess && <p className="text-xs font-semibold text-emerald-600">{resetSuccess}</p>}
                <button
                  type="submit"
                  disabled={resetLoading}
                  className="h-11 w-full rounded-xl bg-slate-900 text-sm font-semibold text-white transition-colors hover:bg-slate-700 disabled:cursor-not-allowed disabled:opacity-70"
                >
                  {resetLoading ? 'Guardando...' : 'Cambiar contraseña'}
                </button>
              </form>
            )}

            {/* ── Login / Register form ── */}
            {(authMode === 'login' || authMode === 'register') && (
            <form className="mt-5 space-y-3" onSubmit={handleAuthSubmit}>
              {authMode === 'register' && (
                <input
                  type="text"
                  value={authName}
                  onChange={(e) => setAuthName(e.target.value)}
                  placeholder="Nombre completo"
                  className="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-300 focus:outline-none"
                />
              )}
              <input
                type="email"
                value={authEmail}
                onChange={(e) => setAuthEmail(e.target.value)}
                placeholder="Email"
                className="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-300 focus:outline-none"
              />
              <input
                type="password"
                value={authPassword}
                onChange={(e) => setAuthPassword(e.target.value)}
                placeholder="Contraseña"
                className="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-300 focus:outline-none"
              />
              {authMode === 'register' && (
                <input
                  type="password"
                  value={authConfirmPassword}
                  onChange={(e) => setAuthConfirmPassword(e.target.value)}
                  placeholder="Repetir contraseña"
                  className="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-700 placeholder:text-slate-400 focus:border-slate-300 focus:outline-none"
                />
              )}

              {authError && <p className="text-xs font-semibold text-rose-500">{authError}</p>}

              <button
                type="submit"
                disabled={authLoading}
                className="h-11 w-full rounded-xl bg-slate-900 text-sm font-semibold text-white transition-colors hover:bg-slate-700 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {authLoading ? 'Conectando...' : authMode === 'login' ? 'Entrar' : 'Registrarme'}
              </button>

              {authMode === 'login' && (
                <div className="text-center">
                  <button
                    type="button"
                    onClick={() => { setAuthMode('forgot-password'); setAuthError('') }}
                    className="text-xs text-slate-400 hover:text-slate-600"
                  >
                    ¿Olvidaste tu contraseña?
                  </button>
                </div>
              )}
            </form>
            )}

            {(authMode === 'login' || authMode === 'register') && (
            <div className="mt-4 border-t border-slate-100 pt-4 text-center text-xs text-slate-500">
              {authMode === 'login' ? '¿No tenés cuenta?' : '¿Ya tenés cuenta?'}{' '}
              <button
                type="button"
                onClick={() => {
                  setAuthMode((prev) => (prev === 'login' ? 'register' : 'login'))
                  setAuthError('')
                }}
                className="font-bold text-slate-700 hover:text-slate-900"
              >
                {authMode === 'login' ? 'Registrate' : 'Iniciá sesión'}
              </button>
            </div>
            )}
          </article>
        </section>
      </main>
    )
  }

  return renderDashboard()
}

export default App
