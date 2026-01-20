# 28 - Guide : Integrer le Backend dans un Site Externe (Vercel, Next.js, Svelte)

## Scenario

Vous avez :
- Un backend Spring Boot deploye (sur VPS ou Codespaces)
- Un site personnel deploye sur Vercel (ou autre)
- Vous voulez ajouter un formulaire de contact qui utilise ce backend

---

# PARTIE 1 : PREREQUIS - DEPLOYER LE BACKEND

## Option A : Codespaces (temporaire, pour tests)

Le backend sur Codespaces n'est pas permanent. Il s'arrete apres inactivite.

URL exemple :
```
https://fluffy-palm-tree-97qr459xpx472xqq5-8080.app.github.dev
```

## Option B : VPS (permanent, recommande pour production)

Deployez sur un VPS (DigitalOcean, Hetzner, etc.) pour avoir une URL permanente :
```
https://api.votredomaine.com
```

Voir guide 20 pour le deploiement sur VPS.

---

# PARTIE 2 : CONFIGURER CORS

## Pourquoi CORS ?

Votre site Vercel (ex: `https://monsite.vercel.app`) veut appeler votre API (ex: `https://api.monsite.com`). Ce sont deux domaines differents = CORS necessaire.

## Modifier la configuration CORS

### Fichier : `src/main/java/com/example/contact/config/SecurityConfig.java`

Trouvez la methode `corsConfigurationSource()` et modifiez-la :

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // IMPORTANT : Ajoutez vos domaines ici
    configuration.setAllowedOrigins(List.of(
        "http://localhost:3000",                    // Dev local
        "http://localhost:5173",                    // Vite dev server
        "https://monsite.vercel.app",               // Votre site Vercel
        "https://www.monsite.com",                  // Votre domaine custom
        "https://monsite-svelte.vercel.app",        // Autre site Svelte
        "https://monsite-nextjs.vercel.app"         // Autre site Next.js
    ));
    
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setExposedHeaders(List.of("Authorization"));
    configuration.setAllowCredentials(true);  // Si vous utilisez des cookies

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

## Reconstruire et redeployer

```bash
# Sur Codespaces
docker compose -f docker-compose.gmail.yml down
docker compose -f docker-compose.gmail.yml up --build -d

# Sur VPS
git pull
docker compose -f docker-compose.prod.yml up --build -d
```

---

# PARTIE 3 : INTEGRATION HTML SIMPLE

## Code complet a copier dans votre site

```html
<!-- Formulaire de contact -->
<div id="contact-form-container">
  <h2>Contactez-nous</h2>
  
  <div id="contact-message" style="display: none; padding: 15px; margin-bottom: 20px; border-radius: 5px;"></div>
  
  <form id="contactForm">
    <div style="margin-bottom: 15px;">
      <label for="fullName">Nom complet *</label>
      <input type="text" id="fullName" name="fullName" required 
             style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 5px;">
    </div>
    
    <div style="margin-bottom: 15px;">
      <label for="company">Entreprise</label>
      <input type="text" id="company" name="company"
             style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 5px;">
    </div>
    
    <div style="margin-bottom: 15px;">
      <label for="email">Email *</label>
      <input type="email" id="email" name="email" required
             style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 5px;">
    </div>
    
    <div style="margin-bottom: 15px;">
      <label for="phone">Telephone</label>
      <input type="tel" id="phone" name="phone"
             style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 5px;">
    </div>
    
    <div style="margin-bottom: 15px;">
      <label for="requestType">Type de demande *</label>
      <select id="requestType" name="requestType" required
              style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 5px;">
        <option value="">-- Selectionnez --</option>
        <option value="INFO">Demande d'information</option>
        <option value="QUOTE">Demande de devis</option>
        <option value="SUPPORT">Support technique</option>
        <option value="PARTNERSHIP">Partenariat</option>
        <option value="OTHER">Autre</option>
      </select>
    </div>
    
    <div style="margin-bottom: 15px;">
      <label for="message">Message *</label>
      <textarea id="message" name="message" rows="5" required
                style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 5px;"></textarea>
    </div>
    
    <button type="submit" id="submitBtn"
            style="width: 100%; padding: 15px; background: #007bff; color: white; border: none; border-radius: 5px; cursor: pointer; font-size: 16px;">
      Envoyer le message
    </button>
  </form>
</div>

<script>
// CONFIGURATION - Modifiez cette URL
const API_URL = 'https://votre-api.com/api/contact';

// Exemples d'URLs :
// Codespaces : 'https://fluffy-palm-tree-97qr459xpx472xqq5-8080.app.github.dev/api/contact'
// VPS : 'https://api.votredomaine.com/api/contact'

document.getElementById('contactForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const messageDiv = document.getElementById('contact-message');
    const submitBtn = document.getElementById('submitBtn');
    
    // Cacher le message precedent
    messageDiv.style.display = 'none';
    
    // Desactiver le bouton
    submitBtn.disabled = true;
    submitBtn.textContent = 'Envoi en cours...';
    
    // Recuperer les donnees du formulaire
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
            messageDiv.style.display = 'block';
            messageDiv.style.background = '#d4edda';
            messageDiv.style.color = '#155724';
            messageDiv.textContent = 'Merci ! Votre message a ete envoye avec succes.';
            document.getElementById('contactForm').reset();
        } else {
            const error = await response.json();
            throw new Error(error.message || 'Erreur serveur');
        }
    } catch (error) {
        messageDiv.style.display = 'block';
        messageDiv.style.background = '#f8d7da';
        messageDiv.style.color = '#721c24';
        messageDiv.textContent = 'Erreur : ' + error.message;
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Envoyer le message';
    }
});
</script>
```

