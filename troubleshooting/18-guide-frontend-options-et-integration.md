# 18 - Guide Frontend : Options et Intégration avec l'API

## 🎯 Objectif

Créer un frontend pour consommer l'API Contact. Ce guide présente toutes les options possibles, du plus simple au plus avancé.

---

# PARTIE A : COMPARAISON DES OPTIONS

## 📊 Tableau comparatif

| Option | Difficulté | Temps | Build | Idéal pour |
|--------|------------|-------|-------|------------|
| **HTML/CSS/JS** | ⭐ Facile | 30 min | Non | Débutants, prototype rapide |
| **Bootstrap + JS** | ⭐ Facile | 45 min | Non | Design rapide sans CSS |
| **Tailwind CDN** | ⭐⭐ Moyen | 1h | Non | Design moderne sans build |
| **React** | ⭐⭐⭐ Moyen | 2h | Oui | SPA, composants réutilisables |
| **Vue.js** | ⭐⭐⭐ Moyen | 2h | Oui | SPA, syntaxe intuitive |
| **Svelte** | ⭐⭐⭐ Moyen | 2h | Oui | Performance, moins de code |
| **Next.js** | ⭐⭐⭐⭐ Avancé | 3h | Oui | SEO, SSR, production |
| **Nuxt.js** | ⭐⭐⭐⭐ Avancé | 3h | Oui | Vue + SSR |
| **Angular** | ⭐⭐⭐⭐⭐ Avancé | 4h | Oui | Entreprise, structure stricte |

---

## 🏆 Ma recommandation

| Situation | Recommandation |
|-----------|----------------|
| Débutant / Prototype | **HTML/CSS/JS** |
| Veut du style rapide | **Bootstrap + JS** |
| Projet moderne simple | **React ou Vue** |
| Production / SEO | **Next.js** |

---

# PARTIE B : OPTION 1 - HTML/CSS/JS VANILLA (LE PLUS SIMPLE)

## ⭐ Avantages

- ✅ Aucun outil à installer
- ✅ Fonctionne directement dans le navigateur
- ✅ Facile à comprendre
- ✅ Pas de build/compilation
- ✅ Un seul fichier

---

## 📁 Structure

```
frontend/
└── index.html    ← Un seul fichier !
```

---

## 📄 Code complet : `index.html`

