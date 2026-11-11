# Deploying QuickCab

This puts the app online for free:

| Part | Where | Cost |
|---|---|---|
| MySQL database | [Aiven](https://aiven.io) free MySQL plan | free |
| Spring Boot API | [Render](https://render.com) web service (Docker) | free, sleeps after 15 min idle |
| React site | Render static site | free |

You end up with two links: `https://quickcab-web.onrender.com` (the app) and
`https://quickcab-api.onrender.com` (the API). The exact names depend on what's free.

> The free API instance goes to sleep when nobody uses it, so the first request
> after a while takes about a minute while it wakes up. Fine for a portfolio demo.

## 1. Create the database (Aiven)

1. Sign up at aiven.io and create a **MySQL** service on the **Free** plan.
2. When it's running, open it and copy the **Host**, **Port**, **User** (`avnadmin`) and **Password**.
3. Your JDBC URL is:
   ```
   jdbc:mysql://HOST:PORT/defaultdb?sslMode=REQUIRED&serverTimezone=UTC
   ```

The tables are created automatically the first time the API starts.

## 2. Deploy the API and the site (Render)

1. Sign up at render.com with your GitHub account.
2. Click **New → Blueprint** and pick the `QuickCab` repository. Render reads `render.yaml` and shows two services.
3. Fill in the values it asks for:

   | Service | Variable | Value |
   |---|---|---|
   | quickcab-api | `DB_URL` | the JDBC URL from step 1 |
   | quickcab-api | `DB_USERNAME` | `avnadmin` |
   | quickcab-api | `DB_PASSWORD` | the Aiven password |
   | quickcab-api | `CORS_ALLOWED_ORIGINS` | `https://quickcab-web.onrender.com` (your site's URL) |
   | quickcab-web | `VITE_API_BASE_URL` | `https://quickcab-api.onrender.com/api` (your API's URL + `/api`) |

   `JWT_SECRET` is generated for you.
4. Click **Apply**. The first build takes a few minutes.

If Render gives your services different URLs than the ones above, update
`CORS_ALLOWED_ORIGINS` and `VITE_API_BASE_URL` to match, then redeploy
(**Manual Deploy → Deploy latest commit**). The site needs a redeploy because
the API address is baked in at build time.

## 3. Check it works

1. Open the site URL, register a rider and log in.
2. In a private window, register a driver with the same vehicle type.
3. Book a ride as the rider. It should appear on the driver's dashboard straight away.

Every push to `main` redeploys both services automatically.

## Troubleshooting

- **"Network Error" when logging in:** `VITE_API_BASE_URL` is wrong, or the API is still waking up. Wait a minute and retry.
- **Blocked by CORS in the browser console:** `CORS_ALLOWED_ORIGINS` must exactly match the site URL (https, no trailing slash).
- **API keeps restarting:** check the Render logs. A database connection error usually means a typo in `DB_URL` or the password.
