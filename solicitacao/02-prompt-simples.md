# PROMPT SIMPLE: Contact Form API

## Copy this prompt to any AI

```
Create a contact form that sends data to my API.

ENDPOINT: POST https://YOUR-BACKEND-URL/api/contact

JSON BODY:
{
  "fullName": "string (required)",
  "email": "string (required)",
  "company": "string (optional)",
  "phone": "string (optional)",
  "requestType": "INFO" or "DEMO" or "SUPPORT" or "PARTNERSHIP" or "OTHER" (required),
  "message": "string (required)"
}

RESPONSE: JSON with id, status, createdAt

MY TECH: [React / Next.js / Svelte / Vue / HTML]
MY URL: [your actual backend URL]
STYLE: [your design preferences]
```

---

## Example filled

```
Create a contact form that sends data to my API.

ENDPOINT: POST https://api.mysite.com/api/contact

JSON BODY:
{
  "fullName": "string (required)",
  "email": "string (required)",
  "company": "string (optional)",
  "phone": "string (optional)",
  "requestType": "INFO" or "DEMO" or "SUPPORT" or "PARTNERSHIP" or "OTHER" (required),
  "message": "string (required)"
}

RESPONSE: JSON with id, status, createdAt

MY TECH: Svelte
MY URL: https://api.mysite.com
STYLE: dark theme, minimal
```

---

## Ultra minimal

```
Contact form POST to https://YOUR-URL/api/contact

Fields: fullName, email, requestType (INFO/DEMO/SUPPORT/PARTNERSHIP/OTHER), message
Optional: company, phone

Tech: [your framework]
```