```html
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Formulaire de Contact</title>
    <style>
        /* ============================================
           VARIABLES & RESET
           ============================================ */
        :root {
            --primary: #2563eb;
            --primary-dark: #1d4ed8;
            --success: #10b981;
            --error: #ef4444;
            --bg: #f8fafc;
            --card-bg: #ffffff;
            --text: #1e293b;
            --text-light: #64748b;
            --border: #e2e8f0;
            --shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        /* ============================================
           CONTAINER
           ============================================ */
        .container {
            background: var(--card-bg);
            border-radius: 16px;
            box-shadow: var(--shadow);
            padding: 40px;
            width: 100%;
            max-width: 500px;
        }

        /* ============================================
           HEADER
           ============================================ */
        .header {
            text-align: center;
            margin-bottom: 30px;
        }

        .header h1 {
            color: var(--text);
            font-size: 28px;
            margin-bottom: 8px;
        }

        .header p {
            color: var(--text-light);
            font-size: 14px;
        }

        /* ============================================
           FORM
           ============================================ */
        .form-group {
            margin-bottom: 20px;
        }

        label {
            display: block;
            color: var(--text);
            font-weight: 500;
            margin-bottom: 6px;
            font-size: 14px;
        }

        label .required {
            color: var(--error);
        }

        input, select, textarea {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid var(--border);
            border-radius: 8px;
            font-size: 16px;
            transition: border-color 0.2s, box-shadow 0.2s;
            font-family: inherit;
        }

        input:focus, select:focus, textarea:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
        }

        textarea {
            resize: vertical;
            min-height: 120px;
        }

        /* ============================================
           BUTTON
           ============================================ */
        .btn {
            width: 100%;
            padding: 14px;
            background: var(--primary);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.2s, transform 0.1s;
        }

        .btn:hover {
            background: var(--primary-dark);
        }

        .btn:active {
            transform: scale(0.98);
        }

        .btn:disabled {
            background: var(--text-light);
            cursor: not-allowed;
        }

        /* ============================================
           MESSAGES
           ============================================ */
        .message {
            padding: 14px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
            display: none;
        }

        .message.success {
            background: #d1fae5;
            color: #065f46;
            border: 1px solid #a7f3d0;
        }

        .message.error {
            background: #fee2e2;
            color: #991b1b;
            border: 1px solid #fecaca;
        }

        .message.show {
            display: block;
            animation: slideIn 0.3s ease;
        }

        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateY(-10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        /* ============================================
           LOADING SPINNER
           ============================================ */
        .spinner {
            display: inline-block;
            width: 20px;
            height: 20px;
            border: 2px solid rgba(255,255,255,0.3);
            border-radius: 50%;
            border-top-color: white;
            animation: spin 0.8s linear infinite;
            margin-right: 8px;
            vertical-align: middle;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        /* ============================================
           RESPONSIVE
           ============================================ */
        @media (max-width: 480px) {
            .container {
                padding: 24px;
            }
            
            .header h1 {
                font-size: 24px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>📧 Contactez-nous</h1>
            <p>Remplissez le formulaire ci-dessous, nous vous répondrons rapidement.</p>
        </div>

        <div id="successMessage" class="message success">
            ✅ Merci ! Votre message a été envoyé avec succès.
        </div>

        <div id="errorMessage" class="message error">
            ❌ Une erreur est survenue. Veuillez réessayer.
        </div>

        <form id="contactForm">
            <div class="form-group">
                <label for="fullName">
                    Nom complet <span class="required">*</span>
                </label>
                <input 
                    type="text" 
                    id="fullName" 
                    name="fullName" 
                    placeholder="Jean Dupont"
                    required
                >
            </div>

            <div class="form-group">
                <label for="company">Entreprise</label>
                <input 
                    type="text" 
                    id="company" 
                    name="company" 
                    placeholder="Mon Entreprise Inc."
                >
            </div>

            <div class="form-group">
                <label for="email">
                    Email <span class="required">*</span>
                </label>
                <input 
                    type="email" 
                    id="email" 
                    name="email" 
                    placeholder="jean.dupont@email.com"
                    required
                >
            </div>

            <div class="form-group">
                <label for="phone">Téléphone</label>
                <input 
                    type="tel" 
                    id="phone" 
                    name="phone" 
                    placeholder="514-555-1234"
                >
            </div>

            <div class="form-group">
                <label for="requestType">
                    Type de demande <span class="required">*</span>
                </label>
                <select id="requestType" name="requestType" required>
                    <option value="">-- Sélectionnez --</option>
                    <option value="INFO">Demande d'information</option>
                    <option value="QUOTE">Demande de devis</option>
                    <option value="SUPPORT">Support technique</option>
                    <option value="PARTNERSHIP">Partenariat</option>
                    <option value="OTHER">Autre</option>
                </select>
            </div>

            <div class="form-group">
                <label for="message">
                    Message <span class="required">*</span>
                </label>
                <textarea 
                    id="message" 
                    name="message" 
                    placeholder="Décrivez votre demande..."
                    required
                ></textarea>
            </div>

            <button type="submit" class="btn" id="submitBtn">
                Envoyer le message
            </button>
        </form>
    </div>

    <script>
        // ============================================
        // CONFIGURATION
        // ============================================
        
        // Changez cette URL selon votre environnement
        const API_URL = 'http://localhost:8080/api/contact';
        
        // Pour Codespaces, utilisez :
        // const API_URL = 'https://votre-codespace-8080.app.github.dev/api/contact';

        // ============================================
        // DOM ELEMENTS
        // ============================================
        const form = document.getElementById('contactForm');
        const submitBtn = document.getElementById('submitBtn');
        const successMessage = document.getElementById('successMessage');
        const errorMessage = document.getElementById('errorMessage');

        // ============================================
        // FORM SUBMISSION
        // ============================================
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            // Hide messages
            successMessage.classList.remove('show');
            errorMessage.classList.remove('show');
            
            // Show loading
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="spinner"></span>Envoi en cours...';
            
            // Get form data
            const formData = {
                fullName: document.getElementById('fullName').value,
                company: document.getElementById('company').value || null,
                email: document.getElementById('email').value,
                phone: document.getElementById('phone').value || null,
                requestType: document.getElementById('requestType').value,
                message: document.getElementById('message').value
            };
            
            try {
                const response = await fetch(API_URL, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(formData)
                });
                
                if (response.ok) {
                    // Success
                    successMessage.classList.add('show');
                    form.reset();
                } else {
                    // API error
                    const error = await response.json();
                    console.error('API Error:', error);
                    errorMessage.textContent = '❌ ' + (error.message || 'Une erreur est survenue.');
                    errorMessage.classList.add('show');
                }
            } catch (error) {
                // Network error
                console.error('Network Error:', error);
                errorMessage.textContent = '❌ Impossible de contacter le serveur.';
                errorMessage.classList.add('show');
            } finally {
                // Reset button
                submitBtn.disabled = false;
                submitBtn.innerHTML = 'Envoyer le message';
            }
        });
    </script>
</body>
</html>
```

