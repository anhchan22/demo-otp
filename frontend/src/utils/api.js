const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'

export async function api(path, options = {}) {
  const response = await fetch(`${API_URL}${path}`, {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  })
  const body = await response.json().catch(() => ({}))
  if (!response.ok) {
    const exception = new Error(body.message || 'Có lỗi xảy ra, vui lòng thử lại')
    exception.code = body.code
    exception.channels = body.channels
    throw exception
  }
  return body
}