---

# PARTIE 4 : INTEGRATION REACT

## Composant React complet

Creez un fichier `ContactForm.jsx` :

```jsx
import { useState } from 'react';

// CONFIGURATION - Modifiez cette URL
const API_URL = 'https://votre-api.com/api/contact';

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
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setStatus({ type: '', message: '' });

    try {
      const response = await fetch(API_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...formData,
          company: formData.company || null,
          phone: formData.phone || null
        })
      });

      if (response.ok) {
        setStatus({ 
          type: 'success', 
          message: 'Merci ! Votre message a ete envoye avec succes.' 
        });
        setFormData({
          fullName: '',
          company: '',
          email: '',
          phone: '',
          requestType: '',
          message: ''
        });
      } else {
        const error = await response.json();
        throw new Error(error.message || 'Erreur serveur');
      }
    } catch (error) {
      setStatus({ 
        type: 'error', 
        message: 'Erreur : ' + error.message 
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="contact-form">
      <h2>Contactez-nous</h2>
      
      {status.message && (
        <div className={`alert ${status.type}`}>
          {status.message}
        </div>
      )}
      
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="fullName">Nom complet *</label>
          <input
            type="text"
            id="fullName"
            name="fullName"
            value={formData.fullName}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="company">Entreprise</label>
          <input
            type="text"
            id="company"
            name="company"
            value={formData.company}
            onChange={handleChange}
          />
        </div>

        <div className="form-group">
          <label htmlFor="email">Email *</label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="phone">Telephone</label>
          <input
            type="tel"
            id="phone"
            name="phone"
            value={formData.phone}
            onChange={handleChange}
          />
        </div>

        <div className="form-group">
          <label htmlFor="requestType">Type de demande *</label>
          <select
            id="requestType"
            name="requestType"
            value={formData.requestType}
            onChange={handleChange}
            required
          >
            <option value="">-- Selectionnez --</option>
            <option value="INFO">Demande d'information</option>
            <option value="QUOTE">Demande de devis</option>
            <option value="SUPPORT">Support technique</option>
            <option value="PARTNERSHIP">Partenariat</option>
            <option value="OTHER">Autre</option>
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="message">Message *</label>
          <textarea
            id="message"
            name="message"
            rows="5"
            value={formData.message}
            onChange={handleChange}
            required
          />
        </div>

        <button type="submit" disabled={loading}>
          {loading ? 'Envoi en cours...' : 'Envoyer le message'}
        </button>
      </form>
    </div>
  );
}
```

## Utilisation dans une page

```jsx
import ContactForm from './components/ContactForm';

export default function ContactPage() {
  return (
    <div className="container">
      <ContactForm />
    </div>
  );
}
```

---

# PARTIE 5 : INTEGRATION NEXT.JS

## Composant Next.js (App Router)

Creez `app/contact/page.tsx` :