---

## 🚀 Comment utiliser

### Option A : Ouvrir directement

1. Créez un fichier `index.html`
2. Copiez le code ci-dessus
3. Double-cliquez sur le fichier pour l'ouvrir dans le navigateur

### Option B : Avec Live Server (VS Code)

1. Installez l'extension **Live Server**
2. Clic droit sur `index.html` → **Open with Live Server**

### Option C : Servir avec Python

```bash
# Dans le dossier frontend/
python -m http.server 3000
```

Ouvrez http://localhost:3000

---

## ⚠️ Configuration CORS

Si vous avez une erreur CORS, l'API doit autoriser votre origine. 
L'API est déjà configurée pour accepter `*` (toutes les origines).

---

# PARTIE C : OPTION 2 - BOOTSTRAP (STYLE RAPIDE)

## 📄 Version Bootstrap

```html
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Contact - Bootstrap</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container py-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card shadow">
                    <div class="card-body p-4">
                        <h2 class="card-title text-center mb-4">📧 Contactez-nous</h2>
                        
                        <div id="alert" class="alert d-none"></div>
                        
                        <form id="contactForm">
                            <div class="mb-3">
                                <label class="form-label">Nom complet *</label>
                                <input type="text" class="form-control" id="fullName" required>
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label">Entreprise</label>
                                <input type="text" class="form-control" id="company">
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label">Email *</label>
                                <input type="email" class="form-control" id="email" required>
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label">Téléphone</label>
                                <input type="tel" class="form-control" id="phone">
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label">Type de demande *</label>
                                <select class="form-select" id="requestType" required>
                                    <option value="">-- Sélectionnez --</option>
                                    <option value="INFO">Information</option>
                                    <option value="QUOTE">Devis</option>
                                    <option value="SUPPORT">Support</option>
                                    <option value="PARTNERSHIP">Partenariat</option>
                                    <option value="OTHER">Autre</option>
                                </select>
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label">Message *</label>
                                <textarea class="form-control" id="message" rows="4" required></textarea>
                            </div>
                            
                            <button type="submit" class="btn btn-primary w-100" id="submitBtn">
                                Envoyer
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        const API_URL = 'http://localhost:8080/api/contact';
        
        document.getElementById('contactForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const alert = document.getElementById('alert');
            const btn = document.getElementById('submitBtn');
            
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Envoi...';
            
            try {
                const response = await fetch(API_URL, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        fullName: document.getElementById('fullName').value,
                        company: document.getElementById('company').value || null,
                        email: document.getElementById('email').value,
                        phone: document.getElementById('phone').value || null,
                        requestType: document.getElementById('requestType').value,
                        message: document.getElementById('message').value
                    })
                });
                
                if (response.ok) {
                    alert.className = 'alert alert-success';
                    alert.textContent = '✅ Message envoyé avec succès !';
                    e.target.reset();
                } else {
                    throw new Error('Erreur serveur');
                }
            } catch (error) {
                alert.className = 'alert alert-danger';
                alert.textContent = '❌ Erreur lors de l\'envoi.';
            } finally {
                alert.classList.remove('d-none');
                btn.disabled = false;
                btn.textContent = 'Envoyer';
            }
        });
    </script>
</body>
</html>
```

