# PROMPT: Integrate Contact Form with Backend API

## Instructions for AI

Copy and paste this prompt to any AI assistant to integrate a contact form into your website. Replace the placeholder values with your actual data.

---

## PROMPT TO COPY

```
I need you to integrate a contact form into my website that sends data to my backend API.

## API ENDPOINT

- **URL**: https://YOUR-BACKEND-URL/api/contact
- **Method**: POST
- **Content-Type**: application/json

## REQUEST BODY (JSON)

{
  "fullName": "string (required)",
  "email": "string (required, valid email)",
  "company": "string (optional)",
  "phone": "string (optional)",
  "requestType": "string (required, one of: INFO, DEMO, SUPPORT, PARTNERSHIP, OTHER)",
  "message": "string (required)"
}

## EXAMPLE REQUEST

fetch('https://YOUR-BACKEND-URL/api/contact', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    fullName: 'John Doe',
    email: 'john@example.com',
    company: 'Acme Inc',
    phone: '+1-555-123-4567',
    requestType: 'INFO',
    message: 'I would like more information about your services.'
  })
})
.then(response => response.json())
.then(data => console.log('Success:', data))
.catch(error => console.error('Error:', error));

## EXPECTED RESPONSE (SUCCESS - 200 OK)

{
  "id": 1,
  "fullName": "John Doe",
  "email": "john@example.com",
  "company": "Acme Inc",
  "phone": "+1-555-123-4567",
  "requestType": "INFO",
  "message": "I would like more information about your services.",
  "status": "NEW",
  "createdAt": "2026-01-20T15:30:00",
  "updatedAt": "2026-01-20T15:30:00"
}

## POSSIBLE ERRORS

- 400 Bad Request: Missing required fields or invalid email format
- 500 Internal Server Error: Server issue

## REQUEST TYPES (requestType field)

| Value | Description |
|-------|-------------|
| INFO | General information request |
| DEMO | Request for a demo |
| SUPPORT | Technical support request |
| PARTNERSHIP | Business partnership inquiry |
| OTHER | Other type of request |

## FORM FIELDS

Create a form with these fields:

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| fullName | text | Yes | Min 2 characters |
| email | email | Yes | Valid email format |
| company | text | No | - |
| phone | tel | No | - |
| requestType | select | Yes | One of the 5 options above |
| message | textarea | Yes | Min 10 characters |

## REQUIREMENTS

1. Display a loading state while sending
2. Show success message after successful submission
3. Show error message if submission fails
4. Clear form after successful submission
5. Validate fields before sending
6. Handle network errors gracefully

## MY WEBSITE TECHNOLOGY

[REPLACE WITH YOUR TECHNOLOGY: React, Next.js, Svelte, Vue, Angular, HTML/CSS/JS, etc.]

## MY BACKEND URL

[REPLACE WITH YOUR ACTUAL BACKEND URL]

## DESIGN PREFERENCES

[OPTIONAL: Describe your preferred style - colors, layout, etc.]
```

---

## EXAMPLE: FILLED PROMPT FOR REACT

```
I need you to integrate a contact form into my website that sends data to my backend API.

## API ENDPOINT

- **URL**: https://api.mywebsite.com/api/contact
- **Method**: POST
- **Content-Type**: application/json

## REQUEST BODY (JSON)

{
  "fullName": "string (required)",
  "email": "string (required, valid email)",
  "company": "string (optional)",
  "phone": "string (optional)",
  "requestType": "string (required, one of: INFO, DEMO, SUPPORT, PARTNERSHIP, OTHER)",
  "message": "string (required)"
}

## MY WEBSITE TECHNOLOGY

React with TypeScript and Tailwind CSS

## MY BACKEND URL

https://api.mywebsite.com

## DESIGN PREFERENCES

Modern dark theme, purple accent colors, rounded corners, subtle animations
```

---

## EXAMPLE: FILLED PROMPT FOR SVELTE

```
I need you to integrate a contact form into my website that sends data to my backend API.

## API ENDPOINT

- **URL**: https://contact-api.vercel.app/api/contact
- **Method**: POST
- **Content-Type**: application/json

## REQUEST BODY (JSON)

{
  "fullName": "string (required)",
  "email": "string (required, valid email)",
  "company": "string (optional)",
  "phone": "string (optional)",
  "requestType": "string (required, one of: INFO, DEMO, SUPPORT, PARTNERSHIP, OTHER)",
  "message": "string (required)"
}

## MY WEBSITE TECHNOLOGY

SvelteKit with vanilla CSS

## MY BACKEND URL

https://contact-api.vercel.app

## DESIGN PREFERENCES

Minimalist light theme, clean lines, blue accent color
```

---

## EXAMPLE: FILLED PROMPT FOR NEXT.JS

```
I need you to integrate a contact form into my website that sends data to my backend API.

## API ENDPOINT

- **URL**: https://my-spring-api.railway.app/api/contact
- **Method**: POST
- **Content-Type**: application/json

## REQUEST BODY (JSON)

{
  "fullName": "string (required)",
  "email": "string (required, valid email)",
  "company": "string (optional)",
  "phone": "string (optional)",
  "requestType": "string (required, one of: INFO, DEMO, SUPPORT, PARTNERSHIP, OTHER)",
  "message": "string (required)"
}

## MY WEBSITE TECHNOLOGY

Next.js 14 with App Router, TypeScript, and shadcn/ui components

## MY BACKEND URL

https://my-spring-api.railway.app

## DESIGN PREFERENCES

Professional business style, gradient background, glass morphism effects
```

---

## QUICK REFERENCE

### Minimal fetch call (copy-paste ready)

```javascript
const submitForm = async (formData) => {
  const response = await fetch('https://YOUR-URL/api/contact', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      fullName: formData.fullName,
      email: formData.email,
      company: formData.company || '',
      phone: formData.phone || '',
      requestType: formData.requestType,
      message: formData.message
    })
  });
  
  if (!response.ok) throw new Error('Submission failed');
  return response.json();
};
```

### Valid requestType values

```
INFO | DEMO | SUPPORT | PARTNERSHIP | OTHER
```

### Required fields

```
fullName, email, requestType, message
```

### Optional fields

```
company, phone
```