```tsx
'use client';

import { useState, FormEvent } from 'react';

// CONFIGURATION - Modifiez cette URL
const API_URL = 'https://votre-api.com/api/contact';

interface FormData {
  fullName: string;
  company: string;
  email: string;
  phone: string;
  requestType: string;
  message: string;
}

export default function ContactPage() {
  const [formData, setFormData] = useState<FormData>({
    fullName: '',
    company: '',
    email: '',
    phone: '',
    requestType: '',
    message: ''
  });
  
  const [status, setStatus] = useState<{ type: string; message: string }>({ 
    type: '', 
    message: '' 
  });
  const [loading, setLoading] = useState(false);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setStatus({ type: '', message: '' });

    try {
      const response = await fetch(API_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...formData,
          company: formData.company || null,
          phone: formData.phone || null
        })
      });

      if (response.ok) {
        setStatus({ 
          type: 'success', 
          message: 'Merci ! Votre message a ete envoye avec succes.' 
        });
        setFormData({
          fullName: '',
          company: '',
          email: '',
          phone: '',
          requestType: '',
          message: ''
        });
      } else {
        const error = await response.json();
        throw new Error(error.message || 'Erreur serveur');
      }
    } catch (error) {
      setStatus({ 
        type: 'error', 
        message: `Erreur : ${error instanceof Error ? error.message : 'Erreur inconnue'}` 
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="container mx-auto p-8 max-w-md">
      <h1 className="text-2xl font-bold mb-6">Contactez-nous</h1>
      
      {status.message && (
        <div className={`p-4 mb-4 rounded ${
          status.type === 'success' 
            ? 'bg-green-100 text-green-800' 
            : 'bg-red-100 text-red-800'
        }`}>
          {status.message}
        </div>
      )}
      
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block mb-1">Nom complet *</label>
          <input
            type="text"
            name="fullName"
            value={formData.fullName}
            onChange={handleChange}
            required
            className="w-full p-2 border rounded"
          />
        </div>

        <div>
          <label className="block mb-1">Entreprise</label>
          <input
            type="text"
            name="company"
            value={formData.company}
            onChange={handleChange}
            className="w-full p-2 border rounded"
          />
        </div>

        <div>
          <label className="block mb-1">Email *</label>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
            className="w-full p-2 border rounded"
          />
        </div>

        <div>
          <label className="block mb-1">Telephone</label>
          <input
            type="tel"
            name="phone"
            value={formData.phone}
            onChange={handleChange}
            className="w-full p-2 border rounded"
          />
        </div>

        <div>
          <label className="block mb-1">Type de demande *</label>
          <select
            name="requestType"
            value={formData.requestType}
            onChange={handleChange}
            required
            className="w-full p-2 border rounded"
          >
            <option value="">-- Selectionnez --</option>
            <option value="INFO">Demande d'information</option>
            <option value="QUOTE">Demande de devis</option>
            <option value="SUPPORT">Support technique</option>
            <option value="PARTNERSHIP">Partenariat</option>
            <option value="OTHER">Autre</option>
          </select>
        </div>

        <div>
          <label className="block mb-1">Message *</label>
          <textarea
            name="message"
            rows={5}
            value={formData.message}
            onChange={handleChange}
            required
            className="w-full p-2 border rounded"
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full p-3 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
        >
          {loading ? 'Envoi en cours...' : 'Envoyer le message'}
        </button>
      </form>
    </main>
  );
}
```

---

# PARTIE 6 : INTEGRATION SVELTE

## Composant Svelte

Creez `src/lib/ContactForm.svelte` :

```svelte
<script>
  // CONFIGURATION - Modifiez cette URL
  const API_URL = 'https://votre-api.com/api/contact';

  let formData = {
    fullName: '',
    company: '',
    email: '',
    phone: '',
    requestType: '',
    message: ''
  };

  let status = { type: '', message: '' };
  let loading = false;

  async function handleSubmit() {
    loading = true;
    status = { type: '', message: '' };

    try {
      const response = await fetch(API_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...formData,
          company: formData.company || null,
          phone: formData.phone || null
        })
      });

      if (response.ok) {
        status = { 
          type: 'success', 
          message: 'Merci ! Votre message a ete envoye avec succes.' 
        };
        formData = {
          fullName: '',
          company: '',
          email: '',
          phone: '',
          requestType: '',
          message: ''
        };
      } else {
        const error = await response.json();
        throw new Error(error.message || 'Erreur serveur');
      }
    } catch (error) {
      status = { 
        type: 'error', 
        message: 'Erreur : ' + error.message 
      };
    } finally {
      loading = false;
    }
  }
</script>

<div class="contact-form">
  <h2>Contactez-nous</h2>

  {#if status.message}
    <div class="alert {status.type}">
      {status.message}
    </div>
  {/if}

  <form on:submit|preventDefault={handleSubmit}>
    <div class="form-group">
      <label for="fullName">Nom complet *</label>
      <input
        type="text"
        id="fullName"
        bind:value={formData.fullName}
        required
      />
    </div>

    <div class="form-group">
      <label for="company">Entreprise</label>
      <input
        type="text"
        id="company"
        bind:value={formData.company}
      />
    </div>

    <div class="form-group">
      <label for="email">Email *</label>
      <input
        type="email"
        id="email"
        bind:value={formData.email}
        required
      />
    </div>

    <div class="form-group">
      <label for="phone">Telephone</label>
      <input
        type="tel"
        id="phone"
        bind:value={formData.phone}
      />
    </div>

    <div class="form-group">
      <label for="requestType">Type de demande *</label>
      <select
        id="requestType"
        bind:value={formData.requestType}
        required
      >
        <option value="">-- Selectionnez --</option>
        <option value="INFO">Demande d'information</option>
        <option value="QUOTE">Demande de devis</option>
        <option value="SUPPORT">Support technique</option>
        <option value="PARTNERSHIP">Partenariat</option>
        <option value="OTHER">Autre</option>
      </select>
    </div>

    <div class="form-group">
      <label for="message">Message *</label>
      <textarea
        id="message"
        rows="5"
        bind:value={formData.message}
        required
      />
    </div>

    <button type="submit" disabled={loading}>
      {loading ? 'Envoi en cours...' : 'Envoyer le message'}
    </button>
  </form>
</div>

<style>
  .contact-form {
    max-width: 500px;
    margin: 0 auto;
    padding: 20px;
  }

  .form-group {
    margin-bottom: 15px;
  }

  label {
    display: block;
    margin-bottom: 5px;
    font-weight: 500;
  }

  input, select, textarea {
    width: 100%;
    padding: 10px;
    border: 1px solid #ccc;
    border-radius: 5px;
    font-size: 16px;
  }

  button {
    width: 100%;
    padding: 15px;
    background: #007bff;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    font-size: 16px;
  }

  button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  button:hover:not(:disabled) {
    background: #0056b3;
  }

  .alert {
    padding: 15px;
    margin-bottom: 20px;
    border-radius: 5px;
  }

  .alert.success {
    background: #d4edda;
    color: #155724;
  }

  .alert.error {
    background: #f8d7da;
    color: #721c24;
  }
</style>
```