---

# PARTIE D : OPTION 3 - REACT

## 📦 Installation

```bash
npx create-react-app contact-form
cd contact-form
npm start
```

## 📄 `src/ContactForm.jsx`

```jsx
import { useState } from 'react';
import './ContactForm.css';

const API_URL = 'http://localhost:8080/api/contact';

export default function ContactForm() {
  const [formData, setFormData] = useState({
    fullName: '',
    company: '',
    email: '',
    phone: '',
    requestType: '',
    message: ''
  });
  const [status, setStatus] = useState({ type: '', message: '' });
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setStatus({ type: '', message: '' });

    try {
      const response = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });

      if (response.ok) {
        setStatus({ type: 'success', message: 'Message envoyé avec succès !' });
        setFormData({ fullName: '', company: '', email: '', phone: '', requestType: '', message: '' });
      } else {
        throw new Error('Erreur serveur');
      }
    } catch (error) {
      setStatus({ type: 'error', message: 'Erreur lors de l\'envoi.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="contact-form">
      <h1>📧 Contactez-nous</h1>
      
      {status.message && (
        <div className={`alert ${status.type}`}>{status.message}</div>
      )}
      
      <form onSubmit={handleSubmit}>
        <input
          name="fullName"
          placeholder="Nom complet *"
          value={formData.fullName}
          onChange={handleChange}
          required
        />
        <input
          name="company"
          placeholder="Entreprise"
          value={formData.company}
          onChange={handleChange}
        />
        <input
          name="email"
          type="email"
          placeholder="Email *"
          value={formData.email}
          onChange={handleChange}
          required
        />
        <input
          name="phone"
          placeholder="Téléphone"
          value={formData.phone}
          onChange={handleChange}
        />
        <select
          name="requestType"
          value={formData.requestType}
          onChange={handleChange}
          required
        >
          <option value="">Type de demande *</option>
          <option value="INFO">Information</option>
          <option value="QUOTE">Devis</option>
          <option value="SUPPORT">Support</option>
          <option value="PARTNERSHIP">Partenariat</option>
          <option value="OTHER">Autre</option>
        </select>
        <textarea
          name="message"
          placeholder="Votre message *"
          value={formData.message}
          onChange={handleChange}
          required
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Envoi...' : 'Envoyer'}
        </button>
      </form>
    </div>
  );
}
```

---

# PARTIE E : OPTION 4 - VUE.JS

## 📦 Installation

```bash
npm create vue@latest contact-form
cd contact-form
npm install
npm run dev
```

## 📄 `src/components/ContactForm.vue`

