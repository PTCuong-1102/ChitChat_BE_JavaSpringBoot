# 🚀 ChitChat Backend Deployment Guide

## 📋 Prerequisites

- GitHub account
- Backend code pushed to GitHub repository
- Database setup (using existing Neon PostgreSQL)

## 🌐 Deployment Options

### Option 1: Railway (Recommended) 🚂

#### Step 1: Setup Railway Account
1. Go to [Railway.app](https://railway.app)
2. Sign up/Login with GitHub
3. Connect your GitHub account

#### Step 2: Deploy Backend
1. Click "New Project"
2. Select "Deploy from GitHub repo"
3. Choose your backend repository
4. Railway will auto-detect it's a Spring Boot app

#### Step 3: Configure Environment Variables
Add these environment variables in Railway dashboard:

```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://ep-empty-snowflake-a1mipsm3-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require
DATABASE_USERNAME=neondb_owner
DATABASE_PASSWORD=npg_sFid7Gfq8DcS

# JWT Configuration
JWT_SECRET_KEY=dGhpcyBpcyBhIHNlY3VyZSBqd3Qgc2VjcmV0IGtleSBmb3IgY2hpdGNoYXQgYXBwbGljYXRpb24gd2l0aCBzdWZmaWNpZW50IGxlbmd0aA==
JWT_EXPIRATION=3600000

# CORS Configuration (Update with your Vercel domain)
CORS_ALLOWED_ORIGINS=https://your-vercel-domain.vercel.app,https://chit-chat-app-react-js.vercel.app

# Gemini AI Configuration
GEMINI_API_KEY=AIzaSyCcTP3r-KBxe3MwVKxW2opivTBZcu-jXuE

# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# File Upload
UPLOAD_DIR=uploads
```

#### Step 4: Set Build Configuration
Railway should automatically detect `pom.xml` and use Maven. If not, add:

```bash
# Build Command
mvn clean package -DskipTests

# Start Command
java -jar target/*.jar
```

---

### Option 2: Render 🎨

#### Step 1: Setup Render Account
1. Go to [Render.com](https://render.com)
2. Sign up/Login with GitHub

#### Step 2: Create Web Service
1. Click "New +" → "Web Service"
2. Connect your GitHub repository
3. Configure:
   - **Environment**: Java
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/*.jar`

#### Step 3: Configure Environment Variables
Add the same environment variables as Railway option above.

---

### Option 3: Heroku 🟣

#### Step 1: Setup Heroku Account
1. Go to [Heroku.com](https://heroku.com)
2. Sign up/Login
3. Install Heroku CLI

#### Step 2: Deploy
```bash
# Login to Heroku
heroku login

# Create new app
heroku create your-chitchat-backend

# Set environment variables
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set DATABASE_URL=jdbc:postgresql://ep-empty-snowflake-a1mipsm3-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require
heroku config:set DATABASE_USERNAME=neondb_owner
heroku config:set DATABASE_PASSWORD=npg_sFid7Gfq8DcS
heroku config:set JWT_SECRET_KEY=dGhpcyBpcyBhIHNlY3VyZSBqd3Qgc2VjcmV0IGtleSBmb3IgY2hpdGNoYXQgYXBwbGljYXRpb24gd2l0aCBzdWZmaWNpZW50IGxlbmd0aA==
heroku config:set CORS_ALLOWED_ORIGINS=https://your-vercel-domain.vercel.app

# Deploy
git push heroku main
```

---

## 🔧 Update Frontend Configuration

After deploying backend, update your frontend config:

### Step 1: Update Environment Variables in Vercel
1. Go to your Vercel dashboard
2. Select your project
3. Go to Settings → Environment Variables
4. Add:

```bash
NEXT_PUBLIC_BACKEND_URL=https://your-backend-domain.railway.app
NEXT_PUBLIC_WEBSOCKET_URL=wss://your-backend-domain.railway.app
```

### Step 2: Update Frontend Config (if needed)
The `config.ts` file should automatically pick up the environment variables.

---

## 🔍 Testing Deployment

1. **Health Check**: Visit `https://your-backend-domain.com/actuator/health`
2. **API Test**: Try `https://your-backend-domain.com/api/auth/test`
3. **WebSocket Test**: Check browser console for WebSocket connection

---

## 🚨 Common Issues & Solutions

### Issue 1: CORS Errors
```bash
# Solution: Update CORS_ALLOWED_ORIGINS environment variable
CORS_ALLOWED_ORIGINS=https://your-actual-vercel-domain.vercel.app
```

### Issue 2: Database Connection Failed
```bash
# Solution: Check database URL and credentials
DATABASE_URL=jdbc:postgresql://your-db-host/dbname?sslmode=require
```

### Issue 3: JWT Token Issues
```bash
# Solution: Ensure JWT_SECRET_KEY is set in production
JWT_SECRET_KEY=your-production-secret-key
```

### Issue 4: File Upload Issues
```bash
# Solution: Set upload directory
UPLOAD_DIR=uploads
```

---

## 🔄 Automatic Deployment

### Railway/Render:
- Automatically redeploys when you push to GitHub
- Set up branch protection for production

### Heroku:
- Set up automatic deployment from GitHub in Heroku dashboard

---

## 📊 Monitoring

### Railway:
- Built-in metrics and logs
- Real-time deployment logs

### Render:
- Service dashboard with metrics
- Log streaming

### Heroku:
- Heroku dashboard
- Use `heroku logs --tail` for real-time logs

---

## 🔐 Security Considerations

1. **Environment Variables**: Never commit sensitive data to GitHub
2. **HTTPS**: All platforms provide HTTPS by default
3. **Database**: Use connection pooling for production
4. **CORS**: Restrict to your actual frontend domain only
5. **JWT**: Use strong secret keys in production

---

## 📝 Next Steps

1. Deploy backend using one of the above options
2. Update frontend environment variables in Vercel
3. Test the connection
4. Monitor logs for any issues
5. Set up domain (optional)

---

## 📞 Support

If you encounter issues:
1. Check platform-specific documentation
2. Review application logs
3. Verify environment variables
4. Test database connectivity
5. Check CORS configuration 