## Utilisation dans une page Svelte

```svelte
<script>
  import ContactForm from '$lib/ContactForm.svelte';
</script>

<ContactForm />
```

---

# PARTIE 7 : CONFIGURATION VARIABLE D'ENVIRONNEMENT

## React / Next.js

Creez `.env.local` :

```env
NEXT_PUBLIC_API_URL=https://votre-api.com/api/contact
```

Utilisez dans le code :

```javascript
const API_URL = process.env.NEXT_PUBLIC_API_URL;
```

## Svelte / SvelteKit

Creez `.env` :

```env
PUBLIC_API_URL=https://votre-api.com/api/contact
```

Utilisez dans le code :

```javascript
import { PUBLIC_API_URL } from '$env/static/public';
const API_URL = PUBLIC_API_URL;
```

## Vercel

1. Allez sur votre projet Vercel
2. Settings > Environment Variables
3. Ajoutez :

| Key | Value |
|-----|-------|
| NEXT_PUBLIC_API_URL | https://votre-api.com/api/contact |

---

# PARTIE 8 : CHECKLIST DE DEPLOIEMENT

## Backend

- [ ] Backend deploye sur VPS ou Codespaces
- [ ] CORS configure avec le domaine du frontend
- [ ] Gmail configure (MAIL_USER, MAIL_PASSWORD)
- [ ] Test via Swagger fonctionne
- [ ] Emails recus

## Frontend

- [ ] Composant de formulaire cree
- [ ] URL de l'API configuree
- [ ] Variable d'environnement (optionnel)
- [ ] Test en local fonctionne
- [ ] Deploy sur Vercel

## Vercel

- [ ] Variable d'environnement ajoutee (si utilisee)
- [ ] Site deploye
- [ ] Test du formulaire sur le site live
- [ ] Emails recus

---

# PARTIE 9 : DEPANNAGE

## Erreur CORS

Verifiez que votre domaine Vercel est dans la liste CORS du backend :

```java
configuration.setAllowedOrigins(List.of(
    "https://votre-site.vercel.app"
));
```

## Erreur 404

Verifiez l'URL de l'API :
- Correcte : `https://api.domain.com/api/contact`
- Incorrecte : `https://api.domain.com/contact`

## Erreur Network

- Verifiez que le backend est accessible
- Verifiez que le port est public (Codespaces)
- Verifiez le protocole (https vs http)

## Pas d'email recu

- Verifiez les logs du backend
- Verifiez la configuration Gmail
- Verifiez le dossier spam

---

# RESUME

| Framework | Fichier a creer | Import |
|-----------|-----------------|--------|
| HTML | Aucun, code inline | - |
| React | `ContactForm.jsx` | `import ContactForm from './ContactForm'` |
| Next.js | `app/contact/page.tsx` | Route automatique |
| Svelte | `ContactForm.svelte` | `import ContactForm from '$lib/ContactForm.svelte'` |

## URL API a configurer

```javascript
// Codespaces (temporaire)
const API_URL = 'https://CODESPACE-8080.app.github.dev/api/contact';

// VPS (permanent)
const API_URL = 'https://api.votredomaine.com/api/contact';
```