```vue
<template>
  <div class="contact-form">
    <h1>📧 Contactez-nous</h1>
    
    <div v-if="status" :class="['alert', status.type]">
      {{ status.message }}
    </div>
    
    <form @submit.prevent="handleSubmit">
      <input v-model="form.fullName" placeholder="Nom complet *" required />
      <input v-model="form.company" placeholder="Entreprise" />
      <input v-model="form.email" type="email" placeholder="Email *" required />
      <input v-model="form.phone" placeholder="Téléphone" />
      <select v-model="form.requestType" required>
        <option value="">Type de demande *</option>
        <option value="INFO">Information</option>
        <option value="QUOTE">Devis</option>
        <option value="SUPPORT">Support</option>
      </select>
      <textarea v-model="form.message" placeholder="Message *" required></textarea>
      <button :disabled="loading">{{ loading ? 'Envoi...' : 'Envoyer' }}</button>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';

const API_URL = 'http://localhost:8080/api/contact';

const form = reactive({
  fullName: '',
  company: '',
  email: '',
  phone: '',
  requestType: '',
  message: ''
});

const loading = ref(false);
const status = ref(null);

async function handleSubmit() {
  loading.value = true;
  status.value = null;
  
  try {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form)
    });
    
    if (response.ok) {
      status.value = { type: 'success', message: 'Message envoyé !' };
      Object.keys(form).forEach(key => form[key] = '');
    } else {
      throw new Error();
    }
  } catch {
    status.value = { type: 'error', message: 'Erreur lors de l\'envoi.' };
  } finally {
    loading.value = false;
  }
}
</script>
```

---

# PARTIE F : OPTION 5 - SVELTE

## 📦 Installation

```bash
npm create svelte@latest contact-form
cd contact-form
npm install
npm run dev
```

## 📄 `src/routes/+page.svelte`

```svelte
<script>
  const API_URL = 'http://localhost:8080/api/contact';
  
  let form = {
    fullName: '',
    company: '',
    email: '',
    phone: '',
    requestType: '',
    message: ''
  };
  
  let loading = false;
  let status = null;
  
  async function handleSubmit() {
    loading = true;
    status = null;
    
    try {
      const response = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      });
      
      if (response.ok) {
        status = { type: 'success', message: 'Message envoyé !' };
        form = { fullName: '', company: '', email: '', phone: '', requestType: '', message: '' };
      } else {
        throw new Error();
      }
    } catch {
      status = { type: 'error', message: 'Erreur lors de l\'envoi.' };
    } finally {
      loading = false;
    }
  }
</script>

<div class="contact-form">
  <h1>📧 Contactez-nous</h1>
  
  {#if status}
    <div class="alert {status.type}">{status.message}</div>
  {/if}
  
  <form on:submit|preventDefault={handleSubmit}>
    <input bind:value={form.fullName} placeholder="Nom complet *" required />
    <input bind:value={form.company} placeholder="Entreprise" />
    <input bind:value={form.email} type="email" placeholder="Email *" required />
    <input bind:value={form.phone} placeholder="Téléphone" />
    <select bind:value={form.requestType} required>
      <option value="">Type de demande *</option>
      <option value="INFO">Information</option>
      <option value="QUOTE">Devis</option>
      <option value="SUPPORT">Support</option>
    </select>
    <textarea bind:value={form.message} placeholder="Message *" required></textarea>
    <button disabled={loading}>{loading ? 'Envoi...' : 'Envoyer'}</button>
  </form>
</div>
```

---

# PARTIE G : OPTION 6 - NEXT.JS

## 📦 Installation

```bash
npx create-next-app@latest contact-form
cd contact-form
npm run dev
```

## 📄 `app/page.tsx`

