# Deploying WC Server to Railway

## Prerequisites
- Railway account at railway.app
- Railway CLI: npm install -g @railway/cli

## Steps
1. railway login
2. railway new (create new project)
3. railway up (deploy from project root)

## Environment Variables to set in Railway dashboard:
- DATABASE_URL=your_supabase_connection_string
- JWT_SECRET=your_strong_random_secret
- RESEND_API_KEY=your_resend_api_key
- FROM_EMAIL=noreply@yourdomain.com
- ENVIRONMENT=production

> No seeding required. First user registers through the app Welcome screen.

## After deployment:
In AppConfig.kt set IS_PRODUCTION = true
Update BASE_URL to your Railway app URL
