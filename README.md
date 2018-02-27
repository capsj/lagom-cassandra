# Create Docker Registry
```bash
docker run -d -p 5000:5000 --restart always --name registry registry:2
```

# Install _kubectl_ on Mac
```bash
brew install kubectl

# Confirm:
kubectl version
```

# Install _minikube_ on Mac
```bash
# Double check latest release: https://github.com/kubernetes/minikube/releases
curl -Lo minikube https://storage.googleapis.com/minikube/releases/v0.25.0/minikube-darwin-amd64 && chmod +x minikube
sudo mv minikube /usr/local/bin/

# Confirm:
minikube version
```

# Start Minikube
```bash
# required for new-minikube and deploy
export REGISTRY=10.15.5.51:5000

# once-off
./deploy/kubernetes/scripts/install --new-minikube

# Confirm "cluster: Running":
watch minikube status

minikube dashboard
```

# Start Cassandra and Ingress
```bash
./deploy/kubernetes/scripts/install --cassandra
./deploy/kubernetes/scripts/install --nginx

# Confirm:
watch kubectl get pods
```

# Build and Deploy
```bash
./deploy/kubernetes/scripts/install --build
./deploy/kubernetes/scripts/install --deploy
```

# Get Ingress URL
```bash
minikube service nginx-ingress --url
```

# Clean up
```bash
./deploy/kubernetes/scripts/install --delete
```