```tsx
'use client';

import { useState, FormEvent } from 'react';

const API_URL = 'http://localhost:8080/api/contact';

interface FormData {
  fullName: string;
  company: string;
  email: string;
  phone: string;
  requestType: string;
  message: string;
}

export default function ContactPage() {
  const [form, setForm] = useState<FormData>({
    fullName: '',
    company: '',
    email: '',
    phone: '',
    requestType: '',
    message: ''
  });
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState<{ type: string; message: string } | null>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setStatus(null);

    try {
      const response = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      });

      if (response.ok) {
        setStatus({ type: 'success', message: 'Message envoyé avec succès !' });
        setForm({ fullName: '', company: '', email: '', phone: '', requestType: '', message: '' });
      } else {
        throw new Error();
      }
    } catch {
      setStatus({ type: 'error', message: 'Erreur lors de l\'envoi.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen flex items-center justify-center bg-gradient-to-br from-purple-600 to-blue-500 p-4">
      <div className="bg-white rounded-2xl shadow-xl p-8 w-full max-w-md">
        <h1 className="text-2xl font-bold text-center mb-6">📧 Contactez-nous</h1>
        
        {status && (
          <div className={`p-4 rounded mb-4 ${status.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
            {status.message}
          </div>
        )}
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            className="w-full p-3 border rounded"
            placeholder="Nom complet *"
            value={form.fullName}
            onChange={(e) => setForm({ ...form, fullName: e.target.value })}
            required
          />
          {/* ... autres champs ... */}
          <button
            type="submit"
            disabled={loading}
            className="w-full p-3 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
          >
            {loading ? 'Envoi...' : 'Envoyer'}
          </button>
        </form>
      </div>
    </main>
  );
}
```

---

# PARTIE H : DÉPLOYER LE FRONTEND

## 📊 Options de déploiement gratuit

| Service | Pour | URL |
|---------|------|-----|
| **GitHub Pages** | HTML/CSS/JS | pages.github.com |
| **Netlify** | Tous | netlify.com |
| **Vercel** | React/Next.js | vercel.com |
| **Render** | Tous | render.com |
| **Surge** | HTML/CSS/JS | surge.sh |

---

## 🚀 Déployer HTML sur GitHub Pages

1. Créez un repo GitHub
2. Ajoutez votre `index.html`
3. Settings → Pages → Source: main branch
4. Votre site sera sur : `https://username.github.io/repo-name`

---

## 🚀 Déployer sur Netlify (drag & drop)

1. Allez sur https://app.netlify.com/drop
2. Glissez votre dossier `frontend/`
3. C'est déployé !

---

# RÉCAPITULATIF

## 🎯 Quelle option choisir ?

```
┌─────────────────────────────────────────────────────────────┐
│                    ARBRE DE DÉCISION                         │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Débutant ou prototype rapide ?                              │
│  └── OUI → HTML/CSS/JS ou Bootstrap                          │
│  └── NON ↓                                                   │
│                                                              │
│  Besoin de composants réutilisables ?                        │
│  └── OUI ↓                                                   │
│  └── NON → HTML/CSS/JS                                       │
│                                                              │
│  Préférence syntaxe ?                                        │
│  ├── JSX → React                                             │
│  ├── HTML-like → Vue.js                                      │
│  └── Minimaliste → Svelte                                    │
│                                                              │
│  SEO important / Production ?                                │
│  └── OUI → Next.js (React) ou Nuxt.js (Vue)                  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## ✅ Checklist

- [ ] Choisir une option (HTML/CSS/JS recommandé pour débuter)
- [ ] Créer le fichier
- [ ] Configurer `API_URL` selon votre environnement
- [ ] Tester le formulaire
- [ ] Vérifier que le lead est créé (voir guide 17)
- [ ] Déployer (optionnel)

---

## 🎉 Résultat final

```
┌────────────────────────────────────────────────────────────┐
│                    ARCHITECTURE COMPLÈTE                    │
├────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌──────────────┐         ┌──────────────┐                │
│   │   Frontend   │  HTTP   │   Backend    │                │
│   │  (HTML/React │ ──────► │  Spring Boot │                │
│   │   /Vue/etc)  │   POST  │    :8080     │                │
│   └──────────────┘         └──────┬───────┘                │
│                                   │                         │
│                     ┌─────────────┴─────────────┐          │
│                     │                           │          │
│                     ▼                           ▼          │
│              ┌──────────────┐           ┌──────────────┐   │
│              │  PostgreSQL  │           │    Gmail     │   │
│              │   Database   │           │   (emails)   │   │
│              └──────────────┘           └──────────────┘   │
│                                                             │
└────────────────────────────────────────────────────────────┘
```

Vous avez maintenant toutes les options pour créer votre frontend ! 🎊

