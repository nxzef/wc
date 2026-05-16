# Deploying WC Server to Google Cloud Run

The project is configured for automated deployment via GitHub Actions.

## Manual Deployment

If you need to deploy manually from your machine:

1. **Build and push image to Artifact Registry:**
   ```bash
   gcloud builds submit \
     --tag asia-south1-docker.pkg.dev/wedding-clouds/wc-repo/wc-server \
     .
   ```

2. **Deploy to Cloud Run:**
   ```bash
   gcloud run deploy wc-server \
     --image asia-south1-docker.pkg.dev/wedding-clouds/wc-repo/wc-server \
     --platform managed \
     --region asia-south1 \
     --allow-unauthenticated \
     --port 8080 \
     --memory 512Mi \
     --min-instances 0 \
     --max-instances 2
   ```

## Automated Deployment (GitHub Actions)

Every push to the `main` branch triggers a build and deployment.

### Required GitHub Secrets:
- `GCP_SA_KEY`: JSON key for the `github-deploy` service account.
- `DATABASE_URL`: Full JDBC connection string to Supabase.

The workflow:
1. Builds the server Shadow JAR.
2. Builds and pushes the Docker image to Google Artifact Registry.
3. Deploys the service to Google Cloud Run.
4. Updates the `latest_version` in the Supabase `app_config` table.
